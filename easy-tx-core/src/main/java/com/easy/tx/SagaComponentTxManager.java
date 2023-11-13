package com.easy.tx;

import com.easy.tx.constant.BranchType;
import com.easy.tx.context.GlobalTxContext;
import com.easy.tx.manager.SagaBranchTxManager;
import com.easy.tx.message.BranchTxSession;
import com.easy.tx.pojo.SagaComponentInfo;
import com.easy.tx.pojo.SagaComponentTxExecutor;
import org.apache.commons.lang3.StringUtils;

/**
 * 组件事务执行人服务
 *
 * @author hzh
 * @date 2023/08/09
 */
public class SagaComponentTxManager {
    private SagaBranchTxManager sagaBranchTxManager;

    public void setSagaBranchTxManager(SagaBranchTxManager sagaBranchTxManager) {
        this.sagaBranchTxManager = sagaBranchTxManager;
    }

    /**
     * 做执行
     *
     * @param sagaComponentTxExecutor 执行器
     * @return {@link Object}
     */
    public Object doExecute(SagaComponentTxExecutor sagaComponentTxExecutor) {
        Object result = null;
        String globalTxId = GlobalTxContext.getGlobalTxId();
        Long expireTime = GlobalTxContext.getExpireTime();
        long beginTime = System.currentTimeMillis();
        BranchTxSession branchInfo = new BranchTxSession(globalTxId, beginTime,expireTime, BranchType.SAGA.ordinal());
        SagaComponentInfo sagaComponentInfo = sagaComponentTxExecutor.getSagaComponentInfo();
        Object lockValue = sagaComponentInfo.getLockValue();
        if (lockValue != null) {
            String lockKey = sagaComponentInfo.getLockKey();
            if (StringUtils.isNotBlank(lockKey)) {
                branchInfo.setLockKey(lockKey);
                branchInfo.setLockValue(lockValue);
            }
        }
        String branchTxId = sagaBranchTxManager.startTransaction(branchInfo);
        try {
            result = sagaComponentTxExecutor.execute();
            sagaBranchTxManager.commit(branchTxId, sagaComponentInfo);
        } catch (Throwable e) {
            sagaBranchTxManager.rollback();
            throw e;
        }
        return result;
    }


    /**
     * 做执行aop
     *
     * @param sagaComponentTxExecutor 传奇组件事务遗嘱执行人
     * @return {@link Object}
     * @throws Throwable throwable
     */
    public Object doExecuteAop(SagaComponentTxExecutor sagaComponentTxExecutor) throws Throwable {
        Object result = null;
        String globalTxId = GlobalTxContext.getGlobalTxId();
        Long expireTime = GlobalTxContext.getExpireTime();
        long beginTime = System.currentTimeMillis();
        BranchTxSession branchInfo = new BranchTxSession(globalTxId, beginTime,expireTime, BranchType.SAGA.ordinal());
        SagaComponentInfo sagaComponentInfo = sagaComponentTxExecutor.getSagaComponentInfo();
        Object lockValue = sagaComponentInfo.getLockValue();
        if (lockValue != null) {
            String lockKey = sagaComponentInfo.getLockKey();
            if (StringUtils.isNotBlank(lockKey)) {
                branchInfo.setLockKey(lockKey);
                branchInfo.setLockValue(lockValue);
            }
        }
        String branchTxId = sagaBranchTxManager.startTransaction(branchInfo);
        try {
            result = sagaComponentTxExecutor.executeAop();
            sagaBranchTxManager.commit(branchTxId, sagaComponentInfo);
        } catch (Throwable e) {
            sagaBranchTxManager.rollback();
            throw e;
        }
        return result;
    }
}
