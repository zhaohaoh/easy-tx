package com.easy.tx.store.undo;


/**
 * 事务调用程序存储 redis版本要做 并且使用
 *
 * @author hzh
 * @date 2023/08/01
 */
public interface UndoLogStore {
    /**
     * 获取事务调用程序
     *
     */
    UndoLog getUndoLog(String globalTxId,String branchTxId);

    /**
     * 保存事务调用程序
     *
     */
    UndoLog addUndoLog(UndoLog undoLog);

    /**
     * 删除事务调用程序
     *
     * @param branchTxId 当地事务id
     */

    void removeUndoLog(String globalTxId,String branchTxId);
    /**
     * 删除事务调用程序
     *
     * @param globalTxId 全局事务id
     */
    void removeUndoLog(String globalTxId);
    
    /**
     * 删除过期日志
     *
     * @param timeoutSecond 超时秒
     */
    boolean removeExpireUndoLog(Long timeoutSecond);
}
