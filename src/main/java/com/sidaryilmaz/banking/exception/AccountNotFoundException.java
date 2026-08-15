package com.sidaryilmaz.banking.exception;

public class AccountNotFoundException extends RuntimeException {

    public AccountNotFoundException(String iban) {
        super("Account not found for IBAN " + iban);
    }
}
