package com.easy.tx.template;

import com.easy.tx.pojo.SagaComponentInfo;
import com.easy.tx.pojo.SagaComponentTxExecutor;
import com.easy.tx.SagaComponentTxExecutorService;
import com.easy.tx.pojo.TxCallback;
import org.apache.commons.lang3.ArrayUtils;

/**
 * SAGA事务组件模板
 *
 * @author hzh
 * @date 2023/08/09
 */
public class SagaTxComponentTemplate {
    private final SagaComponentTxExecutorService sagaComponentTxExecutorService;

    public SagaTxComponentTemplate(SagaComponentTxExecutorService sagaComponentTxExecutorService) {
        this.sagaComponentTxExecutorService = sagaComponentTxExecutorService;
    }

    public void startSagaTx(String lockKey, Object lockValue, Object robackProxy, String robackMethod, Object[] args, TxCallback<?> runnable) {

        SagaComponentTxExecutor sagaComponentTxExecutor = new SagaComponentTxExecutor() {
            @Override
            public Object execute() {
                return runnable.doInTransaction();
            }

            @Override
            public SagaComponentInfo getSagaComponentInfo() {
                SagaComponentInfo sagaComponentInfo = new SagaComponentInfo();
                sagaComponentInfo.setRobackMethod(robackMethod);
                sagaComponentInfo.setLockKey(lockKey);
                sagaComponentInfo.setLockValue(lockValue);
                sagaComponentInfo.setRobackProxy(robackProxy);
                sagaComponentInfo.setArgs(args);
                return sagaComponentInfo;
            }
        };
        sagaComponentTxExecutorService.doExecute(sagaComponentTxExecutor);
    }

    public void startSagaTx(String lockKey, Object lockValue, Object robackProxy, String robackMethod, Object args, TxCallback<?> runnable) {

        SagaComponentTxExecutor sagaComponentTxExecutor = new SagaComponentTxExecutor() {
            @Override
            public Object execute() {
                return runnable.doInTransaction();
            }

            @Override
            public SagaComponentInfo getSagaComponentInfo() {
                SagaComponentInfo sagaComponentInfo = new SagaComponentInfo();
                sagaComponentInfo.setRobackMethod(robackMethod);
                sagaComponentInfo.setLockKey(lockKey);
                sagaComponentInfo.setLockValue(lockValue);
                sagaComponentInfo.setRobackProxy(robackProxy);
                sagaComponentInfo.setArgs(ArrayUtils.toArray(args));
                return sagaComponentInfo;
            }
        };
        sagaComponentTxExecutorService.doExecute(sagaComponentTxExecutor);
    }

    public void startSagaTx(Object robackProxy, String robackMethod, Object args, TxCallback<?> runnable) {

        SagaComponentTxExecutor sagaComponentTxExecutor = new SagaComponentTxExecutor() {
            @Override
            public Object execute() {
                return runnable.doInTransaction();
            }

            @Override
            public SagaComponentInfo getSagaComponentInfo() {
                SagaComponentInfo sagaComponentInfo = new SagaComponentInfo();
                sagaComponentInfo.setRobackMethod(robackMethod);
                sagaComponentInfo.setRobackProxy(robackProxy);
                sagaComponentInfo.setArgs(ArrayUtils.toArray(args));
                return sagaComponentInfo;
            }
        };
        sagaComponentTxExecutorService.doExecute(sagaComponentTxExecutor);
    }

}
