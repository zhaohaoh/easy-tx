package com.easy.tx.remote;

import com.easy.tx.constant.GlobalConfig;
import com.easy.tx.constant.GlobalConfigCache;
import com.easy.tx.util.InetUtils;
import com.easy.tx.util.RpcMessageUtil;

/**
 * 摘要远程客户端
 *
 * @author hzh
 * @date 2023/08/14
 */
public abstract class AbstractRemotingClient implements RemotingClient {
    
    
    @Override
    public void sendAsyncRequest(Object msg, Integer messageType) {
        AddressInfo addressInfo = new AddressInfo(GlobalConfigCache.GLOBAL_CONFIG.getClientAddress(),
                GlobalConfigCache.GLOBAL_CONFIG.getTcAddress());
        RemoteMessage rpcMessage = RpcMessageUtil.buildRequestMessage(msg, addressInfo, messageType);
        sendAsyncRequest(rpcMessage);
    }
    
    @Override
    public Object sendSyncRequest(Object msg, Integer messageType) {
        AddressInfo addressInfo = new AddressInfo(GlobalConfigCache.GLOBAL_CONFIG.getClientAddress(),
                GlobalConfigCache.GLOBAL_CONFIG.getTcAddress());
        RemoteMessage rpcMessage = RpcMessageUtil.buildRequestMessage(msg, addressInfo, messageType);
        return this.sendSyncRequest(rpcMessage);
    }
    
    public abstract Object sendSyncRequest(RemoteMessage rpcMessage);
    
    
    public abstract void sendAsyncRequest(RemoteMessage rpcMessage);
    
    
}
