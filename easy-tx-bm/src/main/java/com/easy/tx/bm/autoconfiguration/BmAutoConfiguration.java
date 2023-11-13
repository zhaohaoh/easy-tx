package com.easy.tx.bm.autoconfiguration;

import com.easy.tx.bm.config.HeartbeatService;
import com.easy.tx.bm.config.HttpHeartbeatService;
import com.easy.tx.bm.remote.BranchCommitProcessor;
import com.easy.tx.bm.remote.BranchRollbackProcessor;
import com.easy.tx.remote.RemoteClientProccessManager;
import com.easy.tx.remote.RemotingClient;
import com.easy.tx.store.undo.UndoLogStore;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author hzh
 * BM= branchManager  分支事务
 */
@Configuration
public class BmAutoConfiguration implements ApplicationContextAware {
    private ApplicationContext applicationContext;
    @Autowired
    private UndoLogStore undoLogStore;
    
    /**
     * BM客户端处理管理器
     *
     * @return {@link RemoteClientProccessManager}
     */
    @Bean
    public RemoteClientProccessManager remoteClientProccessManager() {
        RemoteClientProccessManager remoteClientProccessManager = new RemoteClientProccessManager();
        BranchCommitProcessor globalConmmitProcessor = new BranchCommitProcessor(undoLogStore);
        BranchRollbackProcessor branchRegisterProcessor = new BranchRollbackProcessor(undoLogStore);
        branchRegisterProcessor.setApplicationContext(applicationContext);


        remoteClientProccessManager.registerProcessor(branchRegisterProcessor);
        remoteClientProccessManager.registerProcessor(globalConmmitProcessor);
        return remoteClientProccessManager;
    }
    
    /**
     * 心跳服务
     *
     * @param remotingClient 远程处理客户端
     * @return {@link HeartbeatService}
     */
    @Bean
    public HeartbeatService heartbeatService(RemotingClient remotingClient) {
        return new HttpHeartbeatService(remotingClient);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext=applicationContext;
    }
}
