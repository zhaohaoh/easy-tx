package com.easy.tx.manager;

import com.easy.tx.context.GlobalTxContext;
import com.easy.tx.context.LocalTxContext;
import com.easy.tx.exception.TxException;
import com.easy.tx.store.SagaUndoLog;
import com.easy.tx.store.SagaUndoLogStore;
import com.easy.tx.strategy.TxIdGenerater;
import lombok.extern.slf4j.Slf4j;

/**
 * 本地事务经理
 *
 * @author hzh
 * @date 2023/08/01
 */
@Slf4j
public class SagaLocalTxManager extends AbstractTxManager {


    public SagaLocalTxManager(SagaUndoLogStore transactionInvokerStore, TxIdGenerater txIdGenerateStrategy) {
        super(transactionInvokerStore, txIdGenerateStrategy);
    }

    /**
     * 手动开启事务
     */
    @Override
    public String startTransaction(Long expireTime) {
        String globalTxId = GlobalTxContext.getGlobalTxId();
        if (globalTxId == null) {
            throw new TxException("Local Transaction Try Begin But globalTxId isEmpty!");
        }
        String localTxId = newTxId();
        LocalTxContext.add(globalTxId, localTxId);
        log.debug("start localTx [{}]", localTxId);
        return localTxId;
    }


    @Override
    public void commit() {
    }

    @Override
    public void roback() {
    }

    public void addUndoLog(String localTxId, SagaUndoLog txLog) {
        sagaUndoLogStore.addUndoLog(localTxId, txLog);
    }
}
