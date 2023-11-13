package com.easy.tx.manager.branch;

import com.easy.tx.message.BranchTxSession;
import com.easy.tx.message.GlobalTxSession;

/**
 * 分支事务资源
 *
 * @author hzh
 * @date 2023/08/25
 */
public interface BranchTxResourceManager {
    /**
     * 提交事务
     */
    void commit(BranchTxSession txInfo);

    /**
     * 回滚事务
     */
    void rollback(BranchTxSession txInfo);

    /**
     * 分支登记
     *
     * @param branchInfo
     * @return boolean
     */
    boolean branchRegister(BranchTxSession branchInfo);


}
