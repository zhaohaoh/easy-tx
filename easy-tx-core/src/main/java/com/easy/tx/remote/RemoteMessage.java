package com.easy.tx.remote;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class RemoteMessage {
    /**
     * id
     */
    private String id;
    /**
     * 消息类型
     */
    private Integer messageType;
    /**
     * 头部
     */
    private Map<String, String> headMap = new HashMap<>();
    /**
     * 消息体
     */
    private String body;
    /**
     * 消息源
     */
    private String proccessType;
    /**
     * 地址信息
     */
    private AddressInfo addressInfo;
}
