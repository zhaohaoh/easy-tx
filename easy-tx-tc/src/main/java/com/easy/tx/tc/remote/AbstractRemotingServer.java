package com.easy.tx.tc.remote;

import com.easy.tx.constant.GlobalConfigCache;
import com.easy.tx.remote.AddressInfo;
import com.easy.tx.remote.RemoteMessage;
import com.easy.tx.util.RpcMessageUtil;

/**
 *  远程客户端
 *
 * @author hzh
 * @date 2023/08/14
 */
public abstract class AbstractRemotingServer implements RemotingServer {


    @Override
    public RemoteMessage sendSyncRequest(AddressInfo addressInfo, Object msg, Integer messageType) {
        RemoteMessage rpcMessage = RpcMessageUtil.buildRequestMessage(msg, addressInfo, messageType);
        return sendSyncRequest(addressInfo, rpcMessage);
    }

    @Override
    public void sendAsyncRequest(AddressInfo addressInfo, Object msg, Integer messageType) {
        RemoteMessage rpcMessage = RpcMessageUtil.buildRequestMessage(msg, addressInfo, messageType);
        sendAsyncRequest(addressInfo, rpcMessage);
    }


    public abstract RemoteMessage sendSyncRequest(AddressInfo serverAddress, RemoteMessage rpcMessage);


    public abstract void sendAsyncRequest(AddressInfo serverAddress, RemoteMessage rpcMessage);

}
