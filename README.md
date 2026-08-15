# Banking REST API

A small core-banking service that exposes account and money-transfer operations over a REST interface. It is built around a clean layered design (controller → service → repository) and uses optimistic locking on the account balance so concurrent transfers cannot silently overwrite each other.

## What it does

- Open an account with an owner name, ISO currency code, and an opening balance
- Look up an account by its IBAN
- Move money between two same-currency accounts and record the transaction
- List the transaction history for a given account

Every write goes through a single transactional service method, so a transfer either fully completes (both balances updated, a `COMPLETED` record written) or is rejected before any state changes.

## Tech stack

| Concern            | Choice                          |
|--------------------|---------------------------------|
| Language / runtime | Java 17                         |
| Framework          | Spring Boot 3.3                  |
| Persistence        | Spring Data JPA + H2 (in-memory) |
| Validation         | Jakarta Bean Validation         |
| Testing            | JUnit 5, Mockito, MockMvc       |
| Build              | Maven                           |

H2 keeps the project self-contained for review — swapping in PostgreSQL is a matter of changing the datasource properties and the dependency.

## Running it

```bash
mvn spring-boot:run
```

The service starts on port `8080`. The H2 console is available at `http://localhost:8080/h2-console` (JDBC URL `jdbc:h2:mem:banking`).

## Trying it out

The `requests.http` file contains ready-to-run calls for IntelliJ's HTTP client. A minimal flow:

```bash
# open an account
curl -X POST http://localhost:8080/api/v1/accounts \
  -H "Content-Type: application/json" \
  -d '{"ownerName":"Ada Lovelace","currency":"TRY","initialBalance":1000.00}'

# transfer between two IBANs returned by the calls above
curl -X POST http://localhost:8080/api/v1/accounts/transfers \
  -H "Content-Type: application/json" \
  -d '{"sourceIban":"TR...","targetIban":"TR...","amount":300.00}'
```

## API

| Method | Path                                   | Description                       |
|--------|----------------------------------------|-----------------------------------|
| POST   | `/api/v1/accounts`                     | Open a new account                |
| GET    | `/api/v1/accounts/{iban}`              | Fetch account details             |
| POST   | `/api/v1/accounts/transfers`           | Transfer funds between accounts   |
| GET    | `/api/v1/accounts/{iban}/transactions` | List an account's transactions    |

### Error handling

Errors are returned as RFC 7807 `ProblemDetail` responses:

- `400` — request body fails validation
- `404` — referenced IBAN does not exist
- `422` — currency mismatch or insufficient funds

## Design notes

- **Money is `BigDecimal`.** Floating-point types are never used for balances.
- **Transfers are atomic.** The debit, credit, and transaction record share one `@Transactional` boundary.
- **Optimistic locking.** `Account` carries a `@Version` column so a lost update under concurrency fails fast rather than corrupting a balance.
- **IBANs are generated server-side** and checked for uniqueness before persistence.

## Tests

```bash
mvn test
```

The suite covers the transfer rules (happy path, insufficient funds, currency mismatch, transaction persistence) at the service layer with Mockito, and the HTTP contract (status codes, validation, JSON shape) at the web layer with MockMvc.
