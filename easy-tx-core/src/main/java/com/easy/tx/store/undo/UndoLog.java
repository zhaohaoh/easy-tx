package com.easy.tx.store.undo;

import com.easy.tx.constant.BranchType;
import lombok.Data;

import java.util.Date;

/**
 * undo日志
 *
 * @author hzh
 * @date 2023/08/16
 */
@Data
public class UndoLog {
    /**
     * id
     */
    private Long id;
    /**
     * 分支id
     */
    private String branchTxId;
    /**
     * 全局事务id
     */
    private String globalTxId;
    /**
     * 回滚信息
     */
    private byte[] rollbackInfo;
    /**
     * 分支类型
     */
    private Integer branchType;
    /**
     * 创建时间
     */
    private Date createTime;
    /**
     * 更新时间
     */
    private Date updateTime;
    /**
     * 扩展字段
     */
    private String ext;
}
