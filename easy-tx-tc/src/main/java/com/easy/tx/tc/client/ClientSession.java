package com.easy.tx.tc.client;

import io.netty.channel.ChannelHandlerContext;
import lombok.Data;


@Data
public class ClientSession {
    
    //不被序列化
    private transient ChannelHandlerContext channel;
    /**
     * 注册时间
     */
    private Long registerTime;
    
    /**
     * 更新时间
     */
    private Long updateTime;
    
    /**
     * 客户端地址
     */
    private String clientId;
    /**
     * 应用id
     */
    private String applicationId;
    
    /**
     * 来源 预留
     */
    private String source;
}
