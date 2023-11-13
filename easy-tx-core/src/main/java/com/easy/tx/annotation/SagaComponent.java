package com.easy.tx.annotation;

import com.easy.tx.constant.RecoveryEnum;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 传奇组件
 *
 * @author hzh
 * @date 2023/08/01
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface SagaComponent {

    /**
     * 超时 暂不使用
     *
     * @return int
     */
    long timeout() default 60;

    /**
     * rollback方法 只支持本类方法    搭配  RecoveryEnum.ROLLBACK
     *
     * @return {@link String}
     */
    String rollbackFor() default "";

    /**
     * 根据本类方法获取的结果锁定 支持el表达式 可以跨类调用
     *
     * @return {@link String}
     */
    String lockForMethod() default "";

    /**
     * 根据指定的value锁定  支持el表达式
     *
     * @return {@link String}
     */
    String lockFor() default "";

    /**
     * 锁定键 一般选择业务表名
     */
    String lockKey() default "";

    /**
     * 锁定的key 根据class获取
     */
    Class<?>[] lockKeyForClassName() default {};

    /**
     * 恢复方式
     *
     * @return {@link RecoveryEnum}
     */
    RecoveryEnum recovery() default  RecoveryEnum.ROLLBACK;
}
