package com.easy.tx.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.web.bind.annotation.GetMapping;

@Getter
@AllArgsConstructor
public enum TxIdEnum {
    //全局事务
    GLOBAL,
    //分支
    BRANCH;
}
