package com.easy.tx.tc.remote;

import com.easy.tx.remote.*;


/**
 * 本地远程服务端
 *
 * @author hzh
 * @date 2023/08/14
 */
public class LocalRemotingServer extends AbstractRemotingServer implements RemotingServer {
    private RemoteClientProccessManager remoteClientProccessManager;

    public LocalRemotingServer(RemoteClientProccessManager remoteClientProccessManager) {
        this.remoteClientProccessManager = remoteClientProccessManager;
    }

    @Override
    public RemoteMessage sendSyncRequest(AddressInfo serverAddress, RemoteMessage rpcMessage) {
        RemoteResponse response = new RemoteResponse();
        remoteClientProccessManager.processMessage(response,rpcMessage);
        return response.getRpcMessage();
    }

    @Override
    public void sendAsyncRequest(AddressInfo serverAddress, RemoteMessage rpcMessage) {
        RemoteResponse response = new RemoteResponse();
        remoteClientProccessManager.processMessage(response,rpcMessage);
    }

}
