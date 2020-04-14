package org.example.rest.exception;

public class ApplicationCustomProcessorException extends Exception {

    public ApplicationCustomProcessorException() {
    }

    public ApplicationCustomProcessorException(String message) {
        super(message);
    }

    public ApplicationCustomProcessorException(String message, Throwable cause) {
        super(message, cause);
    }

    public ApplicationCustomProcessorException(Throwable cause) {
        super(cause);
    }

    public ApplicationCustomProcessorException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
