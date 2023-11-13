package com.easy.tx.tc.tx;

import com.easy.tx.constant.TxStatus;
import com.easy.tx.message.BranchTxSession;
import com.easy.tx.message.GlobalTxSession;

import java.util.List;

public interface StoreManager {
    /**
     * 保存分支
     *
     */
    boolean addBranch(BranchTxSession branchInfo);
    /**
     * getCommitGlobalTx
     *
     */
    List<GlobalTxSession> getGlobalTxByStatus(TxStatus txStatus);

    /**
     * 获取所有分支事务id
     *
     */
    List<String> getAllBranchIds(GlobalTxSession globalTxSession);

    /**
     * 获取分支事务
     *
     */
    BranchTxSession getBranch(String globalTxId,String branchTxId);

    /**
     * 删除分支
     *
     */
    boolean removeBranch(BranchTxSession branchInfo);

    /**
     * 删除分支
     *
     */
    boolean removeBranch(GlobalTxSession globalTxSession);

    /**
     * 保存全局
     *
     */
    boolean putGlobal(GlobalTxSession globalTxSession);

    /**
     * 删除全局
     *
     */
    boolean removeGlobal(GlobalTxSession globalTxSession);

    /**
     * 获取全局事务
     */
    GlobalTxSession getByGlobalId(String globalTxId);

    /**
     * 更新全局状态
     *
     * @param globalTxId 全局事务id
     * @param name       名称
     */
    void updateGlobalStatus(String globalTxId, String name);
    
    /**
     * 更新全局状态
     *
     * @param globalTxId 全局事务id
     * @param name       名称
     */
    void updateGlobalStatusAndRetryCount(String globalTxId, String name);

    /**
     * 更新分支状态
     *
     * @param branchTxId 事务id
     * @param name       名称
     */
    void updateBranchStatus(BranchTxSession branchTx, String name);
}
