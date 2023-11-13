package com.easy.tx.context;

public interface UndoLogTableConstant {


    String UNDO_LOG_TABLE_NAME = "undo_log";
    /**
     * The constant undo_log column name xid
     * this field is not use in mysql
     */
    String UNDO_LOG_ID = "id";

    /**
     * The constant undo_log column name xid
     */
    String UNDO_LOG_GLOBAL_TX_ID = "global_tx_id";

    /**
     * The constant undo_log column name branch_id
     */
    String UNDO_LOG_BRANCH_XID = "branch_tx_id";

    /**
     * The constant undo_log column name rollback_info
     */
    String UNDO_LOG_ROLLBACK_INFO = "rollback_info";
    /**
     * The constant undo_log column name rollback_info
     */
    String UNDO_LOG_EXT = "ext";
    /**
     * The constant undo_log column name rollback_info
     */
    String UNDO_LOG_BRANCH_TYPE = "branch_type";
    
    String CREATE_TIME = "create_time";
}
