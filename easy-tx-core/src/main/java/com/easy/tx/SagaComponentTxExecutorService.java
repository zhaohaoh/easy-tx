package com.easy.tx;

import com.easy.tx.context.GlobalTxContext;
import com.easy.tx.context.TxLockContext;
import com.easy.tx.exception.TxException;
import com.easy.tx.lock.LocalTxLock;
import com.easy.tx.manager.SagaLocalTxManager;
import com.easy.tx.pojo.SagaComponentInfo;
import com.easy.tx.pojo.SagaComponentTxExecutor;
import com.easy.tx.store.SagaUndoLog;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * 组件事务执行人服务
 *
 * @author hzh
 * @date 2023/08/09
 */
public class SagaComponentTxExecutorService {
    private SagaLocalTxManager localTxManager;
    private LocalTxLock txLock;

    public void setLocalTxManager(SagaLocalTxManager localTxManager) {
        this.localTxManager = localTxManager;
    }

    public void setTxLock(LocalTxLock txLock) {
        this.txLock = txLock;
    }

    /**
     * 做执行
     *
     * @param sagaComponentTxExecutor 执行器
     * @return {@link Object}
     */
    public Object doExecute(SagaComponentTxExecutor sagaComponentTxExecutor) {
        SagaUndoLog undoLog = getSagaUndoLog(sagaComponentTxExecutor);

        boolean commit = true;
        Object result = null;
        String txId = localTxManager.startTransaction(GlobalTxContext.getExpireTime());
        try {
            result = sagaComponentTxExecutor.execute();
        } catch (Exception e) {
            commit = false;
            throw e;
        } finally {
            if (commit) {
                localTxManager.addUndoLog(txId, undoLog);
            }
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
        SagaUndoLog undoLog = getSagaUndoLog(sagaComponentTxExecutor);
        boolean commit = true;
        Object result = null;
        String txId = localTxManager.startTransaction(GlobalTxContext.getExpireTime());
        try {
            result = sagaComponentTxExecutor.executeAop();
        } catch (Exception e) {
            commit = false;
            throw e;
        } finally {
            if (commit) {
                localTxManager.addUndoLog(txId, undoLog);
            }
        }
        return result;
    }

    private SagaUndoLog getSagaUndoLog(SagaComponentTxExecutor sagaComponentTxExecutor) {
        String globalTxId = GlobalTxContext.getGlobalTxId();
        Long expireTime = GlobalTxContext.getExpireTime();

        SagaComponentInfo sagaComponentInfo = sagaComponentTxExecutor.getSagaComponentInfo();
        Object thisProxy = sagaComponentInfo.getRobackProxy();

        Object[] args = sagaComponentInfo.getArgs();
        String lockKey = sagaComponentInfo.getLockKey();
        String robackMethod = sagaComponentInfo.getRobackMethod();
        Method[] methods = thisProxy.getClass().getMethods();
        String[] parameterTypesStr = new String[0];
        //只根据名字找方法 只找public
        for (Method method : methods) {
            boolean equals = method.getName().equals(robackMethod);
            if (equals) {
                Class<?>[] parameterTypes = method.getParameterTypes();
                parameterTypesStr = Arrays.stream(parameterTypes).map(Class::getName).toArray(String[]::new);
                if (parameterTypesStr.length >= args.length) {
                    break;
                }
            }
        }

        Object lockValue = sagaComponentInfo.getLockValue();
        if (lockValue != null) {
            if (StringUtils.isNotBlank(lockKey)) {
                boolean tryLock = txLock.tryLock(lockKey, globalTxId, lockValue, expireTime);
                if (!tryLock) {
                    throw new TxException("saga tx is lock  key=" + lockKey);
                }
                TxLockContext.add(globalTxId, lockKey);
            }
        }

        //添加事务

        SagaUndoLog txLog = new SagaUndoLog(sagaComponentInfo.getRobackProxy().getClass().getName(), sagaComponentInfo.getRobackMethod(), sagaComponentInfo.getArgs(), parameterTypesStr);
        return txLog;
    }
}
