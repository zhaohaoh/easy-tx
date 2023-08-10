package com.easy.tx.exception;

public class TxException extends RuntimeException {
    public TxException(String message) {
        super(message);
    }

    public TxException(String message, Throwable cause) {
        super(message, cause);
    }

    public TxException(Throwable cause) {
        super(cause);
    }

    public TxException() {
        super();
    }
}