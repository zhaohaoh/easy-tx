package com.easy.tx.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum RecoveryEnum {
    //正向恢复
    FORWARD,
    // 反向回滚
    ROLLBACK
}
