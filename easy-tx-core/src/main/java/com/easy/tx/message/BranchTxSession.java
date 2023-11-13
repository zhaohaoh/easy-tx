package com.easy.tx.message;

import com.easy.tx.constant.GlobalConfigCache;
import com.easy.tx.store.undo.SagaUndoLog;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
public class BranchTxSession {
    /**
     * 全局事务id
     */
    private String globalTxId;
    /**
     * 分支事务id
     */
    private String branchTxId;

    /**
     * 锁key
     */
    private String lockKey;

    /**
     * 锁values
     */
    private Object lockValue;
    /**
     * 事务到期时间
     */
    private Long expireTime;
    /**
     * 事务开始时间
     */
    private Long beginTime;
    /**
     * 分支类型
     */
    private Integer branchType;
    
    /**
     * 应用id
     */
    private String applicationId;
    
    /**
     * 分支状态
     */
    private String status;


    public BranchTxSession(String globalTxId,Long beginTime, Long expireTime,Integer branchType) {
        this.globalTxId = globalTxId;
        this.beginTime=beginTime;
        this.expireTime = expireTime;
        this.branchType = branchType;
        this.applicationId= GlobalConfigCache.GLOBAL_CONFIG.getApplicationId();
    }

    public BranchTxSession() {
    }
}
