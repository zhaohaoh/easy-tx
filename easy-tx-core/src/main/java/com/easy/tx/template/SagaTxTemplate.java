package com.easy.tx.template;

import com.easy.tx.manager.SagaGlobalTxManager;

/**
 * SAGA全局事务模板
 *
 * @author hzh
 * @date 2023/08/09
 */
public class SagaTxTemplate {
    private final SagaGlobalTxManager sagaGlobalTxManager;

    public SagaTxTemplate(SagaGlobalTxManager sagaGlobalTxManager) {
        this.sagaGlobalTxManager = sagaGlobalTxManager;
    }

    public String startTransaction() {
        return sagaGlobalTxManager.startTransaction(System.currentTimeMillis() + 60 * 1000);
    }

    /**
     * 开始事务
     *
     * @return {@link String}
     */
    public String startTransaction(Long expireTime) {
        return sagaGlobalTxManager.startTransaction(expireTime);
    }

    /**
     * 提交事务
     */
    public void commit() {
        sagaGlobalTxManager.commit();
    }

    /**
     * 回滚事务
     */
    public void rollback() {
        sagaGlobalTxManager.rollback();
    }

}
