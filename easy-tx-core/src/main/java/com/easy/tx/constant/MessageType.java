package com.easy.tx.constant;

public interface MessageType {
    /**
     * 心跳
     */
    Integer KEEP_AVAILABLE = 1;
    /**
     * 解锁
     */
    Integer UN_LOCK = 7;

    /**
     * 锁
     */
    Integer LOCK_RESP = 8;
    /**
     * 解锁
     */
    Integer UN_LOCK_RESP = 9;


    /**
     * 全局注册
     */
    Integer GLOBAL_REGISTER = 11;
    /**
     * 全局提交
     */
    Integer GLOBAL_COMMIT= 12;
    /**
     * 全局回滚
     */
    Integer GLOBAL_ROLLBACK= 13;

    /**
     * 分支注册
     */
    Integer BRANCH_REGISTER = 21;
    /**
     * 分支提交
     */
    Integer BRANCH_COMMIT= 22;
    /**
     * 分支提交
     */
    Integer BRANCH_ROLLBACK= 23;


    /**
     * 回滚时执行UNDOLOG
     */
    Integer BM_ROLLBACK = 31;
    /**
     * 回滚时执行UNDOLOG
     */
    Integer BM_COMMIT = 32;
    
    /**
     * 回滚时执行UNDOLOG
     */
    Integer UNDOLOG_DELETE = 44;
}
