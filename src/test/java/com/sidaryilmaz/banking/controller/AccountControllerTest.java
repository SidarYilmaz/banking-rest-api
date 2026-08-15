package com.sidaryilmaz.banking.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sidaryilmaz.banking.dto.AccountResponse;
import com.sidaryilmaz.banking.dto.CreateAccountRequest;
import com.sidaryilmaz.banking.dto.TransactionResponse;
import com.sidaryilmaz.banking.dto.TransferRequest;
import com.sidaryilmaz.banking.exception.AccountNotFoundException;
import com.sidaryilmaz.banking.model.TransactionStatus;
import com.sidaryilmaz.banking.service.AccountService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AccountController.class)
class AccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AccountService accountService;

    @Test
    void createAccountReturnsCreated() throws Exception {
        CreateAccountRequest request = new CreateAccountRequest("Ada Lovelace", "TRY", new BigDecimal("1000.00"));
        AccountResponse response = new AccountResponse(1L, "TR001", "Ada Lovelace", "TRY",
                new BigDecimal("1000.00"), Instant.now());
        when(accountService.createAccount(any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.iban").value("TR001"))
                .andExpect(jsonPath("$.ownerName").value("Ada Lovelace"));
    }

    @Test
    void createAccountRejectsInvalidCurrency() throws Exception {
        CreateAccountRequest request = new CreateAccountRequest("Ada Lovelace", "turkish", new BigDecimal("1000.00"));

        mockMvc.perform(post("/api/v1/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getAccountReturnsNotFoundWhenMissing() throws Exception {
        when(accountService.getAccount("TR999")).thenThrow(new AccountNotFoundException("TR999"));

        mockMvc.perform(get("/api/v1/accounts/TR999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void transferReturnsCompletedTransaction() throws Exception {
        TransferRequest request = new TransferRequest("TR001", "TR002", new BigDecimal("200.00"));
        TransactionResponse response = new TransactionResponse(1L, "TR001", "TR002",
                new BigDecimal("200.00"), "TRY", TransactionStatus.COMPLETED, Instant.now());
        when(accountService.transfer(any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/accounts/transfers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"));
    }
}
