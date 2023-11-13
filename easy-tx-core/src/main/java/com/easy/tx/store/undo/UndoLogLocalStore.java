package com.easy.tx.store.undo;

import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


/**
 * 事务调用程序本地存储
 *
 * @author hzh
 * @date 2023/08/01
 */
@Slf4j
public class UndoLogLocalStore implements UndoLogStore {
    
    private static final Map<String, List<UndoLog>> LOCAL_CONTEXT = new ConcurrentHashMap<>();
    
    
    /**
     * 获取事务调用程序
     *
     * @param branchTxId 当地事务id
     */
    @Override
    public UndoLog getUndoLog(String globalTxId, String branchTxId) {
        List<UndoLog> undoLogs = LOCAL_CONTEXT.get(globalTxId);
        if (CollectionUtils.isEmpty(undoLogs)) {
            return null;
        }
        UndoLog undoLog = undoLogs.stream().filter(log1 -> log1.getBranchTxId().equals(branchTxId)).findFirst()
                .orElse(null);
        return undoLog;
    }
    
    /**
     * 保存事务调用程序
     *
     * @param undoLog
     * @return {@link SagaUndoLog}
     */
    @Override
    public UndoLog addUndoLog(UndoLog undoLog) {
        List<UndoLog> undoLogs = LOCAL_CONTEXT.computeIfAbsent(undoLog.getGlobalTxId(), a -> new ArrayList<>());
        undoLogs.add(undoLog);
        return undoLog;
    }
    
    /**
     * 删除事务调用程序
     *
     * @param branchTxId 当地事务id
     */
    @Override
    public void removeUndoLog(String globalTxId, String branchTxId) {
        List<UndoLog> undoLogs = LOCAL_CONTEXT.get(globalTxId);
        if (CollectionUtils.isEmpty(undoLogs)) {
            return;
        }
        undoLogs.removeIf(log -> log.getBranchTxId().equals(branchTxId));
    }
    
    /**
     * 删除事务调用程序
     *
     * @param globalTxId 当地事务id
     */
    @Override
    public void removeUndoLog(String globalTxId) {
        LOCAL_CONTEXT.remove(globalTxId);
    }
    
    @Override
    public boolean removeExpireUndoLog(Long timeout) {
        List<String> removeKeys = new ArrayList<>();
        
        LOCAL_CONTEXT.forEach((k, v) -> {
            for (UndoLog undoLog : v) {
                Date createTime = undoLog.getCreateTime();
                long expireTime = createTime.getTime() + timeout;
                if (System.currentTimeMillis() >= expireTime) {
                    removeKeys.add(k);
                    return;
                }
            }
        });
        boolean success = false;
        if (!CollectionUtils.isEmpty(removeKeys)) {
            success = true;
        }
        removeKeys.forEach(LOCAL_CONTEXT::remove);
        return success;
    }
}
