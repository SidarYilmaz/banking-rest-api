package com.sidaryilmaz.banking.service;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;

@Component
public class IbanGenerator {

    private static final String COUNTRY_CODE = "TR";
    private static final int ACCOUNT_DIGITS = 22;

    private final SecureRandom random = new SecureRandom();

    public String generate() {
        StringBuilder digits = new StringBuilder(ACCOUNT_DIGITS);
        for (int i = 0; i < ACCOUNT_DIGITS; i++) {
            digits.append(random.nextInt(10));
        }
        String checkDigits = String.format("%02d", random.nextInt(100));
        return COUNTRY_CODE + checkDigits + digits;
    }
}
