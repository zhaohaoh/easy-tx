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
    public String newTxId() {
        if (idGenerator == null) {
            synchronized (RedisTxIdGenertater.class) {
                if (idGenerator == null) {
                    idGenerator = redissonClient.getIdGenerator("EASY_TX_ID");
                    idGenerator.tryInit(0, 1000);
                }
            }
        }

        long id = idGenerator.nextId();
        if (id >= (Long.MAX_VALUE >> 1)) {
            id = reTryInit(id);
        }
        return "TX" + id;
    }

    /**
     * 重新尝试init
     *
     * @param id id
     * @return long
     */
    private long reTryInit(long id) {
        RLock easyTxIdLock = redissonClient.getLock("EASY_TX_ID_LOCK");
        boolean lock = easyTxIdLock.tryLock();
        if (lock) {
            try {
                idGenerator = redissonClient.getIdGenerator("EASY_TX_ID");
                id = idGenerator.nextId();
                if (id >= (Long.MAX_VALUE >> 1)) {
                    idGenerator.delete();
                    idGenerator = redissonClient.getIdGenerator("EASY_TX_ID");
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
