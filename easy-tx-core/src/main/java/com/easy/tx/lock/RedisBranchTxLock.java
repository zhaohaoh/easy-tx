package com.easy.tx.lock;

import com.easy.tx.constant.RedisCacheKeyBuilder;
import com.easy.tx.exception.TxException;
import org.redisson.api.*;
import org.redisson.codec.JsonJacksonCodec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;


/**
 * redis事务锁
 *
 * @author hzh
 * @date 2023/08/02
 */
public class RedisBranchTxLock implements BranchTxLock {
    private final Logger log = LoggerFactory.getLogger(BranchTxLock.class);

    private final RedissonClient redissonClient;

    public RedisBranchTxLock(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
    }

    /**
     * 试着锁
     */
    @Override
    public boolean tryLock(String lockKey, String globalTxId, Object obj, Long expireTime) {
        boolean success = true;
        if (obj == null) {
            return success;
        }

        try {
            Set<Object> lockValues;
            if (!(obj instanceof Collection) && !(obj.getClass().isArray())) {
                lockValues = Collections.singleton(obj);
            } else {
                if (obj.getClass().isArray()) {
                    lockValues = Arrays.stream((Object[]) obj).collect(Collectors.toSet());
                } else {
                    lockValues = new HashSet<>((Collection<?>) obj);
                }

                // 用redis做的话数据量太大就不上锁
                if (lockValues.size() >= 10000) {
                    return true;
                }
            }

            //获取是否已经锁定
            RMapCache<Object, Object> mapCache = redissonClient.getMapCache(RedisCacheKeyBuilder.buildLockValue(lockKey), JsonJacksonCodec.INSTANCE);
            Set<Map.Entry<Object, Object>> entries = mapCache.readAllEntrySet();
            for (Map.Entry<Object, Object> entry : entries) {
                Object redisTxId = entry.getKey();
                if (redisTxId != null && redisTxId.equals(globalTxId)) {
                    continue;
                }
                Collection<?> value = (Collection<?>) entry.getValue();
                if (!CollectionUtils.isEmpty(value)) {
                    boolean disjoint = Collections.disjoint(lockValues, value);
                    if (!disjoint) {
                        //数据重复说明,说明已经锁定
                        success = false;
                        break;
                    }
                }
            }

            //成功获取到锁
            if (success) {
                if (expireTime <= System.currentTimeMillis()) {
                    throw new TxException("Easy-Tx is timeout " + globalTxId);
                }
                setLockValue(lockKey, globalTxId, expireTime, lockValues);
                RSet<Object> set = redissonClient.getSet(RedisCacheKeyBuilder.buildLockTx(globalTxId));
                set.add(lockKey);
                long timeout = expireTime - System.currentTimeMillis();
                set.expire(Duration.ofMillis(timeout > 0 ? timeout : 0));
            }
        } catch (Exception e) {
            log.error("TxLock tryLock Exception", e);
            return success;
        }
        return success;
    }

    /**
     * 设置锁的值
     *
     * @param lockKey    锁定键
     * @param globalTxId 事务id
     * @param expireTime 到期时间
     * @param lockValues 锁定值
     */
    private void setLockValue(String lockKey, String globalTxId, Long expireTime, Set<Object> lockValues) {
        RMapCache<Object, Object> mapCache = redissonClient.getMapCache(RedisCacheKeyBuilder.buildLockValue(lockKey), JsonJacksonCodec.INSTANCE);
        mapCache.put(globalTxId, lockValues);
        mapCache.expire(Instant.ofEpochMilli(expireTime));
    }

    /**
     * 释放锁
     *
     * @param globalTxId 事务id
     * @return boolean
     */
    @Override
    public boolean unLock(String globalTxId) {
        try {
            RSet<Object> set = redissonClient.getSet(RedisCacheKeyBuilder.buildLockTx(globalTxId));
            Set<Object> all = set.readAll();
            if (all != null) {
                for (Object lockKey : all) {
                    //先删除锁定的数据。这样就算有txId没有被删除，至少数据不会被锁定
                    RMapCache<Object, Object> mapCache = redissonClient.getMapCache(RedisCacheKeyBuilder.buildLockValue(lockKey.toString()), JsonJacksonCodec.INSTANCE);
                    mapCache.remove(globalTxId);
                    set.remove(lockKey);
                }
            }

        } catch (Exception e) {
            log.error("TxLock unLock Exception", e);
            return false;
        }
        return true;
    }
}
