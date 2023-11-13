package com.easy.tx.tc.tx;

import com.easy.tx.constant.RedisCacheKeyBuilder;
import com.easy.tx.constant.TxStatus;
import com.easy.tx.exception.TxTimeoutException;
import com.easy.tx.message.BranchTxSession;
import com.easy.tx.message.GlobalTxSession;
import com.easy.tx.tc.properties.TcGlobalConfig;
import com.easy.tx.tc.properties.TcGlobalConfigCache;
import com.easy.tx.util.BeanUtils;
import org.redisson.api.*;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 事务存储管理 存储时间先默认24个小时，根据定时任务决定是否删除和人为删除事务
 *
 * @author hzh
 * @date 2023/08/22
 */
public class RedisTxStoreManager implements StoreManager {
    
    private final RedissonClient redissonClient;
    
    public RedisTxStoreManager(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
    }
    
    /**
     * 保存分支事务
     */
    @Override
    public boolean addBranch(BranchTxSession branchInfo) {
        //批处理
        RBatch batch = redissonClient.createBatch();
        //存储分支事务的所有信息
        RMapAsync<Object, Object> map = batch
                .getMap(RedisCacheKeyBuilder.buildBranchKey(branchInfo.getGlobalTxId(), branchInfo.getBranchTxId()));
        Map<String, ?> branchMap = BeanUtils.beanToMapIngoreNull(branchInfo);
        map.putAllAsync(branchMap);
        map.expireAsync(TcGlobalConfigCache.tcGlobalConfig.getStorageTime());
        //添加全局和分支事务的关系
        RScoredSortedSetAsync<Object> branchs = batch
                .getScoredSortedSet(RedisCacheKeyBuilder.buildBranchsKey(branchInfo.getGlobalTxId()));
        Long expireTime = branchInfo.getExpireTime();
        long timeout = expireTime - System.currentTimeMillis();
        
        //存储分数为过期的时间
        branchs.addAsync(expireTime, branchInfo.getBranchTxId());
        branchs.expireAsync(TcGlobalConfigCache.tcGlobalConfig.getStorageTime());
        
        BatchResult<?> execute = batch.execute();
        List<?> responses = execute.getResponses();
        return true;
    }
    
    
    @Override
    public List<String> getAllBranchIds(GlobalTxSession globalTxSession) {
        RScoredSortedSet<Object> set = redissonClient
                .getScoredSortedSet(RedisCacheKeyBuilder.buildBranchsKey(globalTxSession.getGlobalTxId()));
        Collection<Object> branchs = set.valueRange(0, Integer.MAX_VALUE);
        return branchs.stream().map(Object::toString).collect(Collectors.toList());
    }
    
    @Override
    public BranchTxSession getBranch(String globalTxId, String branchTxId) {
        RMap<Object, Object> map = redissonClient.getMap(RedisCacheKeyBuilder.buildBranchKey(globalTxId, branchTxId));
        Map<Object, Object> branchMap = map.readAllMap();
        Map<String, Object> branchMaps = branchMap.entrySet().stream()
                .collect(Collectors.toMap(e -> String.valueOf(e.getKey()), Map.Entry::getValue));
        return BeanUtils.mapToBean(branchMaps, BranchTxSession.class);
    }
    
    @Override
    public boolean removeBranch(BranchTxSession branchInfo) {
        RMap<Object, Object> map = redissonClient
                .getMap(RedisCacheKeyBuilder.buildBranchKey(branchInfo.getGlobalTxId(), branchInfo.getBranchTxId()));
        redissonClient.getScoredSortedSet(RedisCacheKeyBuilder.buildBranchsKey(branchInfo.getGlobalTxId()))
                .remove(branchInfo.getBranchTxId());
        return map.delete();
    }
    
    @Override
    public boolean removeBranch(GlobalTxSession globalTxSession) {
        //批处理删除
        RScoredSortedSet<Object> sortedSet = redissonClient
                .getScoredSortedSet(RedisCacheKeyBuilder.buildBranchsKey(globalTxSession.getGlobalTxId()));
        Collection<Object> objects = sortedSet.readAll();
        for (Object branch : objects) {
            String name = RedisCacheKeyBuilder.buildBranchKey(globalTxSession.getGlobalTxId(), branch.toString());
            redissonClient.getMap(name).delete();
        }
        sortedSet.delete();
        return true;
    }
    
