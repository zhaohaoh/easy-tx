package com.easy.tx.pojo;


/**
 * 传奇组件事务遗嘱执行人
 *
 * @author hzh
 * @date 2023/08/09
 */
public interface SagaComponentTxExecutor {

    default Object executeAop() throws Throwable {
        return null;
    }

    SagaComponentInfo getSagaComponentInfo();

    default Object execute() {
        return null;
    }
}
