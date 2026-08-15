package com.sidaryilmaz.banking.service;

import com.sidaryilmaz.banking.dto.AccountResponse;
import com.sidaryilmaz.banking.dto.CreateAccountRequest;
import com.sidaryilmaz.banking.dto.TransactionResponse;
import com.sidaryilmaz.banking.dto.TransferRequest;
import com.sidaryilmaz.banking.exception.AccountNotFoundException;
import com.sidaryilmaz.banking.exception.CurrencyMismatchException;
import com.sidaryilmaz.banking.exception.InsufficientFundsException;
import com.sidaryilmaz.banking.mapper.AccountMapper;
import com.sidaryilmaz.banking.model.Account;
import com.sidaryilmaz.banking.model.Transaction;
import com.sidaryilmaz.banking.model.TransactionStatus;
import com.sidaryilmaz.banking.repository.AccountRepository;
import com.sidaryilmaz.banking.repository.TransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AccountService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final IbanGenerator ibanGenerator;
    private final AccountMapper mapper;

    public AccountService(AccountRepository accountRepository,
                          TransactionRepository transactionRepository,
                          IbanGenerator ibanGenerator,
                          AccountMapper mapper) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.ibanGenerator = ibanGenerator;
        this.mapper = mapper;
    }

    @Transactional
    public AccountResponse createAccount(CreateAccountRequest request) {
        String iban = generateUniqueIban();
        Account account = new Account(
                iban,
                request.ownerName(),
                request.currency(),
                request.initialBalance()
        );
        return mapper.toResponse(accountRepository.save(account));
    }

    @Transactional(readOnly = true)
    public AccountResponse getAccount(String iban) {
        return mapper.toResponse(findAccountOrThrow(iban));
    }

    @Transactional
    public TransactionResponse transfer(TransferRequest request) {
        Account source = findAccountOrThrow(request.sourceIban());
        Account target = findAccountOrThrow(request.targetIban());

        if (!source.getCurrency().equals(target.getCurrency())) {
            throw new CurrencyMismatchException(source.getCurrency(), target.getCurrency());
        }
        if (source.getBalance().compareTo(request.amount()) < 0) {
            throw new InsufficientFundsException(source.getIban());
        }

        source.debit(request.amount());
        target.credit(request.amount());

        Transaction transaction = new Transaction(
                source.getIban(),
                target.getIban(),
                request.amount(),
                source.getCurrency(),
                TransactionStatus.COMPLETED
        );
        return mapper.toResponse(transactionRepository.save(transaction));
    }

    @Transactional(readOnly = true)
    public List<TransactionResponse> getTransactions(String iban) {
        findAccountOrThrow(iban);
        return transactionRepository
                .findBySourceIbanOrTargetIbanOrderByExecutedAtDesc(iban, iban)
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    private Account findAccountOrThrow(String iban) {
        return accountRepository.findByIban(iban)
                .orElseThrow(() -> new AccountNotFoundException(iban));
    }

    private String generateUniqueIban() {
        String iban;
        do {
            iban = ibanGenerator.generate();
        } while (accountRepository.existsByIban(iban));
        return iban;
    }
}
