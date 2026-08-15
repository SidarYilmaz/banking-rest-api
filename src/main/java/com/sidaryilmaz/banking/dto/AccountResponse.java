package com.sidaryilmaz.banking.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record AccountResponse(
        Long id,
        String iban,
        String ownerName,
        String currency,
        BigDecimal balance,
        Instant createdAt
) {
}
