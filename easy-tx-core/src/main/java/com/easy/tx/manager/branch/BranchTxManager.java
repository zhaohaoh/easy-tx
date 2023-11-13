package com.easy.tx.manager.branch;

import com.easy.tx.message.BranchTxSession;
import com.easy.tx.pojo.SagaComponentInfo;
import com.easy.tx.store.undo.UndoLog;

/**
 * 事务经理
 *
 * @author hzh
 * @date 2023/08/02
 */
public interface BranchTxManager {

    /**
     * 开始事务
     *
     * @return {@link String}
     */
    String startTransaction(BranchTxSession branchInfo);

    /**
     * 提交事务
     */
    void commit(String branchTxId,SagaComponentInfo sagaComponentInfo );

    /**
     * 回滚事务
     */
    void rollback();

    /**
     * 新事务id
     *
     * @return {@link String}
     */
    String newTxId();

}
