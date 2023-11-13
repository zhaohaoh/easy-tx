package com.easy.tx.remote;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * fuwu信息
 *
 * @author hzh
 * @date 2023/08/15
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AddressInfo {
    /**
     * 源地址
     */
    private String sourceAddress;
    /**
     * 目标地址
     */
    private String targetAddress;
}
