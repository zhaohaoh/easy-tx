package com.easy.tx.autoconfigure;

import com.easy.tx.lock.LocalTxLock;
import com.easy.tx.lock.RedisTxLock;
import org.redisson.api.RedissonClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 事务锁配置
 *
 * @author hzh
 * @date 2023/08/08
 */
@Configuration
public class TxLockAutoConfiguration {
    /**
     * 事务锁
     *
     * @param redissonClient redisson客户
     * @return {@link LocalTxLock}
     */
    @Bean
    public LocalTxLock txLock(RedissonClient redissonClient) {
        return new RedisTxLock(redissonClient);
    }
}
