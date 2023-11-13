package com.easy.tx.interceptor;

import com.easy.tx.constant.RecoveryEnum;
import com.easy.tx.pojo.SagaComponentInfo;
import com.easy.tx.pojo.SagaComponentTxExecutor;
import com.easy.tx.SagaComponentTxManager;
import com.easy.tx.annotation.SagaComponent;
import com.easy.tx.context.GlobalTxContext;
import com.easy.tx.exception.TxException;
import com.easy.tx.util.SpElUtil;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import java.lang.reflect.Method;
import java.util.Arrays;


/**
 * 事务拦截器
 *
 * @author hzh
 * @date 2023/08/01
 */
public class SagaTransactionInterceptor implements MethodInterceptor {

    private final Logger log = LoggerFactory.getLogger(SagaTransactionInterceptor.class);
    private static final ParameterNameDiscoverer parameterNameDiscoverer = new DefaultParameterNameDiscoverer();
    private SagaComponentTxManager sagaComponentTxExecutorService;

    public void setSagaComponentTxExecutorService(SagaComponentTxManager sagaComponentTxExecutorService) {
        this.sagaComponentTxExecutorService = sagaComponentTxExecutorService;
    }

    @Override
    public Object invoke(final MethodInvocation methodInvocation) throws Throwable {
        // 全局事务未开启直接返回
        String globalTxId = GlobalTxContext.getGlobalTxId();
        if (StringUtils.isEmpty(globalTxId)) {
            log.warn("Local Transaction Try Begin But globalTxId isEmpty!");
            return methodInvocation.proceed();
        }
        // 事务超时
        Long expireTime = GlobalTxContext.getExpireTime();
        if (expireTime != null && expireTime <= System.currentTimeMillis()) {
            throw new TxException("tx is timeout " + globalTxId);
        }
        Object thisProxy = methodInvocation.getThis();
        Method thisMethod = methodInvocation.getMethod();
        final SagaComponent sagaComponent = thisMethod.getAnnotation(SagaComponent.class);

        // 获取锁定的数据
        Object lockValue = null;
        final Object[] args = methodInvocation.getArguments();
        try {
            if (StringUtils.isNotBlank(sagaComponent.lockFor())) {
                String[] parameterNames = parameterNameDiscoverer.getParameterNames(thisMethod);
                lockValue = SpElUtil.parse(sagaComponent.lockFor(), parameterNames, args);
            } else if (StringUtils.isNotBlank(sagaComponent.lockForMethod())) {
                Method lockMethod = this.getClass().getDeclaredMethod(sagaComponent.lockForMethod(), methodInvocation.getMethod().getParameterTypes());
                lockValue = lockMethod.invoke(thisProxy, args);
            }
        } catch (Exception e) {
            log.error(globalTxId + " get lock value Exception ", e);
        }


        String lockKey = "";
        if (StringUtils.isNotBlank(sagaComponent.lockKey())) {
            lockKey = sagaComponent.lockKey();
        } else if (sagaComponent.lockKeyForClassName().length > 0) {
            lockKey = StringUtils.join(Arrays.stream(sagaComponent.lockKeyForClassName()).map(Class::getName).toArray(), "&&");
        } else {
            lockKey = thisProxy.getClass().getSimpleName() + "." + thisMethod.getName();
        }


//        Method rollbackMethod = thisProxy.getClass().getDeclaredMethod(sagaComponent.rollbackFor(), thisMethod.getParameterTypes());


        Object finalLockValue = lockValue;
        String finalLockKey = lockKey;
        SagaComponentTxExecutor sagaComponentTxExecutor = new SagaComponentTxExecutor() {
            @Override
            public Object executeAop() throws Throwable {
                return methodInvocation.proceed();
            }
            @Override
            public SagaComponentInfo getSagaComponentInfo() {
                SagaComponentInfo sagaCompoentInfo = new SagaComponentInfo();
                sagaCompoentInfo.setTimeout(sagaComponent.timeout());
                sagaCompoentInfo.setLockValue(finalLockValue);
                sagaCompoentInfo.setLockKey(finalLockKey);
                sagaCompoentInfo.setRollbackMethod(sagaComponent.recovery().equals(RecoveryEnum.FORWARD)?thisMethod.getName() :sagaComponent.rollbackFor());
                sagaCompoentInfo.setRollbackProxy(methodInvocation.getThis());
                sagaCompoentInfo.setArgs(args);
                return sagaCompoentInfo;
            }
        };

        return sagaComponentTxExecutorService.doExecuteAop(sagaComponentTxExecutor);
    }

}
