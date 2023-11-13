package com.easy.tx.tc.client;


import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


/**
 * 客户端上下文
 *
 * @author hzh
 * @date 2023/10/27
 */
@Slf4j
public class ClientContext {
    
    private static final RestTemplate restTemplate = new RestTemplate();
    
    public ClientContext() {
        HttpComponentsClientHttpRequestFactory httpRequestFactory = new HttpComponentsClientHttpRequestFactory();
        httpRequestFactory.setConnectionRequestTimeout(5000);
        httpRequestFactory.setConnectTimeout(5000);
        httpRequestFactory.setReadTimeout(5000);
        restTemplate.setRequestFactory(httpRequestFactory);
    }
    
    /**
     * 客户端维持
     */
    public static final Map<String, Set<ClientSession>> clients = new ConcurrentHashMap<>();
    
    private static final ScheduledExecutorService removeTask = new ScheduledThreadPoolExecutor(1,
            new BasicThreadFactory.Builder().namingPattern("clients-removeTask-%d").daemon(false).build());
    
    static {
        // 定时删除过期客户端
        removeTask.scheduleAtFixedRate(ClientContext::removeExpireClient, 64, 64, TimeUnit.SECONDS);
    }
    
    /**
     * 添加客户端
     *
     * @param clientSession 客户端会话
     */
    public static void putClient(ClientSession clientSession) {
        String clientId = clientSession.getClientId();
        if (clients.get(clientId) == null) {
            log.info("The client is added first :{}", clientSession);
            clientSession.setRegisterTime(clientSession.getUpdateTime());
        }
        clients.computeIfAbsent(clientSession.getApplicationId(), a -> {
            Set<ClientSession> set = new HashSet<>();
            set.add(clientSession);
            return set;
        });
        if (log.isDebugEnabled()) {
            log.debug("The client is update registr :{}", clientSession);
        }
    }
    
    /**
     * 超时客户端删除
     */
    public static void removeExpireClient() {
        List<ClientSession> clientSessions = new ArrayList<>();
        clients.forEach((k, v) -> {
            for (ClientSession expireClient : v) {
                if (expireClient == null) {
                    continue;
                }
                boolean ping = ping(expireClient.getClientId());
                if (ping) {
                } else {
                    clientSessions.add(expireClient);
                }
            }
        });
        clients.forEach((k, v) -> {
            clientSessions.forEach(v::remove);
        });
    }
    
    /**
     * 获取客户根据应用id
     */
    public static ClientSession getClientByApplicationId(String applicationId) {
        Set<ClientSession> clientSessions = clients.get(applicationId);
        if (CollectionUtils.isEmpty(clientSessions)) {
            log.error("getClientByApplicationId not has valid client applicationId:{}",applicationId);
            return null;
        }
        //TODO 获取其中一个有效的客户端
        for (ClientSession clientSession : clientSessions) {
            boolean ping = ping(clientSession.getClientId());
            if (ping) {
                return clientSession;
            }
        }
        log.error("getClientByApplicationId not has valid client applicationId:{}",applicationId);
        return null;
    }
    
    
    public static boolean ping(String address) {
        try {
            boolean http = StringUtils.startsWith(address, "http");
            if (!http) {
                address = "http://" + address + "/heartbeat";
            }
            restTemplate.getForObject(address, Map.class);
        } catch (Exception e) {
            return false;
        }
        return true;
    }
}
