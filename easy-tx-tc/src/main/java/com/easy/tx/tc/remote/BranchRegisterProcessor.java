package com.easy.tx.tc.remote;

import com.easy.tx.constant.MessageType;
import com.easy.tx.constant.TxStatus;
import com.easy.tx.lock.BranchTxLock;
import com.easy.tx.message.BranchTxSession;
import com.easy.tx.message.GlobalTxSession;
import com.easy.tx.remote.RemoteMessage;
import com.easy.tx.remote.RemoteMessageProcessor;
import com.easy.tx.remote.RemoteResponse;
import com.easy.tx.tc.tx.StoreManager;
import com.easy.tx.util.RpcMessageUtil;
import com.easy.tx.util.TxObjectMapper;

/**
 * 分支注册处理器
 *
 * @author hzh
 * @date 2023/08/22
 */
public class BranchRegisterProcessor implements RemoteMessageProcessor {
    private final BranchTxLock branchTxLock;
    private final StoreManager storeManager;

    public BranchRegisterProcessor(BranchTxLock branchTxLock, StoreManager storeManager) {
        this.branchTxLock = branchTxLock;
        this.storeManager = storeManager;
    }

    /**
     * 处理消息
     */
    @Override
    public void process(RemoteResponse response, RemoteMessage message) {
        String body = message.getBody();
        BranchTxSession branchInfo = TxObjectMapper.toBean(body,BranchTxSession.class);
        branchInfo.setStatus(TxStatus.Register.name());
        //是否上锁
        boolean tryLock = true;
        if (branchInfo.getLockKey() != null && branchInfo.getLockValue() != null) {
            tryLock = branchTxLock.tryLock(branchInfo.getLockKey(), branchInfo.getGlobalTxId(), branchInfo.getLockValue(), branchInfo.getExpireTime());
        }

        //注册分支
        boolean success = storeManager.addBranch(branchInfo);

        //返回结果
        RemoteMessage responseMessage = RpcMessageUtil.buildResponseMessage(tryLock && success, MessageType.BRANCH_REGISTER);
        response.setRpcMessage(responseMessage);
    }

    @Override
    public Integer getType() {
        return MessageType.BRANCH_REGISTER;
    }
}
