package com.easy.tx.annotation;

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
    int timeout() default 60;

    /**
     * roback方法
     *
     * @return {@link String}
     */
    String robackFor();

    /**
     * 根据本类方法获取的结果锁定
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
}
