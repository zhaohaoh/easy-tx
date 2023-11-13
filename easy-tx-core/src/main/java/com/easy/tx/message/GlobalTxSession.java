package com.easy.tx.message;

import lombok.Data;

/**
 * 当前事务会话
 *
 * @author hzh
 * @date 2023/08/18
 */
@Data
public class GlobalTxSession {
    /**
     * 全局事务id
     */
    private String globalTxId;

    /**
     * 分支类型
     */
    private Integer branchType;
    /**
     * 事务到期时间
     */
    private Long expireTime;
    /**
     * 事务开始时间
     */
    private Long beginTime;

    /**
     * 全局事务的状态
     */
    private String status;
    
    /**
     * 全局事务的重试次数
     */
    private Integer retryCount=0;

    public GlobalTxSession(String globalTxId, Integer branchType, Long expireTime, Long beginTime) {
        this.globalTxId = globalTxId;
        this.branchType = branchType;
        this.expireTime = expireTime;
        this.beginTime = beginTime;
    }
    public GlobalTxSession(String globalTxId, Integer branchType) {
        this.globalTxId = globalTxId;
        this.branchType = branchType;
    }

    public GlobalTxSession() {
    }
}
