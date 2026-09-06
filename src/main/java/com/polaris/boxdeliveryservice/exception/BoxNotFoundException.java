package com.polaris.boxdeliveryservice.exception;

public class BoxNotFoundException extends RuntimeException {
    public BoxNotFoundException(Long boxId) {
        super("Box not found with id " + boxId);
    }
}
