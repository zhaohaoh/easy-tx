package com.easy.tx.exception;

public class TxTimeoutException extends TxException{
    public TxTimeoutException(String message) {
        super(message);
    }

    public TxTimeoutException(String message, Throwable cause) {
        super(message, cause);
    }

    public TxTimeoutException(Throwable cause) {
        super(cause);
    }

    public TxTimeoutException() {
        super();
    }
}