    @Override
    public boolean putGlobal(GlobalTxSession globalTxSession) {
        RMap<Object, Object> map = redissonClient
                .getMap(RedisCacheKeyBuilder.buildGlobalKey(globalTxSession.getGlobalTxId()));
        Map<String, ?> branchMap = BeanUtils.beanToMap(globalTxSession);
        map.putAll(branchMap);
        map.expire(TcGlobalConfigCache.tcGlobalConfig.getStorageTime());
        Long expireTime = globalTxSession.getExpireTime();
        long timeout = expireTime - System.currentTimeMillis();
        if (timeout <= 0) {
            map.delete();
            throw new TxTimeoutException(String.format("globalTxId [%s] timeout ", globalTxSession.getGlobalTxId()));
        }
        
        //通过set去重直接覆盖事务id
        RScoredSortedSet<Object> sortedSet = redissonClient
                .getScoredSortedSet(RedisCacheKeyBuilder.buildGlobalStatusKey());
        //根据状态的值来设置分数
        int code = TxStatus.getTxStatusCode(globalTxSession.getStatus());
        sortedSet.add(code, globalTxSession.getGlobalTxId());
        //超过24小时照样删。每次添加都会重新设置超时时间
        sortedSet.expire(TcGlobalConfigCache.tcGlobalConfig.getStorageTime());
        return true;
    }
    
    @Override
    public boolean removeGlobal(GlobalTxSession globalTxSession) {
        RMap<Object, Object> map = redissonClient
                .getMap(RedisCacheKeyBuilder.buildGlobalKey(globalTxSession.getGlobalTxId()));
        //删除状态事务id
        RScoredSortedSet<Object> sortedSet = redissonClient
                .getScoredSortedSet(RedisCacheKeyBuilder.buildGlobalStatusKey());
        sortedSet.remove(globalTxSession.getGlobalTxId());
        return map.delete();
    }
    
    @Override
    public GlobalTxSession getByGlobalId(String globalTxId) {
        RMap<String, Object> map = redissonClient.getMap(RedisCacheKeyBuilder.buildGlobalKey(globalTxId));
        Map<String, Object> txMap = map.readAllMap();
        return BeanUtils.mapToBean(txMap, GlobalTxSession.class);
    }
    
    @Override
    public void updateGlobalStatus(String globalTxId, String status) {
        RMap<String, Object> map = redissonClient.getMap(RedisCacheKeyBuilder.buildGlobalKey(globalTxId));
        map.put("status", status);
        
        //通过set去重直接覆盖事务id
        RScoredSortedSet<Object> sortedSet = redissonClient
                .getScoredSortedSet(RedisCacheKeyBuilder.buildGlobalStatusKey());
        //根据状态的值来设置分数
        int code = TxStatus.getTxStatusCode(status);
        sortedSet.add(code, globalTxId);
    }
    
    @Override
    public void updateGlobalStatusAndRetryCount(String globalTxId, String status) {
        RMap<String, Object> map = redissonClient.getMap(RedisCacheKeyBuilder.buildGlobalKey(globalTxId));
        map.put("status", status);
        map.put("retryCount",Integer.parseInt(map.get("retryCount").toString()) + 1);
        //通过set去重直接覆盖事务id
        RScoredSortedSet<Object> sortedSet = redissonClient
                .getScoredSortedSet(RedisCacheKeyBuilder.buildGlobalStatusKey());
        //根据状态的值来设置分数
        int code = TxStatus.getTxStatusCode(status);
        sortedSet.add(code, globalTxId);
    }
    
    @Override
    public void updateBranchStatus(BranchTxSession branchTx, String status) {
        RMap<String, Object> map = redissonClient
                .getMap(RedisCacheKeyBuilder.buildBranchKey(branchTx.getGlobalTxId(), branchTx.getBranchTxId()));
        map.fastPut("status", status);
    }
    
    /**
     * 获取全局事务根据状态
     *
     * @return {@link List}<{@link GlobalTxSession}>
     */
    @Override
    public List<GlobalTxSession> getGlobalTxByStatus(TxStatus txStatus) {
        RScoredSortedSet<Object> sortedSet = redissonClient
                .getScoredSortedSet(RedisCacheKeyBuilder.buildGlobalStatusKey());
        List<GlobalTxSession> globalTxSessions = new ArrayList<>();
        //根据指定状态分数查询该状态下所有事务
        int scope = TxStatus.getTxStatusCode(txStatus.name());
        Collection<Object> globalTxIds = sortedSet.valueRange(scope, true, scope, true, 0, 1000);
        for (Object globalTxId : globalTxIds) {
            GlobalTxSession globalTxSession = getByGlobalId(globalTxId.toString());
            globalTxSessions.add(globalTxSession);
        }
        return globalTxSessions;
    }
    
    
}
