package com.easy.tx.tc.remote;

import com.easy.tx.remote.AddressInfo;
import com.easy.tx.remote.RemoteMessage;

/**
 *
 * @author hzh
 * @date 2023/08/11
 */
public interface RemotingServer {

    RemoteMessage sendSyncRequest(AddressInfo serverAddress, Object msg, Integer messageType);

    void sendAsyncRequest(AddressInfo serverAddress, Object msg, Integer messageType);
}
