package com.easy.tx.store.undo;

import com.easy.tx.exception.TxException;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * undo日志数据库存储
 *
 * @author hzh
 * @date 2023/08/16
 */
public class UndoLogDbStore implements UndoLogStore {

    private DataSource dataSource;

    private UndoLogExcecutor undoLogExcecutor;

    @Override
    public UndoLog getUndoLog(String globalTxId, String branchTxId) {
        Connection connection = getConnection();
        return undoLogExcecutor.getUndoLog(connection, globalTxId,branchTxId);
    }


    @Override
    public UndoLog addUndoLog(UndoLog undoLog) {
        Connection connection = getConnection();
        undoLogExcecutor.addUndoLog(connection, undoLog);
        try {
            connection.commit();
        } catch (SQLException throwables) {
            throw new TxException(throwables);
        }
        return undoLog;
    }

    @Override
    public void removeUndoLog(String globalTxId,String branchTxId) {
        Connection connection = getConnection();
        undoLogExcecutor.removeUndoLog(connection,null, branchTxId);
        try {
            connection.commit();
        } catch (SQLException throwables) {
            throw new TxException(throwables);
        }
    }

    @Override
    public void removeUndoLog(String globalTxId) {
        Connection connection = getConnection();
        undoLogExcecutor.removeUndoLog(connection, globalTxId,null);
        try {
            connection.commit();
        } catch (SQLException throwables) {
            throw new TxException(throwables);
        }
    }
    
    @Override
    public boolean removeExpireUndoLog(Long timeout) {
        Connection connection = getConnection();
        boolean success = undoLogExcecutor.removeExpireUndoLog(connection, timeout);
        try {
            connection.commit();
        } catch (SQLException throwables) {
            throw new TxException(throwables);
        }
        return success;
    }
    
    private Connection getConnection() {
        try {
            return dataSource.getConnection();
        } catch (SQLException throwables) {
            throw new TxException(throwables);
        }
    }

}
