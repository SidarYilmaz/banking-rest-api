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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private IbanGenerator ibanGenerator;

    @InjectMocks
    private AccountService accountService;

    private AccountMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new AccountMapper();
        accountService = new AccountService(accountRepository, transactionRepository, ibanGenerator, mapper);
    }

    @Test
    void createAccountPersistsAccountWithGeneratedIban() {
        CreateAccountRequest request = new CreateAccountRequest("Ada Lovelace", "TRY", new BigDecimal("1000.00"));
        when(ibanGenerator.generate()).thenReturn("TR000000000000000000000001");
        when(accountRepository.existsByIban(anyString())).thenReturn(false);
        when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AccountResponse response = accountService.createAccount(request);

        assertThat(response.iban()).isEqualTo("TR000000000000000000000001");
        assertThat(response.ownerName()).isEqualTo("Ada Lovelace");
        assertThat(response.balance()).isEqualByComparingTo("1000.00");
    }

    @Test
    void createAccountRegeneratesIbanOnCollision() {
        CreateAccountRequest request = new CreateAccountRequest("Grace Hopper", "USD", BigDecimal.ZERO);
        when(ibanGenerator.generate()).thenReturn("TR001", "TR002");
        when(accountRepository.existsByIban("TR001")).thenReturn(true);
        when(accountRepository.existsByIban("TR002")).thenReturn(false);
        when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AccountResponse response = accountService.createAccount(request);

        assertThat(response.iban()).isEqualTo("TR002");
    }

    @Test
    void getAccountThrowsWhenMissing() {
        when(accountRepository.findByIban("TR999")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> accountService.getAccount("TR999"))
                .isInstanceOf(AccountNotFoundException.class);
    }

    @Test
    void transferMovesFundsBetweenAccounts() {
        Account source = new Account("TR001", "Sender", "TRY", new BigDecimal("500.00"));
        Account target = new Account("TR002", "Receiver", "TRY", new BigDecimal("100.00"));
        when(accountRepository.findByIban("TR001")).thenReturn(Optional.of(source));
        when(accountRepository.findByIban("TR002")).thenReturn(Optional.of(target));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TransferRequest request = new TransferRequest("TR001", "TR002", new BigDecimal("200.00"));
        TransactionResponse response = accountService.transfer(request);

        assertThat(source.getBalance()).isEqualByComparingTo("300.00");
        assertThat(target.getBalance()).isEqualByComparingTo("300.00");
        assertThat(response.status()).isEqualTo(TransactionStatus.COMPLETED);
    }

    @Test
    void transferRejectedWhenBalanceTooLow() {
        Account source = new Account("TR001", "Sender", "TRY", new BigDecimal("50.00"));
        Account target = new Account("TR002", "Receiver", "TRY", new BigDecimal("100.00"));
        when(accountRepository.findByIban("TR001")).thenReturn(Optional.of(source));
        when(accountRepository.findByIban("TR002")).thenReturn(Optional.of(target));

        TransferRequest request = new TransferRequest("TR001", "TR002", new BigDecimal("200.00"));

        assertThatThrownBy(() -> accountService.transfer(request))
                .isInstanceOf(InsufficientFundsException.class);
        verify(transactionRepository, never()).save(any(Transaction.class));
    }

    @Test
    void transferRejectedOnCurrencyMismatch() {
        Account source = new Account("TR001", "Sender", "TRY", new BigDecimal("500.00"));
        Account target = new Account("TR002", "Receiver", "USD", new BigDecimal("100.00"));
        when(accountRepository.findByIban("TR001")).thenReturn(Optional.of(source));
        when(accountRepository.findByIban("TR002")).thenReturn(Optional.of(target));

        TransferRequest request = new TransferRequest("TR001", "TR002", new BigDecimal("100.00"));

        assertThatThrownBy(() -> accountService.transfer(request))
                .isInstanceOf(CurrencyMismatchException.class);
    }

    @Test
    void transferPersistsCompletedTransactionRecord() {
        Account source = new Account("TR001", "Sender", "TRY", new BigDecimal("500.00"));
        Account target = new Account("TR002", "Receiver", "TRY", new BigDecimal("100.00"));
        when(accountRepository.findByIban("TR001")).thenReturn(Optional.of(source));
        when(accountRepository.findByIban("TR002")).thenReturn(Optional.of(target));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> invocation.getArgument(0));

        accountService.transfer(new TransferRequest("TR001", "TR002", new BigDecimal("150.00")));

        ArgumentCaptor<Transaction> captor = ArgumentCaptor.forClass(Transaction.class);
        verify(transactionRepository).save(captor.capture());
        assertThat(captor.getValue().getAmount()).isEqualByComparingTo("150.00");
        assertThat(captor.getValue().getStatus()).isEqualTo(TransactionStatus.COMPLETED);
    }
}
