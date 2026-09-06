package com.polaris.boxdeliveryservice.exception;

public class WeightLimitExceededException extends RuntimeException {
    public WeightLimitExceededException(Long boxId, java.math.BigDecimal attempted, java.math.BigDecimal limit) {
        super("Box " + boxId + " cannot carry " + attempted + "g — weight limit is " + limit + "g");
    }
}
