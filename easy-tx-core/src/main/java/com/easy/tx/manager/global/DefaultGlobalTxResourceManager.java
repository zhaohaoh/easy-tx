package com.easy.tx.manager.global;

import com.easy.tx.constant.MessageType;
import com.easy.tx.manager.branch.BranchTxResourceManager;
import com.easy.tx.message.BranchTxSession;
import com.easy.tx.message.GlobalTxSession;
import com.easy.tx.message.TxLockInfo;
import com.easy.tx.remote.RemotingClient;

/**
 * 分支事务资源管理者
 *
 * @author hzh
 * @date 2023/08/14
 */
public class DefaultGlobalTxResourceManager implements GlobalTxResourceManager {
    /**
     * 分支事务锁
     */
    private final RemotingClient remotingClient;

    public DefaultGlobalTxResourceManager(RemotingClient remotingClient) {
        this.remotingClient = remotingClient;
    }

    /**
     * 提交事务
     */
    @Override
    public void commit(GlobalTxSession globalTxSession) {
        remotingClient.sendSyncRequest(globalTxSession, MessageType.GLOBAL_COMMIT);
    }

    /**
     * 提交事务
     */
    @Override
    public void rollback(GlobalTxSession globalTxSession) {
        remotingClient.sendSyncRequest( globalTxSession, MessageType.GLOBAL_ROLLBACK);
    }

    /**
     * 分支注册
     *
     */
    @Override
    public boolean register(GlobalTxSession globalTxSession) {
        remotingClient.sendSyncRequest(globalTxSession, MessageType.GLOBAL_REGISTER);
        return true;
    }

}
