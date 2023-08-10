package com.easy.tx.pojo;

/**
 * 全局事务执行人
 *
 * @author hzh
 * @date 2023/08/09
 */
public interface GlobalTransactionalExecutor {

    Object execute() throws Throwable;

    GlobalTransactionalInfo getTransactionInfo();
}
