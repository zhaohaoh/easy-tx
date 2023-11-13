package com.easy.tx.tc.manager;

import com.easy.tx.constant.GlobalConfigCache;
import com.easy.tx.constant.MessageType;
import com.easy.tx.constant.TxStatus;
import com.easy.tx.lock.BranchTxLock;
import com.easy.tx.message.BranchTxSession;
import com.easy.tx.message.GlobalTxSession;
import com.easy.tx.remote.AddressInfo;
import com.easy.tx.remote.RemoteMessage;
import com.easy.tx.tc.properties.TcGlobalConfigCache;
import com.easy.tx.tc.tx.StoreManager;
import com.easy.tx.tc.client.ClientContext;
import com.easy.tx.tc.client.ClientSession;
import com.easy.tx.tc.remote.RemotingServer;
import org.springframework.util.CollectionUtils;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * tc全局执行人
 *
 * @author hzh
 * @date 2023/11/10
 */
public class TcGlobalExecutor {
    private final Duration expiredTime = Duration.ofSeconds(7200);
    private final BranchTxLock branchTxLock;
    
    private final StoreManager storeManager;
    
    private final RemotingServer remotingServer;
    
    private final String tcAddress = TcGlobalConfigCache.tcGlobalConfig.getTcAddress();
    
    public TcGlobalExecutor(BranchTxLock branchTxLock, StoreManager storeManager, RemotingServer remotingServer) {
        this.branchTxLock = branchTxLock;
        this.storeManager = storeManager;
        this.remotingServer = remotingServer;
    }
    
    /**
     * 提交
     *
     * @param globalTxSession 全局事务
     * @return boolean
     */
    public boolean commit(GlobalTxSession globalTxSession) {
        storeManager.updateGlobalStatus(globalTxSession.getGlobalTxId(), TxStatus.Committing.name());
        
        List<String> branchIds = storeManager.getAllBranchIds(globalTxSession);
        
        List<String> failBranchs = new ArrayList<>();
        
        // 发送到各个服务执行undolog
        for (String branchId : branchIds) {
            BranchTxSession branch = storeManager.getBranch(globalTxSession.getGlobalTxId(), branchId);
            //获取一个可用的clientSession
            ClientSession clientSession = ClientContext.getClientByApplicationId(branch.getApplicationId());
            if (clientSession == null) {
                failBranchs.add(branchId);
                storeManager.updateBranchStatus(branch, TxStatus.CommitFailed.name());
                continue;
            }
            //服务端发送到客户端  删除undolog  rpc有通道维持心跳。http靠自己维持心跳
            AddressInfo addressInfo = new AddressInfo(tcAddress, clientSession.getClientId());
            RemoteMessage remoteMessage = remotingServer.sendSyncRequest(addressInfo, branch, MessageType.BM_COMMIT);
            String res = (String) remoteMessage.getBody();
            if (!Boolean.getBoolean(res)) {
                failBranchs.add(branchId);
                storeManager.updateBranchStatus(branch, TxStatus.CommitFailed.name());
            }
        }
        
        //失败
        if (!CollectionUtils.isEmpty(failBranchs)) {
            storeManager.updateGlobalStatusAndRetryCount(globalTxSession.getGlobalTxId(), TxStatus.CommitFailed.name());
            return false;
        }
    
        releaseTx(globalTxSession);
        return true;
    }
    
    /**
     * 回滚
     *
     * @param globalTxSession 全局事务
     * @return boolean
     */
    public boolean rollback(GlobalTxSession globalTxSession) {
        
        storeManager.updateGlobalStatus(globalTxSession.getGlobalTxId(), TxStatus.Rollbacking.name());
        
        List<String> branchIds = storeManager.getAllBranchIds(globalTxSession);
        
        // 发送到各个服务执行undolog
        List<String> failBranchs = new ArrayList<>();
        for (String branchId : branchIds) {
            BranchTxSession branch = storeManager.getBranch(globalTxSession.getGlobalTxId(), branchId);
            ClientSession clientSession = ClientContext.getClientByApplicationId(branch.getApplicationId());
            if (clientSession == null) {
                failBranchs.add(branchId);
                storeManager.updateBranchStatus(branch, TxStatus.CommitFailed.name());
                continue;
            }
            //服务端发送到客户端  删除undolog  rpc有通道维持心跳。http靠自己维持心跳
            AddressInfo addressInfo = new AddressInfo(tcAddress, clientSession.getClientId());
            RemoteMessage remoteMessage = remotingServer
                    .sendSyncRequest(new AddressInfo(addressInfo.getTargetAddress(), addressInfo.getSourceAddress()),
                            branch, MessageType.BM_ROLLBACK);
            String res = (String) remoteMessage.getBody();
            if (!Boolean.getBoolean(res)) {
                failBranchs.add(branchId);
                storeManager.updateBranchStatus(branch, TxStatus.RollbackFailed.name());
            }
        }
        
        //回滚失败
        if (!CollectionUtils.isEmpty(failBranchs)) {
            storeManager.updateGlobalStatusAndRetryCount(globalTxSession.getGlobalTxId(), TxStatus.RollbackFailed.name());
            return false;
        }
    
        releaseTx(globalTxSession);
        return true;
    }
    
    public void undoLogDelete(ClientSession clientSession){
        //服务端发送到客户端  删除undolog  rpc有通道维持心跳。http靠自己维持心跳
        AddressInfo addressInfo = new AddressInfo(tcAddress, clientSession.getClientId());
        remotingServer.sendSyncRequest(addressInfo, expiredTime.toMillis(), MessageType.UNDOLOG_DELETE);
    }
   
    
    /**
     * 释放事务
     *
     * @param globalTxSession 全局事务一场
     */
    public void releaseTx(GlobalTxSession globalTxSession) {
        //根据全局事务 删除所有分支事务
        storeManager.removeBranch(globalTxSession);
        
        //等回滚完成才做这些操作
        //删除全局存储
        storeManager.removeGlobal(globalTxSession);
        
        //解锁  无论事务是否成功  到期会自动解锁。这个锁随着事务的超时时间释放。 事务和事务日志不会
        branchTxLock.unLock(globalTxSession.getGlobalTxId());
    }
    
}
