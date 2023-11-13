package com.easy.tx.util;

import com.easy.tx.store.undo.SagaUndoLog;

import java.nio.charset.StandardCharsets;

public interface UndoLogParser {

    default byte[] getDefaultContent() {
        return "".getBytes(StandardCharsets.UTF_8);
    }

    /**
     * Encode branch undo log to byte array.
     *
     * @param branchUndoLog the branch undo log
     * @return the byte array
     */
    byte[] encodeSaga(SagaUndoLog branchUndoLog);

    /**
     * Decode byte array to branch undo log.
     *
     * @param bytes the byte array
     * @return the branch undo log
     */
    SagaUndoLog decodeSaga(byte[] bytes);
}
