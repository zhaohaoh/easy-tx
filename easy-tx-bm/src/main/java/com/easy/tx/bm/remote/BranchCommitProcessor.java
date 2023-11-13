package com.easy.tx.bm.remote;

import com.easy.tx.constant.MessageType;
import com.easy.tx.message.BranchTxSession;
import com.easy.tx.remote.RemoteMessage;
import com.easy.tx.remote.RemoteMessageProcessor;
import com.easy.tx.remote.RemoteResponse;
import com.easy.tx.store.undo.UndoLogStore;
import com.easy.tx.util.RpcMessageUtil;
import com.easy.tx.util.TxObjectMapper;
import lombok.extern.slf4j.Slf4j;

/**
 * 分支回滚处理器
 *
 * @author hzh
 * @date 2023/08/22
 */
@Slf4j
public class BranchCommitProcessor implements RemoteMessageProcessor {

    private final UndoLogStore undoLogStore;

    public BranchCommitProcessor(UndoLogStore undoLogStore) {
        this.undoLogStore = undoLogStore;
    }

    /**
     * 处理消息
     */
    @Override
    public void process(RemoteResponse response, RemoteMessage message) {
        String body = message.getBody();
        BranchTxSession branchTxSession = TxObjectMapper.toBean(body,BranchTxSession.class);
        String branchTxId = branchTxSession.getBranchTxId();
        String globalTxId = branchTxSession.getGlobalTxId();
        undoLogStore.removeUndoLog(globalTxId, branchTxId);
        if (log.isDebugEnabled()) {
            log.debug("branchCommit removeUndoLog branchTxId:{}", branchTxId);
        }

        //返回结果
        RemoteMessage responseMessage = RpcMessageUtil.buildResponseMessage(true, getType());
        response.setRpcMessage(responseMessage);
    }

    @Override
    public Integer getType() {
        return MessageType.BM_COMMIT;
    }
}
