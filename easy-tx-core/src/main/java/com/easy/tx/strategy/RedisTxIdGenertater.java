package com.easy.tx.strategy;

import org.redisson.api.RIdGenerator;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;

/**
 * redis idgenertater
 *
 * @author hzh
 * @date 2023/08/10
 */
public class RedisTxIdGenertater implements TxIdGenerater {
    private final RedissonClient redissonClient;
    private volatile RIdGenerator idGenerator;

    public RedisTxIdGenertater(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
    }

    @Override
    public String newTxId(String type) {
        if (idGenerator == null) {
            synchronized (RedisTxIdGenertater.class) {
                if (idGenerator == null) {
                    idGenerator = redissonClient.getIdGenerator(type);
                    idGenerator.tryInit(0, 1000);
                }
            }
        }

        long id = idGenerator.nextId();
        if (id >= (Long.MAX_VALUE >> 1)) {
            id = reTryInit(type,id);
        }
        return type + "_" + id;
    }

    /**
     * 重新尝试init
     *
     * @param id id
     * @return long
     */
    private long reTryInit(String type,long id) {
        RLock easyTxIdLock = redissonClient.getLock("EASY_TX_ID_LOCK");
        boolean lock = easyTxIdLock.tryLock();
        if (lock) {
            try {
                idGenerator = redissonClient.getIdGenerator(type);
                id = idGenerator.nextId();
                if (id >= (Long.MAX_VALUE >> 1)) {
                    idGenerator.delete();
                    idGenerator = redissonClient.getIdGenerator(type);
                    idGenerator.tryInit(0, 1000);
                    id = idGenerator.nextId();
                }
            } finally {
                easyTxIdLock.unlock();
            }
        }
        return id;
    }
}
