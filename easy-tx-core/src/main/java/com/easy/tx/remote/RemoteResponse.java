package com.easy.tx.remote;

import io.netty.channel.ChannelHandlerContext;
import lombok.Data;

/**
 *  返回体
 *
 * @author hzh
 * @date 2023/08/15
 */
@Data
public class RemoteResponse {
    /**
     * 通道处理程序上下文
     */
    private ChannelHandlerContext channelHandlerContext;
    /**
     * 返回的rpc消息
     */
    private RemoteMessage rpcMessage;
}
