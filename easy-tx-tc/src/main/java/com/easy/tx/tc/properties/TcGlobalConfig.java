package com.easy.tx.tc.properties;

import com.easy.tx.remote.AddressInfo;
import lombok.Data;

import java.time.Duration;

@Data
public class TcGlobalConfig {
    
    /**
     * 事务保存时间
     */
    private Duration storageTime;
    /**
     * 提交重试次数
     */
    private Integer commitRetryCount;
    /**
     * 回滚重试次数
     */
    private Integer rollbackRetryCount;
    /**
     * tc地址
     */
    private String tcAddress;
}
