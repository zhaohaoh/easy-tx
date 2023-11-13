package com.easy.tx.manager.global;

import com.easy.tx.message.BranchTxSession;
import com.easy.tx.message.GlobalTxSession;

/**
 * 分支事务资源
 *
 * @author hzh
 * @date 2023/08/25
 */
public interface GlobalTxResourceManager {
    /**
     * 提交事务
     */
    void commit(GlobalTxSession txInfo);

    /**
     * 回滚事务
     */
    void rollback(GlobalTxSession txInfo);

    /**
     * 分支登记
     *
     * @param branchInfo
     * @return boolean
     */
    boolean register(GlobalTxSession branchInfo);
}
