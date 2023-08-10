package com.easy.tx.pojo;


import lombok.Data;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * 事务调用程序
 *
 * @author hzh
 * @date 2023/08/02
 */
@Data
public class GlobalTransactionInvoker {

    /**
     * 代理类
     */
    private Object protyBean;
    /**
     * 参数
     */
    private Object[] args;
    /**
     * 方法
     */
    private Method method;
    /**
     * 参数类型
     */
    private Class<?>[] parameterTypes;

    public GlobalTransactionInvoker(Object protyBean, Object[] args, Method method, Class<?>[] parameterTypes) {
        this.protyBean = protyBean;
        this.args = args;
        this.method = method;
        this.parameterTypes = parameterTypes;
    }

    public void invoke() throws InvocationTargetException, IllegalAccessException {
        method.invoke(protyBean,args);
    }
}
