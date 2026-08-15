package com.sidaryilmaz.banking.exception;

public class CurrencyMismatchException extends RuntimeException {

    public CurrencyMismatchException(String sourceCurrency, String targetCurrency) {
        super("Currency mismatch between accounts: " + sourceCurrency + " and " + targetCurrency);
    }
}
