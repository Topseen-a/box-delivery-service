package com.polaris.boxdeliveryservice.exception;

public class InvalidBoxStateException extends RuntimeException {
    public InvalidBoxStateException(Long boxId, String currentState, String requiredState) {
        super("Box " + boxId + " is in state " + currentState + ", expected " + requiredState);
    }
}
