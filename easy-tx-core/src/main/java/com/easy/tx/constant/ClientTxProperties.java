package com.easy.tx.constant;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 事务特性
 *
 * @author hzh
 * @date 2023/11/09
 */
@Configuration
@ConfigurationProperties(prefix = "easy.tx.client")
@Data
public class ClientTxProperties {
    
    /**
     * tc地址
     */
    private String tcAddress;
    
    /**
     * 自身服务地址
     */
    private String address;
    
    /**
     * 自身服务名
     */
    private String applicationId;
 
}
