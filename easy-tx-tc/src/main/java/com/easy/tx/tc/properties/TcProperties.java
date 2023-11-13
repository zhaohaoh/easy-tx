package com.easy.tx.tc.properties;

import com.easy.tx.util.InetUtils;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * 事务特性
 *
 * @author hzh
 * @date 2023/11/09
 */
@Configuration
@ConfigurationProperties(prefix = "easy.tx.tc")
@Data
public class TcProperties {
    
    /**
     * 事务保存时间
     */
    private Duration storageTime = Duration.ofHours(1);
    
    /**
     * 提交重试次数
     */
    private Integer commitRetryCount = 5;
    
    /**
     * 回滚重试次数
     */
    private Integer rollbackRetryCount = 5;
    
    private String tcAddress;
}
