package com.easy.tx.bm.controller;

import com.easy.tx.remote.RemoteClientProccessManager;
import com.easy.tx.remote.RemoteMessage;
import com.easy.tx.remote.RemoteResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 远程控制器
 *
 * @author hzh
 * @date 2023/08/15
 */
@RestController
@RequestMapping
public class RemoteController {

    @Autowired
    private RemoteClientProccessManager remoteClientProccessManager;

    @PostMapping("/easy/tx")
    public RemoteResponse txMessage(@RequestBody RemoteMessage remoteMessage) {
        RemoteResponse remoteResponse = new RemoteResponse();
        remoteClientProccessManager.processMessage(remoteResponse,remoteMessage);
        return remoteResponse;
    }
    
}
