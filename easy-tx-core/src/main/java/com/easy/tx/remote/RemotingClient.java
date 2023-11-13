package com.easy.tx.remote;

/**
 * 远程客户端  把branch_table  lock_table 和全局事务表对象都设计出来。然后消息的传递通过这个对象。
 *
 * @author hzh
 * @date 2023/08/11
 */
public interface RemotingClient {
 

    void sendAsyncRequest(Object msg, Integer messageType);

    Object sendSyncRequest(Object msg, Integer messageType);

}
