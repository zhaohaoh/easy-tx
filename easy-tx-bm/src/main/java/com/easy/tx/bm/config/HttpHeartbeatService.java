package com.easy.tx.bm.config;

import com.easy.tx.constant.GlobalConfig;
import com.easy.tx.constant.GlobalConfigCache;
import com.easy.tx.remote.RemotingClient;
import com.sun.org.apache.xml.internal.security.Init;
import com.sun.xml.internal.ws.api.policy.PolicyResolver;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.SmartLifecycle;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static com.easy.tx.constant.MessageType.KEEP_AVAILABLE;


/**
 * http心跳服务
 *
 * @author hzh
 * @date 2023/11/03
 */
public class HttpHeartbeatService implements HeartbeatService, ApplicationListener<ApplicationReadyEvent> {
    
    private RemotingClient remotingClient;
    
    
    public HttpHeartbeatService(RemotingClient remotingClient) {
        this.remotingClient = remotingClient;
    }
    
    
    @Override
    public void ping() {
        //ping并且注册应用id
        String applicationId = GlobalConfigCache.GLOBAL_CONFIG.getApplicationId();
        remotingClient.sendSyncRequest(applicationId, KEEP_AVAILABLE);
    }
    
 
    
    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        ping();
    }
}
