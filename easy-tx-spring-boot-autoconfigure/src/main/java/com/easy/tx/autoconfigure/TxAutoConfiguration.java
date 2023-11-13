package com.easy.tx.autoconfigure;


import com.easy.tx.SagaComponentTxManager;
import com.easy.tx.advisor.GlobalTransactionAnnotationAdvisor;
import com.easy.tx.advisor.SagaTransactionAnnotationAdvisor;
import com.easy.tx.annotation.GlobalTransaction;
import com.easy.tx.annotation.SagaComponent;
import com.easy.tx.constant.ClientTxProperties;
import com.easy.tx.constant.GlobalConfig;
import com.easy.tx.constant.GlobalConfigCache;
import com.easy.tx.interceptor.GlobalTransactionInterceptor;
import com.easy.tx.interceptor.SagaTransactionInterceptor;
import com.easy.tx.manager.branch.BranchTxResourceManager;
import com.easy.tx.manager.SagaGlobalTxManager;
import com.easy.tx.manager.branch.DefaultBranchTxResourceManager;
import com.easy.tx.manager.global.DefaultGlobalTxResourceManager;
import com.easy.tx.manager.global.GlobalTxResourceManager;
import com.easy.tx.remote.*;
import com.easy.tx.store.undo.UndoLogLocalStore;
import com.easy.tx.store.undo.UndoLogStore;
import com.easy.tx.strategy.RedisTxIdGenertater;
import com.easy.tx.strategy.TxIdGenerater;
import com.easy.tx.util.InetUtils;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RedissonClient;
import org.springframework.aop.Advisor;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 事务配置
 *
 * @author hzh
 * @date 2023/08/02
 */
@Configuration
public class TxAutoConfiguration implements InitializingBean {
    
    @Value("${server.port:80}")
    private String port;
    
    @Value("${spring.application.name:easyTxApplication}")
    private String applicationId;
    
    @Autowired
    private ClientTxProperties clientTxProperties;
    
    /**
     * 事务调用程序存储
     *
     * @return {@link UndoLogStore}
     */
    @Bean
    public UndoLogStore undoLogStore() {
        return new UndoLogLocalStore();
    }
    
    
    /**
     * 持有事务的客户端
     *
     * @return {@link RemotingClient}
     */
    @Bean
    public RemotingClient remotingClient() {
        return new HttpRemotingClient();
    }
    
    
    /**
     * 分支事务资源经理
     *
     * @param remotingClient 远程处理客户端
     * @return {@link BranchTxResourceManager}
     */
    @Bean
    public BranchTxResourceManager branchTxResourceManager(RemotingClient remotingClient) {
        return new DefaultBranchTxResourceManager(remotingClient);
    }
    
    /**
     * 全局事务资源经理
     *
     * @param remotingClient 远程处理客户端
     * @return {@link GlobalTxResourceManager}
     */
    @Bean
    public GlobalTxResourceManager globalTxResourceManager(RemotingClient remotingClient) {
        return new DefaultGlobalTxResourceManager(remotingClient);
    }
    
    
    /**
     * 全局事务注释顾问  目前仅支持saga分布式事务
     *
     * @return {@link Advisor}
     */
    @Bean
    public Advisor globalTransactionAnnotationAdvisor(SagaGlobalTxManager sagaGlobalTxManager) {
        GlobalTransactionInterceptor interceptor = new GlobalTransactionInterceptor();
        interceptor.setGlobalTxManager(sagaGlobalTxManager);
        return new GlobalTransactionAnnotationAdvisor(interceptor, GlobalTransaction.class);
    }
    
    /**
     * saga事务注释顾问
     *
     * @return {@link Advisor}
     */
    @Bean
    public Advisor sagaTransactionAnnotationAdvisor(SagaComponentTxManager sagaComponentTxManager) {
        SagaTransactionInterceptor interceptor = new SagaTransactionInterceptor();
        interceptor.setSagaComponentTxExecutorService(sagaComponentTxManager);
        return new SagaTransactionAnnotationAdvisor(interceptor, SagaComponent.class);
    }
    
    
    @Bean
    public TxIdGenerater txIdGenerater(RedissonClient redissonClient) {
        return new RedisTxIdGenertater(redissonClient);
    }
    
    @Override
    public void afterPropertiesSet() throws Exception {
        GlobalConfigCache.GLOBAL_CONFIG = new GlobalConfig();
        
        //服务器地址
        
        GlobalConfigCache.GLOBAL_CONFIG.setApplicationId(
                StringUtils.isNotBlank(clientTxProperties.getApplicationId()) ? clientTxProperties.getApplicationId()
                        : applicationId);
        
        GlobalConfigCache.GLOBAL_CONFIG.setTcAddress(clientTxProperties.getTcAddress());
        if (clientTxProperties.getAddress() != null) {
            GlobalConfigCache.GLOBAL_CONFIG.setClientAddress(clientTxProperties.getAddress());
        } else {
            GlobalConfigCache.GLOBAL_CONFIG
                    .setClientAddress(InetUtils.findFirstNonLoopbackHostInfo().getIpAddress() + ":" + port);
        }
    }
}
