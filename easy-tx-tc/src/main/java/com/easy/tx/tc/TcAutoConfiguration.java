package com.easy.tx.tc;

import com.easy.tx.lock.BranchTxLock;
import com.easy.tx.lock.RedisBranchTxLock;
import com.easy.tx.remote.RemoteClientProccessManager;
import com.easy.tx.tc.manager.TcGlobalExecutor;
import com.easy.tx.tc.properties.TcGlobalConfigCache;
import com.easy.tx.tc.properties.TcProperties;
import com.easy.tx.tc.remote.RemoteServerProccessManager;
import com.easy.tx.tc.tx.RedisTxStoreManager;
import com.easy.tx.tc.tx.StoreManager;
import com.easy.tx.tc.remote.*;
import com.easy.tx.tc.time.TxTimer;
import com.easy.tx.util.InetUtils;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class TcAutoConfiguration implements InitializingBean {
    @Value("${server.port:80}")
    private String port;
    
    @Autowired
    private RedissonClient redissonClient;
    
    @Autowired
    private TcProperties tcProperties;
    
    @Bean
    public BranchTxLock branchTxLock() {
        return new RedisBranchTxLock(redissonClient);
    }
    
    @Bean
    public StoreManager storeManager() {
        return new RedisTxStoreManager(redissonClient);
    }
    
//    @Bean
//    @ConditionalOnMissingBean(RemotingClient.class)
//    public RemotingClient remotingClient(RemoteServerProccessManager remoteServerProccessManager) {
//        return new LocalRemotingClient(remoteServerProccessManager);
//    }
    
    @Bean
    @ConditionalOnMissingBean(RemotingServer.class)
    public RemotingServer remotingServer(RemoteClientProccessManager remoteClientProccessManager) {
        return new LocalRemotingServer(remoteClientProccessManager);
    }
    
    @Bean
    public TcGlobalExecutor txGlobalExecutor(StoreManager storeManager, BranchTxLock branchTxLock,
            RemotingServer remotingServer) {
        return new TcGlobalExecutor(branchTxLock, storeManager, remotingServer);
    }
    
    @Bean(initMethod = "start")
    public TxTimer txScheduledTask(TcGlobalExecutor txGlobalExecutor, StoreManager storeManager) {
        return new TxTimer(txGlobalExecutor, storeManager);
    }
    
    @Bean
    public RemoteServerProccessManager remoteProccessManager(StoreManager storeManager, BranchTxLock branchTxLock,
            TcGlobalExecutor globalExecutor) {
        //tc服务端处理消息管理器
        RemoteServerProccessManager remoteProccessManager = new RemoteServerProccessManager();
        //全局事务提交处理器
        GlobalConmmitProcessor globalConmmitProcessor = new GlobalConmmitProcessor(globalExecutor);
        //分支注册处理器
        BranchRegisterProcessor branchRegisterProcessor = new BranchRegisterProcessor(branchTxLock, storeManager);
        //全局回滚处理器
        GlobalRollbackProcessor globalRollbackProcessor = new GlobalRollbackProcessor(globalExecutor);
        //全局注册处理器
        GlobalRegisterProcessor globalRegisterProcessor = new GlobalRegisterProcessor(storeManager);
        KeepAvailableProcessor keepAvailableProcessor = new KeepAvailableProcessor();
        remoteProccessManager.registerProcessor(branchRegisterProcessor);
        remoteProccessManager.registerProcessor(globalConmmitProcessor);
        remoteProccessManager.registerProcessor(globalRollbackProcessor);
        remoteProccessManager.registerProcessor(globalRegisterProcessor);
        remoteProccessManager.registerProcessor(keepAvailableProcessor);
        return remoteProccessManager;
    }
    
    @Override
    public void afterPropertiesSet() throws Exception {
        Duration storageTime = tcProperties.getStorageTime();
        TcGlobalConfigCache.tcGlobalConfig.setStorageTime(storageTime);
        TcGlobalConfigCache.tcGlobalConfig.setCommitRetryCount(tcProperties.getCommitRetryCount());
        TcGlobalConfigCache.tcGlobalConfig.setRollbackRetryCount(tcProperties.getRollbackRetryCount());
        if (tcProperties.getTcAddress()!=null){
            TcGlobalConfigCache.tcGlobalConfig.setTcAddress(tcProperties.getTcAddress());
        }else{
            TcGlobalConfigCache.tcGlobalConfig.setTcAddress(InetUtils.findFirstNonLoopbackHostInfo().getIpAddress() + ":" + port);
        }
    }
}
