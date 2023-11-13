package com.easy.tx.util;

import com.easy.tx.remote.AddressInfo;
import com.easy.tx.remote.RemoteMessage;

import java.util.UUID;

public class RpcMessageUtil {
    public static RemoteMessage buildRequestMessage(Object msg, AddressInfo addressInfo, Integer messageType) {
        RemoteMessage rpcMessage = new RemoteMessage();
        rpcMessage.setId(UUID.randomUUID().toString());
        rpcMessage.setMessageType(messageType);
        rpcMessage.setBody(TxObjectMapper.toJsonStr(msg));
        rpcMessage.setAddressInfo(addressInfo);
        return rpcMessage;
    }

    public static RemoteMessage buildResponseMessage(Object msg, Integer messageType) {
        RemoteMessage rpcMessage = new RemoteMessage();
        rpcMessage.setId(UUID.randomUUID().toString());
        rpcMessage.setMessageType(messageType);
        rpcMessage.setBody(TxObjectMapper.toJsonStr(msg));
        return rpcMessage;
    }
}
