package com.easy.tx.store;


import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 事务调用程序本地存储
 *
 * @author hzh
 * @date 2023/08/01
 */
public class SagaUndoLogLocalStore implements SagaUndoLogStore {

    private static final Map<String, SagaUndoLog> LOCAL_CONTEXT = new ConcurrentHashMap<>();


    /**
     * 获取事务调用程序
     *
     * @param localTxId 当地事务id
     */
    @Override
    public SagaUndoLog getUndoLog(String localTxId) {
        return LOCAL_CONTEXT.get(localTxId);
    }

    /**
     * 保存事务调用程序
     *
     * @param localTxId 当地事务id
     * @param sagaUndoLog
     * @return {@link SagaUndoLog}
     */
    @Override
    public SagaUndoLog addUndoLog(String localTxId, SagaUndoLog sagaUndoLog) {
        return LOCAL_CONTEXT.put(localTxId, sagaUndoLog);

    }

    /**
     * 删除事务调用程序
     *
     * @param localTxId 当地事务id
     */
    @Override
    public void removeUndoLog(String localTxId) {
        LOCAL_CONTEXT.remove(localTxId);
    }
}
