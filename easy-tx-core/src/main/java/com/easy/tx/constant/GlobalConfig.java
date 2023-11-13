package com.easy.tx.constant;

import com.easy.tx.remote.AddressInfo;
import lombok.Data;

@Data
public class GlobalConfig {
    
    /**
     * tc地址
     */
    private String tcAddress;
    
    /**
     * 客户端地址
     */
    private String clientAddress;
    
    /**
     * 应用程序id
     */
    private String applicationId;
}
