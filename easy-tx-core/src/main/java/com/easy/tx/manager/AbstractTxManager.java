package com.easy.tx.manager;

import com.easy.tx.constant.TxIdEnum;
import com.easy.tx.store.undo.UndoLogStore;
import com.easy.tx.strategy.TxIdGenerater;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 抽象事务经理
 *
 * @author hzh
 * @date 2023/08/02
 */
public abstract class AbstractTxManager implements GlobalTxManager {
    private final Logger log = LoggerFactory.getLogger(this.getClass());
    protected UndoLogStore undoLogStore;
    private final TxIdGenerater txIdGenerater;

    public AbstractTxManager(UndoLogStore transactionInvokerStore, TxIdGenerater txIdGenerateStrategy) {
        this.undoLogStore = transactionInvokerStore;
        this.txIdGenerater = txIdGenerateStrategy;
    }

    @Override
    public String newTxId() {
        return txIdGenerater.newTxId(TxIdEnum.GLOBAL.name());
    }
}
