package com.polaris.boxdeliveryservice.dto.request;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record BoxCreateRequest(

        @NotBlank
        @Size(max = 20)
        String txref,

        @NotNull
        @DecimalMin(value = "0", inclusive = false)
        @DecimalMax(value = "500")
        BigDecimal weightLimit,

        @NotNull
        @Min(0)
        @Max(100)
        Integer batteryCapacity
) {
}
