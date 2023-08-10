package com.easy.tx.manager;

import com.easy.tx.store.SagaUndoLogStore;
import com.easy.tx.strategy.TxIdGenerater;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 抽象事务经理
 *
 * @author hzh
 * @date 2023/08/02
 */
public abstract class AbstractTxManager implements TxManager {
    private final Logger log = LoggerFactory.getLogger(this.getClass());
    protected SagaUndoLogStore sagaUndoLogStore;
    private final TxIdGenerater txIdGenerateStrategy;

    public AbstractTxManager(SagaUndoLogStore transactionInvokerStore, TxIdGenerater txIdGenerateStrategy) {
        this.sagaUndoLogStore = transactionInvokerStore;
        this.txIdGenerateStrategy = txIdGenerateStrategy;
    }

    @Override
    public String newTxId() {
        return txIdGenerateStrategy.newTxId();
    }
}
