package com.easy.tx.manager;

import com.easy.tx.pojo.GlobalTransactionInvoker;
import com.easy.tx.context.GlobalTxContext;
import com.easy.tx.context.LocalTxContext;
import com.easy.tx.context.TxLockContext;
import com.easy.tx.lock.LocalTxLock;
import com.easy.tx.store.SagaUndoLog;
import com.easy.tx.store.SagaUndoLogStore;
import com.easy.tx.strategy.TxIdGenerater;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;


import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

/**
 * saga全局事务经理
 *
 * @author hzh
 * @date 2023/08/10
 */
@Slf4j
public class SagaGlobalTxManager extends AbstractTxManager implements ApplicationContextAware {

    private ApplicationContext applicationContext;
    private final LocalTxLock txLock;

    public SagaGlobalTxManager(SagaUndoLogStore sagaUndoLogStore, LocalTxLock txLock, TxIdGenerater txIdGenerateStrategy) {
        super(sagaUndoLogStore, txIdGenerateStrategy);
        this.txLock = txLock;
    }

    /**
     * 手动开启事务
     */
    @Override
    public String startTransaction(Long expireTime) {
        String globalTxId = GlobalTxContext.getGlobalTxId();
        if (globalTxId == null) {
            globalTxId = newTxId();
            GlobalTxContext.bind(globalTxId, expireTime);
        }
        return globalTxId;
    }

    @Override
    public void commit() {
        String globalTxId = GlobalTxContext.getGlobalTxId();
        try {
            List<String> keys = TxLockContext.get();
            keys.forEach(k -> txLock.unLock(k, globalTxId));
            LocalTxContext.get().forEach(sagaUndoLogStore::removeUndoLog);
        } catch (Exception e) {
            log.error("Saga Global Transaction finally Exception", e);
        } finally {
            TxLockContext.clear();
            GlobalTxContext.remove();
            LocalTxContext.clear();
        }
    }

    @Override
    public void roback() {
        String globalTxId = GlobalTxContext.getGlobalTxId();
        try {
            LinkedList<String> txIds = LocalTxContext.get();
            log.info("Saga Global Transaction Roback Begin...{} localTxIds.{}", globalTxId, txIds);
            //回滚
            String localTxId = null;
            for (int i = txIds.size() - 1; i >= 0; i--) {
                try {
                    localTxId = txIds.get(i);
                    SagaUndoLog undoLog = sagaUndoLogStore.getUndoLog(localTxId);
                    log.info("Saga Local Transaction Roback Begin...{} ... txLog:{}", localTxId, undoLog);
                    if (undoLog == null) {
                        continue;
                    }

                    Class<?> aClass = Class.forName(undoLog.getClassName());
                    Object bean = applicationContext.getBean(aClass);
                    String[] parameterTypesArray = undoLog.getParameterTypes();
                    Class[] parameterTypes = Arrays.stream(parameterTypesArray).map(a -> {
                        try {
                            return Class.forName(a);
                        } catch (ClassNotFoundException e) {
                            log.error("Saga Global tx ClassNotFoundException", e);
                        }
                        return null;
                    }).filter(Objects::nonNull).toArray(Class[]::new);
                    if (ArrayUtils.isEmpty(parameterTypes)) {
                        continue;
                    }
                    Method method = bean.getClass().getMethod(undoLog.getMethodName(), parameterTypes);
                    Object[] args = undoLog.getArgs();
                    GlobalTransactionInvoker transactionInvoker = new GlobalTransactionInvoker(bean, args, method, parameterTypes);
                    transactionInvoker.invoke();
                    log.info("Saga Local Transaction Roback End...{} ... txLog:{}", localTxId, undoLog);
                } catch (Exception e) {
                    log.error("Saga Global Roback localTxId=" + localTxId, e);
                }
            }
            log.info("Saga Global Transaction Roback End...{} ...localTxIds{}", globalTxId, txIds);
        } finally {
            try {
                List<String> keys = TxLockContext.get();
                keys.forEach(k -> txLock.unLock(k, globalTxId));
                LocalTxContext.get().forEach(sagaUndoLogStore::removeUndoLog);
            } catch (Exception e) {
                log.error("Saga Global Transaction finally Exception", e);
            } finally {
                TxLockContext.clear();
                GlobalTxContext.remove();
                LocalTxContext.clear();
            }
        }
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
