package com.sidaryilmaz.banking.controller;

import com.sidaryilmaz.banking.dto.AccountResponse;
import com.sidaryilmaz.banking.dto.CreateAccountRequest;
import com.sidaryilmaz.banking.dto.TransactionResponse;
import com.sidaryilmaz.banking.dto.TransferRequest;
import com.sidaryilmaz.banking.service.AccountService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/accounts")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AccountResponse createAccount(@Valid @RequestBody CreateAccountRequest request) {
        return accountService.createAccount(request);
    }

    @GetMapping("/{iban}")
    public AccountResponse getAccount(@PathVariable String iban) {
        return accountService.getAccount(iban);
    }

    @GetMapping("/{iban}/transactions")
    public List<TransactionResponse> getTransactions(@PathVariable String iban) {
        return accountService.getTransactions(iban);
    }

    @PostMapping("/transfers")
    public ResponseEntity<TransactionResponse> transfer(@Valid @RequestBody TransferRequest request) {
        return ResponseEntity.ok(accountService.transfer(request));
    }
}
