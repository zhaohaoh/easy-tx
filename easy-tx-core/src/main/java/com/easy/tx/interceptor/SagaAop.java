package com.easy.tx.interceptor;//package com.framework.shop.admin.tcc.interceptor;
//
//import com.framework.shop.admin.tcc.annotation.SagaComponent;
//import com.framework.shop.admin.tcc.context.GlobalTxContext;
//import com.framework.shop.admin.tcc.manager.LocalTxManager;
//import com.framework.shop.admin.tcc.store.TxLog;
//import lombok.extern.slf4j.Slf4j;
//import org.aspectj.lang.ProceedingJoinPoint;
//import org.aspectj.lang.Signature;
//import org.aspectj.lang.annotation.Around;
//import org.aspectj.lang.annotation.Aspect;
//import org.aspectj.lang.reflect.MethodSignature;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.core.annotation.Order;
//import org.springframework.stereotype.Component;
//import org.springframework.util.StringUtils;
//
//import java.lang.reflect.Method;
//import java.util.Arrays;
//
//
//@Slf4j
//@Aspect
//@Component
////必须比事务大
//@Order(0)
//public class SagaAop {
//    @Autowired
//    private LocalTxManager localTxManager;
//
//    @Around(value = "@annotation(sagaComponent)") //around 与 下面参数名around对应
//    public Object invoke(ProceedingJoinPoint point, SagaComponent sagaComponent) throws Throwable {
//        Object aThis = point.getThis();
//        Object[] args = point.getArgs();
//        Signature sig = point.getSignature();
//        MethodSignature ms = (MethodSignature) sig;
//        Class[] parameterTypes = ms.getParameterTypes();
//        Method method = aThis.getClass().getDeclaredMethod(sagaComponent.rollbackMethod(), parameterTypes);
//
//        //添加事务
//        String[] parameterTypesStr = null;
//        if (parameterTypes != null) {
//            parameterTypesStr = Arrays.stream(parameterTypes).map(Class::getName).toArray(String[]::new);
//        }
//
//        TxLog txLog = new TxLog(aThis.getClass().getName(), method.getName(), args, parameterTypesStr);
//        boolean commit = true;
//        Object result = null;
//
//        String globalTxId = GlobalTxContext.getGlobalTxId();
//        if (StringUtils.isEmpty(globalTxId)) {
//            log.warn("Local Transaction Try Begin But globalTxId isEmpty!");
//            return point.proceed();
//        }
//
//        String txId = localTxManager.startLocalTransaction(globalTxId);
//        try {
//            result = point.proceed();
//        } catch (Exception e) {
//            commit = false;
//            throw e;
//        } finally {
//            localTxManager.endLocalTransaction(commit, txId, txLog);
//        }
//        return result;
//    }
//
//}
