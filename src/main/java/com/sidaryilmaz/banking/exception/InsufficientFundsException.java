package com.sidaryilmaz.banking.exception;

public class InsufficientFundsException extends RuntimeException {

    public InsufficientFundsException(String iban) {
        super("Insufficient funds on account " + iban);
    }
}
