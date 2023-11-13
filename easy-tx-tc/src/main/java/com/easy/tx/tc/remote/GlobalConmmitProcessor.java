package com.easy.tx.tc.remote;

import com.easy.tx.constant.MessageType;
import com.easy.tx.message.GlobalTxSession;
import com.easy.tx.remote.*;
import com.easy.tx.tc.manager.TcGlobalExecutor;
import com.easy.tx.util.RpcMessageUtil;
import com.easy.tx.util.TxObjectMapper;

/**
 * 分支注册处理器
 *
 * @author hzh
 * @date 2023/08/22
 */
public class GlobalConmmitProcessor implements RemoteMessageProcessor {
    private final TcGlobalExecutor globalConmmitManager;

    public GlobalConmmitProcessor(TcGlobalExecutor globalConmmitManager) {
        this.globalConmmitManager = globalConmmitManager;
    }

    /**
     * 处理消息
     */
    @Override
    public void process(RemoteResponse response, RemoteMessage message) {
        String body = message.getBody();

        GlobalTxSession globalTxSession = TxObjectMapper.toBean(body,GlobalTxSession.class);
        
        globalConmmitManager.commit(globalTxSession);
        
        //返回结果
        RemoteMessage responseMessage = RpcMessageUtil.buildResponseMessage(true, getType());
        response.setRpcMessage(responseMessage);
    }

    @Override
    public Integer getType() {
        return MessageType.GLOBAL_COMMIT;
    }
}
