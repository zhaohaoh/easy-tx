package com.easy.tx.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * 全局事务定义
 *
 * @author hzh
 * @date 2023/08/01
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD,ElementType.TYPE})
public @interface GlobalTransaction {
    /**
     * 超时
     *
     * @return int
     */
    int timeout() default 60;

    Class<? extends Throwable>[] rollbackFor() default {Exception.class};

    Class<? extends Throwable>[] noRollbackFor() default {};
}
