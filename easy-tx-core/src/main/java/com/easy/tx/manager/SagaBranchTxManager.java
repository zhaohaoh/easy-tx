package com.easy.tx.manager;

import com.easy.tx.constant.BranchType;
import com.easy.tx.context.GlobalTxContext;
import com.easy.tx.exception.TxException;
import com.easy.tx.manager.branch.AbstractBranchTxManager;
import com.easy.tx.manager.branch.BranchTxResourceManager;
import com.easy.tx.message.BranchTxSession;
import com.easy.tx.pojo.SagaComponentInfo;
import com.easy.tx.store.undo.SagaUndoLog;
import com.easy.tx.store.undo.UndoLogStore;
import com.easy.tx.store.undo.UndoLog;
import com.easy.tx.strategy.TxIdGenerater;
import com.easy.tx.util.JacksonUndoLogParser;
import lombok.extern.slf4j.Slf4j;
import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * 本地事务经理
 *
 * @author hzh
 * @date 2023/08/01
 */
@Slf4j
public class SagaBranchTxManager extends AbstractBranchTxManager {


    public SagaBranchTxManager(UndoLogStore undoLogStore, TxIdGenerater txIdGenerateStrategy, BranchTxResourceManager branchTxResource) {
        super(undoLogStore, txIdGenerateStrategy, branchTxResource);
    }

    /**
     * 手动开启事务
     */
    @Override
    public String startTransaction(BranchTxSession branchInfo) {
        String globalTxId = GlobalTxContext.getGlobalTxId();
        if (globalTxId == null) {
            throw new TxException("Local Transaction Try Begin But globalTxId isEmpty!");
        }
        String branchTxId = newTxId();
        branchInfo.setBranchTxId(branchTxId);
        log.debug("start branchTx [{}]", branchTxId);
        //注册分支事务
        branchTxResource.branchRegister(branchInfo);
        return branchTxId;
    }



    /**
     * saga分支提交记录回滚方法   应该未提交也要记录undolog回滚 因为一个方法中 有1,2,3个调用，不能确定是哪个调用出现错误，哪个失败
     *
     */
    @Override
    public void commit(String branchTxId, SagaComponentInfo sagaComponentInfo) {
        SagaUndoLog sagaUndoLog = getSagaUndoLog(sagaComponentInfo);
        UndoLog undoLog = new UndoLog();
        undoLog.setRollbackInfo(JacksonUndoLogParser.encodeSaga(sagaUndoLog));
        undoLog.setBranchTxId(branchTxId);
        undoLog.setGlobalTxId(GlobalTxContext.getGlobalTxId());
        undoLog.setBranchType(BranchType.SAGA.ordinal());
        undoLogStore.addUndoLog(undoLog);
    }

    /**
     * saga分支回滚由全局事务发起回滚操作  后续可以加入失败重试机制
     */
    @Override
    public void rollback() {
    }

    private SagaUndoLog getSagaUndoLog(SagaComponentInfo sagaComponentInfo) {
        Object thisProxy = sagaComponentInfo.getRollbackProxy();

        Object[] args = sagaComponentInfo.getArgs();
        String lockKey = sagaComponentInfo.getLockKey();
        String rollbackMethod = sagaComponentInfo.getRollbackMethod();
        Method[] methods = thisProxy.getClass().getMethods();
        String[] parameterTypesStr = new String[0];
        //只根据名字找方法 只找public
        for (Method method : methods) {
            boolean equals = method.getName().equals(rollbackMethod);
            if (equals) {
                Class<?>[] parameterTypes = method.getParameterTypes();
                parameterTypesStr = Arrays.stream(parameterTypes).map(Class::getName).toArray(String[]::new);
                if (parameterTypesStr.length >= args.length) {
                    break;
                }
            }
        }


        //添加事务

        return new SagaUndoLog(sagaComponentInfo.getRollbackProxy().getClass().getName(), sagaComponentInfo.getRollbackMethod(),
                sagaComponentInfo.getArgs(), parameterTypesStr);
    }
}
