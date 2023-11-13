package com.easy.tx.bm.remote;

import com.easy.tx.constant.MessageType;
import com.easy.tx.message.BranchTxSession;
import com.easy.tx.remote.RemoteMessage;
import com.easy.tx.remote.RemoteMessageProcessor;
import com.easy.tx.remote.RemoteResponse;
import com.easy.tx.store.undo.UndoLogStore;
import com.easy.tx.util.RpcMessageUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * 分支回滚处理器
 *
 * @author hzh
 * @date 2023/08/22
 */
@Slf4j
public class UndologDeleteProcessor implements RemoteMessageProcessor {

    private final UndoLogStore undoLogStore;

    public UndologDeleteProcessor(UndoLogStore undoLogStore) {
        this.undoLogStore = undoLogStore;
    }

    /**
     * 处理消息
     */
    @Override
    public void process(RemoteResponse response, RemoteMessage message) {
        String body = message.getBody();
        long timeout = Long.parseLong(body);
        boolean success = undoLogStore.removeExpireUndoLog(timeout);
        if (success){
            log.info("undolog remove expire undoLog success");
        }
    
        //返回结果
        RemoteMessage responseMessage = RpcMessageUtil.buildResponseMessage(true, getType());
        response.setRpcMessage(responseMessage);
    }

    @Override
    public Integer getType() {
        return MessageType.UNDOLOG_DELETE;
    }
}
