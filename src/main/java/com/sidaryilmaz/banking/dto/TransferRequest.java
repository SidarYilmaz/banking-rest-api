package com.sidaryilmaz.banking.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record TransferRequest(

        @NotBlank
        String sourceIban,

        @NotBlank
        String targetIban,

        @NotNull
        @DecimalMin(value = "0.01", message = "transfer amount must be greater than zero")
        BigDecimal amount
) {
}
