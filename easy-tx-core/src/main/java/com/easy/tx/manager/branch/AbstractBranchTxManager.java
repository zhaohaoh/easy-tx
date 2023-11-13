package com.easy.tx.manager.branch;

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
public abstract class AbstractBranchTxManager implements BranchTxManager {
    private final Logger log = LoggerFactory.getLogger(this.getClass());
    protected UndoLogStore undoLogStore;
    private final TxIdGenerater txIdGenerater;
    protected BranchTxResourceManager branchTxResource;

    public AbstractBranchTxManager(UndoLogStore undoLogStore, TxIdGenerater txIdGenerateStrategy,BranchTxResourceManager branchTxResource) {
        this.undoLogStore = undoLogStore;
        this.txIdGenerater = txIdGenerateStrategy;
        this.branchTxResource = branchTxResource;
    }

    @Override
    public String newTxId() {
        return txIdGenerater.newTxId(TxIdEnum.BRANCH.name());
    }
}
