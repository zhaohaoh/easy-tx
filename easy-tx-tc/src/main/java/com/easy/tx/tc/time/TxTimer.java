package com.easy.tx.tc.time;

import com.easy.tx.constant.GlobalConfigCache;
import com.easy.tx.constant.TxStatus;
import com.easy.tx.message.GlobalTxSession;
import com.easy.tx.tc.properties.TcGlobalConfigCache;
import com.easy.tx.tc.tx.StoreManager;
import com.easy.tx.tc.client.ClientContext;
import com.easy.tx.tc.client.ClientSession;
import com.easy.tx.tc.manager.TcGlobalExecutor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 事务定时任务调度器
 *
 * @author hzh
 * @date 2023/10/26
 */
@Slf4j
public class TxTimer {
    
    
    private final ScheduledExecutorService undoLogDelete = new ScheduledThreadPoolExecutor(1,
            new BasicThreadFactory.Builder().namingPattern("undo_log_delete_%d").daemon(false).build());
    
    private final ScheduledExecutorService commitRetry = new ScheduledThreadPoolExecutor(1,
            new BasicThreadFactory.Builder().namingPattern("commitRetry_%d").daemon(false).build());
    
    private final ScheduledExecutorService rollbackRetry = new ScheduledThreadPoolExecutor(1,
            new BasicThreadFactory.Builder().namingPattern("rollbackRetry_%d").daemon(false).build());
    
    private final ScheduledExecutorService timeoutTx = new ScheduledThreadPoolExecutor(1,
            new BasicThreadFactory.Builder().namingPattern("timeoutTx_%d").daemon(false).build());
    
    private TcGlobalExecutor txGlobalExecutor;
    
    private StoreManager storeManager;
    
    
    public TxTimer(TcGlobalExecutor txGlobalExecutor, StoreManager storeManager) {
        this.txGlobalExecutor = txGlobalExecutor;
        this.storeManager = storeManager;
    }
    
    public void start() {
        //开启undolog删除
        startUndoLogDelete();
        //提交重试
        startCommitRetry();
        //回滚重试
        startRollbackRetry();
        //超时事务处理
        startTimeoutTx();
    }
    
    /**
     * 启动撤消日志 计划启动  10分钟 执行一次 如果是内存中的undolog必须尽快删除。如果是持久化的undolog可以延期保存
     */
    public void startUndoLogDelete() {
        String tcAddress = GlobalConfigCache.GLOBAL_CONFIG.getTcAddress();
        undoLogDelete.scheduleAtFixedRate(() -> {
            //超时多少时间的undolog进行删除
            
            Map<String, Set<ClientSession>> clients = ClientContext.clients;
            if (CollectionUtils.isEmpty(clients)) {
                return;
            }
            for (Set<ClientSession> value : clients.values()) {
                for (ClientSession clientSession : value) {
                    if (clientSession != null) {
                        txGlobalExecutor.undoLogDelete(clientSession);
                    }
                }
            }
        }, 0, 7200, TimeUnit.SECONDS);
    }
    
    /**
     * 计划启动  执行获取commit失败的事务，commit失败实际上就是undo_log没有删除。延时也会被删掉。影响不算很大。但是rollback失败不能不处理
     */
    public void startCommitRetry() {
        commitRetry.scheduleAtFixedRate(() -> {
            List<GlobalTxSession> globalTxSessions = storeManager.getGlobalTxByStatus(TxStatus.CommitFailed);
            for (GlobalTxSession globalTxSession : globalTxSessions) {
                if (globalTxSession != null && globalTxSession.getRetryCount()<= TcGlobalConfigCache.tcGlobalConfig
                        .getCommitRetryCount()) {
                    boolean commit = txGlobalExecutor.commit(globalTxSession);
                    log.info("tc retry commit global :{} result:{}", globalTxSession,commit);
                } else if (globalTxSession != null ){
                    txGlobalExecutor.releaseTx(globalTxSession);
                    log.error("tc retry commit releaseTx :{}  Over specified times", globalTxSession);
                }
            }
        }, 5, 5, TimeUnit.SECONDS);
    }
    
    public void startRollbackRetry() {
        rollbackRetry.scheduleAtFixedRate(() -> {
            List<GlobalTxSession> globalTxSessions = storeManager.getGlobalTxByStatus(TxStatus.RollbackFailed);
            for (GlobalTxSession globalTxSession : globalTxSessions) {
                if (globalTxSession != null && globalTxSession.getRetryCount()<= TcGlobalConfigCache.tcGlobalConfig
                        .getRollbackRetryCount()) {
                    boolean rollback = txGlobalExecutor.rollback(globalTxSession);
                    log.info("tc retry rollback global :{} result:{}", globalTxSession,rollback);
                }else if (globalTxSession != null ){
                    txGlobalExecutor.releaseTx(globalTxSession);
                    log.error("tc retry rollback releaseTx :{}  Over specified times", globalTxSession);
                }
            }
        }, 5, 5, TimeUnit.SECONDS);
    }
    
    
    /**
     * 开始超时事务 开始超时事务 超时事务设置成失败
     */
    public void startTimeoutTx() {
        timeoutTx.scheduleAtFixedRate(() -> {
            List<GlobalTxSession> globalTxSessions = storeManager.getGlobalTxByStatus(TxStatus.Rollbacking);
            for (GlobalTxSession globalTxSession : globalTxSessions) {
                Long expireTime = globalTxSession.getExpireTime();
                if (expireTime <= System.currentTimeMillis()) {
                    storeManager.updateGlobalStatus(globalTxSession.getGlobalTxId(), TxStatus.RollbackFailed.name());
                }
            }
            
            globalTxSessions = storeManager.getGlobalTxByStatus(TxStatus.Committing);
            for (GlobalTxSession globalTxSession : globalTxSessions) {
                Long expireTime = globalTxSession.getExpireTime();
                if (expireTime <= System.currentTimeMillis()) {
                    storeManager.updateGlobalStatus(globalTxSession.getGlobalTxId(), TxStatus.CommitFailed.name());
                }
            }
        }, 30, 30, TimeUnit.SECONDS);
    }
}
