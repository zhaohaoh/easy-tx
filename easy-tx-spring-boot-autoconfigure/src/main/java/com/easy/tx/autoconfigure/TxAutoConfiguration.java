package com.easy.tx.autoconfigure;


import com.easy.tx.SagaComponentTxExecutorService;
import com.easy.tx.advisor.GlobalTransactionAnnotationAdvisor;
import com.easy.tx.advisor.SagaTransactionAnnotationAdvisor;
import com.easy.tx.annotation.GlobalTransaction;
import com.easy.tx.annotation.SagaComponent;
import com.easy.tx.interceptor.GlobalTransactionInterceptor;
import com.easy.tx.interceptor.SagaTransactionInterceptor;
import com.easy.tx.lock.LocalTxLock;
import com.easy.tx.manager.SagaGlobalTxManager;
import com.easy.tx.manager.SagaLocalTxManager;
import com.easy.tx.store.SagaUndoLogLocalStore;
import com.easy.tx.store.SagaUndoLogStore;
import com.easy.tx.strategy.RedisTxIdGenertater;
import com.easy.tx.strategy.TxIdGenerater;
import com.easy.tx.template.SagaTxComponentTemplate;
import com.easy.tx.template.SagaTxTemplate;
import org.redisson.api.RedissonClient;
import org.springframework.aop.Advisor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 事务配置
 *
 * @author hzh
 * @date 2023/08/02
 */
@Configuration
public class TxAutoConfiguration {


    /**
     * 事务调用程序存储
     *
     * @return {@link SagaUndoLogStore}
     */
    @Bean
    public SagaUndoLogStore transactionInvokerStore() {
        return new SagaUndoLogLocalStore();
    }

    /**
     * 当地事务manager
     *
     * @return {@link SagaLocalTxManager}
     */
    @Bean
    public SagaLocalTxManager localTxManager(TxIdGenerater txIdGenerater) {
        return new SagaLocalTxManager(transactionInvokerStore(), txIdGenerater);
    }

    /**
     * 全局事务管理
     *
     * @return {@link SagaGlobalTxManager}
     */
    @Bean
    public SagaGlobalTxManager globalTxManager(LocalTxLock txLock, TxIdGenerater txIdGenerater) {
        return new SagaGlobalTxManager(transactionInvokerStore(), txLock, txIdGenerater);
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
    public Advisor sagaTransactionAnnotationAdvisor(SagaComponentTxExecutorService sagaComponentTxExecutorService) {
        SagaTransactionInterceptor interceptor = new SagaTransactionInterceptor();
        interceptor.setSagaComponentTxExecutorService(sagaComponentTxExecutorService);
        return new SagaTransactionAnnotationAdvisor(interceptor, SagaComponent.class);
    }

    @Bean
    public SagaComponentTxExecutorService sagaComponentTxExecutorService(SagaLocalTxManager sagaLocalTxManager,LocalTxLock txLock) {
        SagaComponentTxExecutorService sagaComponentTxExecutorService = new SagaComponentTxExecutorService();
        sagaComponentTxExecutorService.setLocalTxManager(sagaLocalTxManager);
        sagaComponentTxExecutorService.setTxLock(txLock);
        return sagaComponentTxExecutorService;
    }

    @Bean
    public SagaTxComponentTemplate sagaTxComponentTemplate(SagaComponentTxExecutorService sagaComponentTxExecutorService) {
        return new SagaTxComponentTemplate(sagaComponentTxExecutorService);
    }

    @Bean
    public SagaTxTemplate sagaTxTemplate(SagaGlobalTxManager sagaGlobalTxManager) {
        return new SagaTxTemplate(sagaGlobalTxManager);
    }

    @Bean
    public TxIdGenerater txIdGenerater(RedissonClient redissonClient) {
        return new RedisTxIdGenertater(redissonClient);
    }
}
