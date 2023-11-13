package com.easy.tx.store.undo;

import com.easy.tx.exception.TxException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static com.easy.tx.context.UndoLogTableConstant.*;

public class UndoLogExcecutor {
    
    private static final String INSERT_UNDO_LOG_SQL =
            "INSERT INTO " + UNDO_LOG_TABLE_NAME + " (" + UNDO_LOG_BRANCH_XID + ", " + UNDO_LOG_GLOBAL_TX_ID + ", "
                    + UNDO_LOG_EXT + ", " + UNDO_LOG_ROLLBACK_INFO + ")" + " VALUES (?, ?, ?, ?)";
    
    protected static final String DELETE_UNDO_LOG_SQL = "DELETE FROM " + UNDO_LOG_TABLE_NAME + " WHERE 1=1";
    
    protected static final String SELECT_UNDO_LOG_SQL =
            "SELECT * FROM " + UNDO_LOG_TABLE_NAME + " WHERE " + UNDO_LOG_GLOBAL_TX_ID + " = ?  AND "
                    + UNDO_LOG_BRANCH_XID + " = ?  FOR UPDATE";
    
    /**
     * 删除过期日志sql
     */
    protected static final String DELETE_EXPIRE_UNDO_LOG_SQL =
            "DELETE FROM " + UNDO_LOG_TABLE_NAME + " WHERE " + CREATE_TIME + " + ?  <=  now()";
    
    public UndoLog getUndoLog(Connection connection, String gTxId, String branchTxId) {
        ResultSet rs = null;
        PreparedStatement selectPST = null;
        try {
            // Find UNDO LOG
            selectPST = connection.prepareStatement(SELECT_UNDO_LOG_SQL);
            selectPST.setString(1, gTxId);
            selectPST.setString(2, branchTxId);
            rs = selectPST.executeQuery();
            boolean exists = false;
            while (rs.next()) {
                exists = true;
                // It is possible that the server repeatedly sends a rollback request to roll back
                // the same branch transaction to multiple processes,
                // ensuring that only the undo_log in the normal state is processed.
                String globalTxId = rs.getString(UNDO_LOG_GLOBAL_TX_ID);
                Integer branchType = rs.getInt(UNDO_LOG_BRANCH_TYPE);
                byte[] rollbackInfo = rs.getBytes(UNDO_LOG_ROLLBACK_INFO);
                UndoLog undoLog = new UndoLog();
                undoLog.setGlobalTxId(globalTxId);
                undoLog.setBranchTxId(branchTxId);
                undoLog.setRollbackInfo(rollbackInfo);
                undoLog.setBranchType(branchType);
                return undoLog;
            }
        } catch (Exception e) {
            throw new TxException(e);
        }
        return null;
    }
    
    
    public UndoLog addUndoLog(Connection connection, UndoLog undoLog) {
        try (PreparedStatement pst = connection.prepareStatement(INSERT_UNDO_LOG_SQL)) {
            pst.setString(1, undoLog.getBranchTxId());
            pst.setString(2, undoLog.getGlobalTxId());
            pst.setString(3, undoLog.getExt());
            pst.setBytes(4, undoLog.getRollbackInfo());
            pst.executeUpdate();
        } catch (Exception e) {
            if (!(e instanceof SQLException)) {
                e = new SQLException(e);
            }
        }
        return undoLog;
    }
    
    
    public void removeUndoLog(Connection connection, String globalTxId, String branchTxId) {
        String deleteUndoLogSql = DELETE_UNDO_LOG_SQL;
        if (globalTxId != null) {
            deleteUndoLogSql = deleteUndoLogSql + " AND " + UNDO_LOG_GLOBAL_TX_ID + " = ?";
        }
        if (branchTxId != null) {
            deleteUndoLogSql = deleteUndoLogSql + " AND " + UNDO_LOG_BRANCH_XID + " = ?";
        }
        try (PreparedStatement deletePST = connection.prepareStatement(deleteUndoLogSql)) {
            if (globalTxId != null) {
                deletePST.setString(1, globalTxId);
            }
            if (branchTxId != null) {
                deletePST.setString(2, branchTxId);
            }
            deletePST.executeUpdate();
        } catch (Exception e) {
            if (!(e instanceof SQLException)) {
                e = new SQLException(e);
            }
        }
    }
    
    public boolean removeExpireUndoLog(Connection connection, Long timeout) {
        
        try (PreparedStatement deletePST = connection.prepareStatement(DELETE_EXPIRE_UNDO_LOG_SQL)) {
            Date date = new Date(timeout);
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String format = simpleDateFormat.format(date);
            deletePST.setString(1, format);
            int update = deletePST.executeUpdate();
            if (update > 0) {
                return true;
            }
        } catch (Exception e) {
            if (!(e instanceof SQLException)) {
                e = new SQLException(e);
            }
        }
        return false;
    }
}
