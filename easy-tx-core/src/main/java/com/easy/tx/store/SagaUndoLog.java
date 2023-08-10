package com.easy.tx.store;

import lombok.Data;

/**
 * 事务日志
 *
 * @author hzh
 * @date 2023/08/01
 */
@Data
public class SagaUndoLog {
    private String className;
    private String methodName;
    private Object[] args;
    private String[] parameterTypes;

    public SagaUndoLog(String className, String methodName, Object[] args, String[] parameterTypes) {
        this.className = className;
        this.methodName = methodName;
        this.args = args;
        this.parameterTypes = parameterTypes;
    }
}
