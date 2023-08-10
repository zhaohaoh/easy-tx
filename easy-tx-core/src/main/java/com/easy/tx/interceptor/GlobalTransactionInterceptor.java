package com.easy.tx.interceptor;


import com.easy.tx.pojo.GlobalTransactionalExecutor;
import com.easy.tx.pojo.GlobalTransactionalInfo;
import com.easy.tx.annotation.GlobalTransaction;
import com.easy.tx.context.GlobalTxContext;
import com.easy.tx.exception.TxException;
import com.easy.tx.manager.TxManager;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;
import java.util.Objects;


/**
 * 事务拦截器
 *
 * @author hzh
 * @date 2023/08/01
 */
public class GlobalTransactionInterceptor implements MethodInterceptor {

    private TxManager globalTxManager;

    public void setGlobalTxManager(TxManager globalTxManager) {
        this.globalTxManager = globalTxManager;
    }

    @Override
    public Object invoke(final MethodInvocation methodInvocation) throws Throwable {
        Method method = methodInvocation.getMethod();
        Object thisProxy = methodInvocation.getThis();

        final GlobalTransaction globalTransaction = thisProxy.getClass().getAnnotation(GlobalTransaction.class) != null ?
                thisProxy.getClass().getAnnotation(GlobalTransaction.class) :
                method.getAnnotation(GlobalTransaction.class);

        GlobalTransactionalExecutor transactionalExecutor = new GlobalTransactionalExecutor() {
            @Override
            public Object execute() throws Throwable {
                return methodInvocation.proceed();
            }

            @Override
            public GlobalTransactionalInfo getTransactionInfo() {
                GlobalTransactionalInfo transactionInfo = new GlobalTransactionalInfo();
                transactionInfo.setTimeout(globalTransaction.timeout());
                transactionInfo.setNoRollbackFor(globalTransaction.noRollbackFor());
                transactionInfo.setRollbackFor(globalTransaction.rollbackFor());
                return transactionInfo;
            }
        };
        return doExecute(transactionalExecutor);
    }

    /**
     * 做执行
     *
     * @param transactionalExecutor 事务执行人
     * @return {@link Object}
     * @throws Throwable throwable
     */
    private Object doExecute(GlobalTransactionalExecutor transactionalExecutor) throws Throwable {
        //是否存在全局事务
        if (!StringUtils.isEmpty(GlobalTxContext.getGlobalTxId())) {
            return transactionalExecutor.execute();
        }

        GlobalTransactionalInfo transactionInfo = transactionalExecutor.getTransactionInfo();

        boolean commit = true;
        Object o;
        globalTxManager.startTransaction(System.currentTimeMillis() + transactionInfo.getTimeout() * 1000);
        try {
            o = transactionalExecutor.execute();
        } catch (Exception e) {
            // 指定捕获异常和本框架的异常才回滚
            commit = !isRollback(e, transactionInfo) && !(e instanceof TxException);
            throw e;
        } finally {
            if (commit){
                globalTxManager.commit();
            }else{
                globalTxManager.roback();
            }
        }
        return o;
    }

    private boolean isRollback(Throwable e, GlobalTransactionalInfo transactionInfo) {
        boolean isRollback = true;
        Class<? extends Throwable>[] rollbacks = transactionInfo.getRollbackFor();
        Class<? extends Throwable>[] noRollbackFor = transactionInfo.getNoRollbackFor();
        if (ArrayUtils.isNotEmpty(noRollbackFor)) {
            for (Class<? extends Throwable> noRollBack : noRollbackFor) {
                int depth = getDepth(e.getClass(), noRollBack);
                if (depth >= 0) {
                    return false;
                }
            }
        }
        if (ArrayUtils.isNotEmpty(rollbacks)) {
            for (Class<? extends Throwable> rollback : rollbacks) {
                int depth = getDepth(e.getClass(), rollback);
                if (depth >= 0) {
                    return isRollback;
                }
            }
        }
        return false;
    }

    private int getDepth(Class<?> exceptionClass, Class<? extends Throwable> rollback) {
        if (rollback == Throwable.class || rollback == Exception.class) {
            return 0;
        }
        // If we've gone as far as we can go and haven't found it...
        if (exceptionClass == Throwable.class) {
            return -1;
        }
        if (Objects.equals(exceptionClass, rollback)) {
            return 0;
        }
        return getDepth(exceptionClass.getSuperclass(), rollback);
    }

}
