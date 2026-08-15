package com.sidaryilmaz.banking.mapper;

import com.sidaryilmaz.banking.dto.AccountResponse;
import com.sidaryilmaz.banking.dto.TransactionResponse;
import com.sidaryilmaz.banking.model.Account;
import com.sidaryilmaz.banking.model.Transaction;
import org.springframework.stereotype.Component;

@Component
public class AccountMapper {

    public AccountResponse toResponse(Account account) {
        return new AccountResponse(
                account.getId(),
                account.getIban(),
                account.getOwnerName(),
                account.getCurrency(),
                account.getBalance(),
                account.getCreatedAt()
        );
    }

    public TransactionResponse toResponse(Transaction transaction) {
        return new TransactionResponse(
                transaction.getId(),
                transaction.getSourceIban(),
                transaction.getTargetIban(),
                transaction.getAmount(),
                transaction.getCurrency(),
                transaction.getStatus(),
                transaction.getExecutedAt()
        );
    }
}
