package com.easy.tx.manager.branch;

import com.easy.tx.constant.MessageType;
import com.easy.tx.manager.branch.BranchTxResourceManager;
import com.easy.tx.message.BranchTxSession;
import com.easy.tx.message.GlobalTxSession;
import com.easy.tx.message.TxLockInfo;
import com.easy.tx.remote.RemotingClient;
import com.easy.tx.util.InetUtils;

/**
 * 分支事务资源管理者
 *
 * @author hzh
 * @date 2023/08/14
 */
public class DefaultBranchTxResourceManager implements BranchTxResourceManager {
    /**
     * 分支事务锁
     */
    private final RemotingClient remotingClient;

    public DefaultBranchTxResourceManager(RemotingClient remotingClient) {
        this.remotingClient = remotingClient;
    }

    /**
     * 提交事务
     */
    @Override
    public void commit(BranchTxSession branchTxSession) {
        remotingClient.sendSyncRequest( branchTxSession, MessageType.BRANCH_COMMIT);
    }

    /**
     * 提交事务
     */
    @Override
    public void rollback(BranchTxSession branchTxSession) {
        remotingClient.sendSyncRequest(branchTxSession, MessageType.BRANCH_ROLLBACK);
    }

    /**
     * 分支注册
     *
     */
    @Override
    public boolean branchRegister(BranchTxSession branchTxSession) {
        remotingClient.sendSyncRequest(branchTxSession, MessageType.BRANCH_REGISTER);
        return true;
    }
}
