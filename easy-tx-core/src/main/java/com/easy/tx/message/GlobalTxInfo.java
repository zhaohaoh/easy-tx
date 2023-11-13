package com.easy.tx.message;

import lombok.Data;

/**
 * 全局事务存储信息
 */
@Data
public class GlobalTxInfo {
    /**
     * 全局事务id
     */
    private String globalTxId;

    /**
     * 交易名称
     */
    private String transactionName;


    /**
     * 状态
     */
    private Integer status;

    /**
     * 开始时间
     */
    private Long beginTime;

    /**
     * 到期时间
     */
    private Long expireTime;
}
