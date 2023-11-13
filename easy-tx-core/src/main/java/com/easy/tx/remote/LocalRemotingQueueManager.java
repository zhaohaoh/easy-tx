package com.easy.tx.remote;

import javafx.util.Pair;

import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.SynchronousQueue;

public class LocalRemotingQueueManager {
    /**
     * 服务器使用队列
     */
    public static final SynchronousQueue<Pair<RemoteMessage, CompletableFuture<RemoteResponse>>> SERVER_CONSUME_QUEUE = new SynchronousQueue<>();
    /**
     * 客户端消费队列
     */
    public static final SynchronousQueue<Pair<RemoteMessage, CompletableFuture<RemoteResponse>>> CLIENT_CONSUME_QUEUE = new SynchronousQueue<>();
}
