package com.easy.tx.bm.remote;

import com.easy.tx.constant.MessageType;
import com.easy.tx.message.BranchTxSession;
import com.easy.tx.pojo.GlobalTransactionInvoker;
import com.easy.tx.remote.*;
import com.easy.tx.store.undo.SagaUndoLog;
import com.easy.tx.store.undo.UndoLog;
import com.easy.tx.store.undo.UndoLogStore;
import com.easy.tx.util.JacksonUndoLogParser;
import com.easy.tx.util.RpcMessageUtil;
import com.easy.tx.util.TxObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.context.ApplicationContext;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Objects;

/**
 * 分支回滚处理器
 *
 * @author hzh
 * @date 2023/08/22
 */
@Slf4j
public class BranchRollbackProcessor implements RemoteMessageProcessor {
    
    private final UndoLogStore undoLogStore;
    
    private ApplicationContext applicationContext;
    
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }
    
    public BranchRollbackProcessor(UndoLogStore undoLogStore) {
        this.undoLogStore = undoLogStore;
    }
    
    /**
     * 处理消息
     */
    @Override
    public void process(RemoteResponse response, RemoteMessage message) {
        String body = message.getBody();
        //返回结果
        RemoteMessage responseMessage = RpcMessageUtil.buildResponseMessage(true, getType());
        response.setRpcMessage(responseMessage);
        BranchTxSession branchTxSession = TxObjectMapper.toBean(body,BranchTxSession.class);
        String branchTxId = branchTxSession.getBranchTxId();
        String globalTxId = branchTxSession.getGlobalTxId();
        try {
            UndoLog undoLog = undoLogStore.getUndoLog(globalTxId, branchTxId);
            log.info("Saga Local Transaction rollback Begin...{} ... txLog:{}", branchTxId, undoLog);
            if (undoLog == null) {
                return;
            }
            byte[] rollbackInfo = undoLog.getRollbackInfo();
            SagaUndoLog sagaUndoLog = JacksonUndoLogParser.decodeSaga(rollbackInfo);
            
            Class<?> aClass = Class.forName(sagaUndoLog.getClassName());
            Object bean = applicationContext.getBean(aClass);
            String[] parameterTypesArray = sagaUndoLog.getParameterTypes();
            Class[] parameterTypes = Arrays.stream(parameterTypesArray).map(a -> {
                try {
                    return Class.forName(a);
                } catch (ClassNotFoundException e) {
                    log.error("Saga Global tx ClassNotFoundException", e);
                }
                return null;
            }).filter(Objects::nonNull).toArray(Class[]::new);
            if (ArrayUtils.isEmpty(parameterTypes)) {
                return;
            }
            Method method = bean.getClass().getMethod(sagaUndoLog.getMethodName(), parameterTypes);
            Object[] args = sagaUndoLog.getArgs();
            GlobalTransactionInvoker transactionInvoker = new GlobalTransactionInvoker(bean, args, method,
                    parameterTypes);
            transactionInvoker.invoke();
            log.info("Saga Local Transaction rollback End...{} ... txLog:{}", branchTxId, undoLog);
        } catch (Exception e) {
            log.error("Saga Global rollback Exception branchTxId={} ex:", branchTxId, e);
            //异常返回失败
            responseMessage = RpcMessageUtil.buildResponseMessage(false, getType());
            response.setRpcMessage(responseMessage);
            return;
        }
        undoLogStore.removeUndoLog(globalTxId, branchTxId);
        log.info("Saga Global Transaction rollback Finish ...{} ...branchTxIds{}", globalTxId, branchTxId);
    }
    
    @Override
    public Integer getType() {
        return MessageType.BM_ROLLBACK;
    }
    
    
}
