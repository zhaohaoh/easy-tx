package com.easy.tx.autoconfigure;

import com.easy.tx.lock.BranchTxLock;
import com.easy.tx.lock.RedisBranchTxLock;
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
     * @return {@link BranchTxLock}
     */
    @Bean
    public BranchTxLock txLock(RedissonClient redissonClient) {
        return new RedisBranchTxLock(redissonClient);
    }
}
