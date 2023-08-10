package com.easy.tx.lock;


import com.easy.tx.constant.RedisCacheKeyBuilder;
import com.easy.tx.exception.TxException;
import org.redisson.api.ExpiredObjectListener;
import org.redisson.api.RSet;
import org.redisson.api.RSetCache;
import org.redisson.api.RedissonClient;
import org.redisson.codec.JsonJacksonCodec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * redis事务锁
 *
 * @author hzh
 * @date 2023/08/02
 */
public class RedisTxLock implements LocalTxLock {
    private final Logger log = LoggerFactory.getLogger(LocalTxLock.class);

    private final RedissonClient redissonClient;

    public RedisTxLock(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
    }

    /**
     * 试着锁
     */
    @Override
    public boolean tryLock(String lockKey, String txId, Object obj, Long expireTime) {
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

            // 获取当前锁定事务的id列表  内部元素有过期时间
            RSetCache<Object> lockTxSet = redissonClient.getSetCache(RedisCacheKeyBuilder.buildLockTx(lockKey), JsonJacksonCodec.INSTANCE);

            // 排除当前全局事务id，同一个事务做可重入锁
            Set<Object> lockTxIdSet = new HashSet<>(lockTxSet.readAll());
            lockTxIdSet.remove(txId); //这行代码是可重入锁。注释就不可重入
            //获取是否已经锁定
            if (!CollectionUtils.isEmpty(lockTxIdSet)) {
                for (Object currentTxId : lockTxIdSet) {
                    RSet<Object> lockValueSet = redissonClient.getSet(RedisCacheKeyBuilder.buildLockValue(lockKey, String.valueOf(currentTxId)), JsonJacksonCodec.INSTANCE);
                    Set<Object> set = lockValueSet.readAll();
                    if (!CollectionUtils.isEmpty(set)) {
                        boolean disjoint = Collections.disjoint(lockValues, set);
                        if (!disjoint) {
                            //数据重复说明,说明已经锁定
                            success = false;
                            break;
                        }
                    }
                }
            }

            //成功获取到锁
            if (success) {
                if (expireTime <= System.currentTimeMillis()) {
                    throw new TxException("tx is timeout " + txId);
                }
                setLockTx(txId, expireTime, lockTxSet);
                setLockVale(lockKey, txId, expireTime, lockValues, lockTxSet);
            }
        } catch (Exception e) {
            log.error("TxLock tryLock Exception", e);
            return success;
        }
        return success;
    }

    /**
     * 设置锁事务
     *
     * @param txId        事务id
     * @param expireTime  到期时间
     * @param lockTxCache 锁事务缓存
     */
    private void setLockTx(String txId, Long expireTime, RSetCache<Object> lockTxCache) {
        long timeout = expireTime - System.currentTimeMillis();
        lockTxCache.add(txId, timeout, TimeUnit.MILLISECONDS);
    }

    /**
     * 设置锁的值
     *
     * @param lockKey     锁定键
     * @param txId        事务id
     * @param expireTime  到期时间
     * @param lockValues  锁定值
     * @param lockTxCache 锁事务缓存
     */
    private void setLockVale(String lockKey, String txId, Long expireTime, Set<Object> lockValues, RSetCache<Object> lockTxCache) {
        RSet<Object> txSet = redissonClient.getSet(RedisCacheKeyBuilder.buildLockValue(lockKey, txId), JsonJacksonCodec.INSTANCE);
        txSet.addAll(lockValues);
        txSet.expire(Instant.ofEpochMilli(expireTime));
        //监听删除
        txSet.addListener((ExpiredObjectListener) name -> {
            lockTxCache.remove(txId);
        });
    }

    /**
     * 释放锁
     *
     * @param lockKey 锁定键
     * @param txId    事务id
     * @return boolean
     */
    @Override
    public boolean unLock(String lockKey, String txId) {
        try {
            //先删除锁定的数据。这样就算有txId没有被删除，至少数据不会被锁定
            RSet<Object> lockValueSet = redissonClient.getSet(RedisCacheKeyBuilder.buildLockValue(lockKey, txId), JsonJacksonCodec.INSTANCE);
            lockValueSet.delete();
            //前面有监听删除这里再删一次，监听可能会延时
            RSetCache<Object> lockTxCache = redissonClient.getSetCache(RedisCacheKeyBuilder.buildLockTx(lockKey), JsonJacksonCodec.INSTANCE);
            lockTxCache.remove(txId);
        } catch (Exception e) {
            log.error("TxLock unLock Exception", e);
            return false;
        }
        return true;
    }
}
