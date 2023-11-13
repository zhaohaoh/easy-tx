package com.easy.tx.autoconfigure;

import com.easy.tx.SagaComponentTxManager;
import com.easy.tx.manager.SagaBranchTxManager;
import com.easy.tx.manager.SagaGlobalTxManager;
import com.easy.tx.manager.branch.BranchTxResourceManager;
import com.easy.tx.manager.global.GlobalTxResourceManager;
import com.easy.tx.store.undo.UndoLogStore;
import com.easy.tx.strategy.TxIdGenerater;
import com.easy.tx.template.SagaTxComponentTemplate;
import com.easy.tx.template.SagaTxTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


/**
 * 传奇自动配置
 *
 * @author hzh
 * @date 2023/10/21
 */
@Configuration
public class SagaAutoConfiguration {

    @Autowired
    private UndoLogStore undoLogStore;


    /**
     * saga事务分支管理
     *
     * @return {@link SagaBranchTxManager}
     */
    @Bean
    public SagaBranchTxManager sagaBranchTxManager(TxIdGenerater txIdGenerater, BranchTxResourceManager branchTxResourceManager) {
        return new SagaBranchTxManager(undoLogStore, txIdGenerater, branchTxResourceManager);
    }


    /**
     * 全局事务管理
     *
     * @return {@link SagaGlobalTxManager}
     */
    @Bean
    public SagaGlobalTxManager globalTxManager(GlobalTxResourceManager globalTxResourceManager, TxIdGenerater txIdGenerater) {
        return new SagaGlobalTxManager(undoLogStore, globalTxResourceManager, txIdGenerater);
    }

    /**
     * 传奇组件事务管理者
     *
     */
    @Bean
    public SagaComponentTxManager sagaComponentTxManager(SagaBranchTxManager sagaBranchTxManager) {
        SagaComponentTxManager sagaComponentTxManager = new SagaComponentTxManager();
        sagaComponentTxManager.setSagaBranchTxManager(sagaBranchTxManager);
        return sagaComponentTxManager;
    }
    /**
     * 传奇组件事务操作模板
     *
     */
    @Bean
    public SagaTxComponentTemplate sagaTxComponentTemplate(SagaComponentTxManager sagaComponentTxExecutorService) {
        return new SagaTxComponentTemplate(sagaComponentTxExecutorService);
    }
    /**
     * 传奇事务操作模板
     *
     */
    @Bean
    public SagaTxTemplate sagaTxTemplate(SagaGlobalTxManager sagaGlobalTxManager) {
        return new SagaTxTemplate(sagaGlobalTxManager);
    }

}
