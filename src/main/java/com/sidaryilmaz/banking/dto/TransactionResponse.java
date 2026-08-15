package com.sidaryilmaz.banking.dto;

import com.sidaryilmaz.banking.model.TransactionStatus;

import java.math.BigDecimal;
import java.time.Instant;

public record TransactionResponse(
        Long id,
        String sourceIban,
        String targetIban,
        BigDecimal amount,
        String currency,
        TransactionStatus status,
        Instant executedAt
) {
}
