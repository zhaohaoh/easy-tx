package com.easy.tx.bm.client;


import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping
public class HttpHeartBeatController {
    
    @GetMapping("heartbeat")
    public void heartbeat(){
    
    }

}
