package com.easy.tx.pojo;

import lombok.Data;

/**
 * 事务注解信息
 *
 * @author hzh
 * @date 2023/08/02
 */
@Data
public class GlobalTransactionalInfo {

    /**
     * 事务超时时间
     */
    private Integer timeout;

    /**
     * 回滚
     */
    private Class<? extends Throwable>[] rollbackFor;

    /**
     * 不回滚
     */
    private Class<? extends Throwable>[] noRollbackFor;

}
