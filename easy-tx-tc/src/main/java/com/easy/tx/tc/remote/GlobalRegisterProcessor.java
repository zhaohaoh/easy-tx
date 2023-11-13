package com.easy.tx.tc.remote;

import com.easy.tx.constant.TxStatus;
import com.easy.tx.message.BranchTxSession;
import com.easy.tx.message.GlobalTxSession;
import com.easy.tx.remote.RemoteMessage;
import com.easy.tx.remote.RemoteMessageProcessor;
import com.easy.tx.remote.RemoteResponse;
import com.easy.tx.tc.tx.StoreManager;
import com.easy.tx.util.RpcMessageUtil;
import com.easy.tx.util.TxObjectMapper;

import static com.easy.tx.constant.MessageType.GLOBAL_REGISTER;

public class GlobalRegisterProcessor implements RemoteMessageProcessor {
    private StoreManager storeManager;

    public GlobalRegisterProcessor(StoreManager storeManager) {
        this.storeManager = storeManager;
    }

    @Override
    public void process(RemoteResponse response, RemoteMessage message) throws Exception {
        String body = message.getBody();
        GlobalTxSession globalTxSession = TxObjectMapper.toBean(body,GlobalTxSession.class);
        globalTxSession.setStatus(TxStatus.Register.name());
        storeManager.putGlobal(globalTxSession);
        //返回结果
        RemoteMessage responseMessage = RpcMessageUtil.buildResponseMessage(true, getType());
        response.setRpcMessage(responseMessage);
    }

    @Override
    public Integer getType() {
        return GLOBAL_REGISTER;
    }
}
