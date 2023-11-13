package com.easy.tx.manager;

import com.easy.tx.constant.BranchType;
import com.easy.tx.context.GlobalTxContext;
import com.easy.tx.manager.global.GlobalTxResourceManager;
import com.easy.tx.message.GlobalTxSession;
import com.easy.tx.store.undo.UndoLogStore;
import com.easy.tx.strategy.TxIdGenerater;
import lombok.extern.slf4j.Slf4j;

/**
 * saga全局事务管理者  核心类
 *
 * @author hzh
 * @date 2023/08/10
 */
@Slf4j
public class SagaGlobalTxManager extends AbstractTxManager {


    private final GlobalTxResourceManager globalTxResourceManager;

    public SagaGlobalTxManager(UndoLogStore undoLogStore, GlobalTxResourceManager globalTxResourceManager, TxIdGenerater txIdGenerater) {
        super(undoLogStore, txIdGenerater);
        this.globalTxResourceManager = globalTxResourceManager;
    }

    /**
     * 手动开启事务
     */
    @Override
    public String startTransaction(Long expireTime) {
        String globalTxId = GlobalTxContext.getGlobalTxId();
        if (globalTxId == null) {
            globalTxId = newTxId();
            GlobalTxContext.bind(globalTxId, expireTime);
        }
        long beginTime = System.currentTimeMillis();
        GlobalTxSession globalTxSession = new GlobalTxSession(globalTxId, BranchType.SAGA.ordinal(), expireTime, beginTime);
        globalTxResourceManager.register(globalTxSession);
        return globalTxId;
    }

    @Override
    public void commit() {
        String globalTxId = GlobalTxContext.getGlobalTxId();
        GlobalTxSession globalTxSession = new GlobalTxSession(globalTxId, BranchType.SAGA.ordinal());
        globalTxResourceManager.commit(globalTxSession);
    }

    /**
     * TODO 回滚  所有的分支事务发送请求由tc接受后回调到本服务处理。
     * 除了基本的事务id 所有的threadlocal都可以改造成redis和mysql
     */
    @Override
    public void rollback() {
        String globalTxId = GlobalTxContext.getGlobalTxId();
        GlobalTxSession globalTxSession = new GlobalTxSession(globalTxId, BranchType.SAGA.ordinal());
        globalTxResourceManager.rollback(globalTxSession);
    }

}
