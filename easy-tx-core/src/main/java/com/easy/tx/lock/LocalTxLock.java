package com.easy.tx.lock;

/**
 * 本地事务锁
 *
 * @author hzh
 * @date 2023/08/02
 */
public interface LocalTxLock {

    /**
     * 试着锁
     *
     * @return boolean
     */
    boolean tryLock(String lockKey,String globalTxId,Object obj,Long expireTime);

    /**
     * 释放锁
     *
     * @return boolean
     */
    boolean unLock(String lockKey,String globalTxId);


}
