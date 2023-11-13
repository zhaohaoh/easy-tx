package com.easy.tx.tc.remote;

import com.easy.tx.constant.MessageType;
import com.easy.tx.remote.RemoteMessage;
import com.easy.tx.remote.RemoteMessageProcessor;
import com.easy.tx.remote.RemoteResponse;
import com.easy.tx.tc.client.ClientContext;
import com.easy.tx.tc.client.ClientSession;
import com.easy.tx.util.RpcMessageUtil;

/**
 * 分支心跳维持
 *
 * @author hzh
 * @date 2023/08/22
 */
public class KeepAvailableProcessor implements RemoteMessageProcessor {
    
    /**
     * 处理消息
     */
    @Override
    public void process(RemoteResponse response, RemoteMessage remoteMessage) {
        String sourceAddress = remoteMessage.getAddressInfo().getSourceAddress();
        Object body = remoteMessage.getBody();
        ClientSession clientSession = new ClientSession();
        long updateTime = System.currentTimeMillis();
        clientSession.setUpdateTime(updateTime);
        clientSession.setApplicationId(body.toString());
        clientSession.setClientId(sourceAddress);
        ClientContext.putClient( clientSession);
        //返回结果
        RemoteMessage responseMessage = RpcMessageUtil.buildResponseMessage(true, getType());
        response.setRpcMessage(responseMessage);
    }

    @Override
    public Integer getType() {
        return MessageType.KEEP_AVAILABLE;
    }
}
