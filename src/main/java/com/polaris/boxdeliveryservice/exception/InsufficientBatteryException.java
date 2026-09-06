package com.polaris.boxdeliveryservice.exception;

public class InsufficientBatteryException extends RuntimeException {
    public InsufficientBatteryException(Long boxId, Integer currentBattery) {
        super("Box " + boxId + " battery is " + currentBattery + "%, minimum 25% required to enter LOADING state");
    }
}
