package com.easy.tx.samples.controller;

import com.easy.tx.annotation.GlobalTransaction;
import com.easy.tx.annotation.SagaComponent;
import com.easy.tx.template.SagaTxComponentTemplate;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.framework.AopContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.io.IOException;
import java.lang.reflect.Method;


@RestController
@Slf4j
@Api(tags = "后台-redis测试")
@RequestMapping("/redis")
public class TestController {
    @Autowired
    private SagaTxComponentTemplate sagaTxComponentTemplate;

    @ApiOperation(value = "测试新的sss", notes = "")
    @PostMapping("/aaaa/sergfgfgfgssss")
    @Transactional
    @SagaComponent(robackFor = "aaa")
    public Object sefffrssssssssssssssss(@RequestBody SysTest sysTest) {

        return null;
    }

    @ApiOperation(value = "测试新的", notes = "")
    @PostMapping("/aaaa/sergfgfgfg")
    @Transactional
    @SagaComponent(robackFor = "aaa", lockFor = "#sysTest.ids", lockKeyForClassName = {SysTest.class})
    public Object sefffr(@RequestBody SysTest sysTest) {
        TestController o = (TestController) AopContext.currentProxy();
        try {
            Method method = TestController.class.getMethod("bbbbb");
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        return null;
    }

    @ApiOperation(value = "测试事务", notes = "")
    @PostMapping("/aaaa/bbbb")
    public Object bbbbb(@RequestBody SysTest sysTest) throws IOException, InterruptedException {
        SysTest sysTest1 = new SysTest();
        sysTest1.setEmail("213sdas");
        sysTest1.setId(1L);
        Long[] longs = {1L, 2L, 3L};
        sysTest1.setIds(longs);
        TestController o = (TestController) AopContext.currentProxy();
        o.sefffr(sysTest1);
        o.sefffr(sysTest);
        return sysTest1;
    }


    @ApiOperation(value = "手动开启事务", notes = "")
    @PostMapping("/aaaa/testManual")
    @Transactional
    @GlobalTransaction
    public void testManual(@RequestBody SysTest sysTest) {
        SysTest sysTest1 = new SysTest();
        sysTest1.setEmail("213sdas");
        sysTest1.setId(1L);
        Long[] longs = {1L, 2L, 3L};
        sysTest1.setIds(longs);

        sagaTxComponentTemplate.startSagaTx("aaa", "bbb", this, "aaa", sysTest1, () -> {
            TestController o = (TestController) AopContext.currentProxy();
            o.sefffr(sysTest1);
            return null;
        });

        sagaTxComponentTemplate.startSagaTx("aaa", "bbb", this, "aaa", sysTest1, () -> {
            TestController o = (TestController) AopContext.currentProxy();
            o.sefffr(sysTest1);
            throw new RuntimeException();
        });
    }

    public String aaa(SysTest aaa) {
        SysTest serx = new SysTest();
        serx.setPhone("12345555");
        System.out.println("事务回滚");
        return "123";
    }


    public String lock(SysTest aaa) {
        SysTest serx = new SysTest();
        serx.setPhone("12345555");
        System.out.println("锁定事务");
        return "123";
    }

}




