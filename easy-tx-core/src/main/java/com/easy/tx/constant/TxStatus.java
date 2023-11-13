package com.easy.tx.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 事务的状态
 */
@AllArgsConstructor
@Getter
public enum TxStatus {
    /**
     * 事务注册
     */
    // PHASE 1: can accept new branch registering.
    Register(1, "transaction start"),
    
    /**
     * 全局事务提交中，但未结束
     */
    // Committing.
    Committing(2, "2Phase committing"),
    
    /**
     * Rollbacking global status.
     */
    // Rollbacking
    Rollbacking(4, "2Phase rollbacking"),
    
    /**
     * PHASE 2: Final Status: will NOT change any more.
     */
    // Finally: global transaction is successfully committed.
    Committed(9, "global transaction completed with status committed"),
    
    /**
     * The Commit failed.
     */
    // Finally: failed to commit
    CommitFailed(10, "2Phase commit failed"),
    
    /**
     * The Rollbacked.
     */
    // Finally: global transaction is successfully rollbacked.
    Rollbacked(11, "global transaction completed with status rollbacked"),
    
    /**
     * The Rollback failed.
     */
    // Finally: failed to rollback
    RollbackFailed(12, "global transaction completed but rollback failed");
    
    private final int code;
    
    private final String desc;
    
    public static int getTxStatusCode(String name) {
        for (TxStatus value : values()) {
            if (value.name().equals(name)) {
                return value.getCode();
            }
        }
        return 0;
    }
    
    
}
