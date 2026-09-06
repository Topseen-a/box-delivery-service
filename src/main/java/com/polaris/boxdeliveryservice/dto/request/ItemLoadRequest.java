package com.polaris.boxdeliveryservice.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.math.BigDecimal;

public record ItemLoadRequest (

        @NotBlank
        @Pattern(regexp = "^[A-Za-z0-9_-]+$", message = "name may only contain letters, numbers, hyphen and underscore")
        String name,

        @NotNull
        @DecimalMin(value = "0", inclusive = false)
        BigDecimal weight,

        @NotBlank
        @Pattern(regexp = "^[A-Z0-9_]+$", message = "code may only contain upper case letters, numbers and underscore")
        String code
) {
}
