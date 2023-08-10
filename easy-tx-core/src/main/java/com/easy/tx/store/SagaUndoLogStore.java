package com.easy.tx.store;


/**
 * 事务调用程序存储
 *
 * @author hzh
 * @date 2023/08/01
 */
public interface SagaUndoLogStore {
    /**
     * 获取事务调用程序
     *
     * @param localTxId 当地事务id
     */
    SagaUndoLog getUndoLog(String localTxId);

    /**
     * 保存事务调用程序
     *
     * @param localTxId 当地事务id
     */
    SagaUndoLog addUndoLog(String localTxId, SagaUndoLog sagaUndoLog);

    /**
     * 删除事务调用程序
     *
     * @param localTxId 当地事务id
     */
    void removeUndoLog(String localTxId);
}
