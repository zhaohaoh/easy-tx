package com.easy.tx.pojo;

import lombok.Data;

import java.lang.reflect.Method;

/**
 * 传奇compoent信息
 *
 * @author hzh
 * @date 2023/08/08
 */
@Data
public class SagaComponentInfo {
    /**
     * 当前代理
     */
    private Object robackProxy;
    /**
     * 超时 暂不使用
     *
     * @return int
     */
    private Integer timeout;

    /**
     * roback方法
     *
     * @return {@link String}
     */
    private String robackMethod;



    /**
     * 锁值
     */
    private Object lockValue;

    /**
     * 锁定键 一般选择业务表名
     */
    private String lockKey;


    private Object[] args;
}
