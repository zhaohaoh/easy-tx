package com.easy.tx.manager;

import javax.naming.OperationNotSupportedException;

/**
 * 事务经理
 *
 * @author hzh
 * @date 2023/08/02
 */
public interface TxManager {

    /**
     * 开始事务
     *
     * @return {@link String}
     */
    String startTransaction(Long expireTime);

    /**
     * 提交事务
     */
    void commit();

    /**
     * 回滚事务
     */
    void roback();

    /**
     * 新事务id
     *
     * @return {@link String}
     */
    String newTxId();

}
