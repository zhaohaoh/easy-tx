package com.easy.tx.message;

import lombok.Data;

@Data
public class TxLockInfo {
    /**
     * 锁定键
     */
    private String lockKey;
    /**
     * 全局事务id
     */
    private String globalTxId;
    /**
     * 锁的值
     */
    private Object lockValue;
    /**
     * 到期时间
     */
    private Long expireTime;

    public TxLockInfo(String lockKey, String globalTxId, Object lockValue, Long expireTime) {
        this.lockKey = lockKey;
        this.globalTxId = globalTxId;
        this.lockValue = lockValue;
        this.expireTime = expireTime;
    }
    public TxLockInfo(String lockKey, String globalTxId) {
        this.lockKey = lockKey;
        this.globalTxId = globalTxId;
    }
}
