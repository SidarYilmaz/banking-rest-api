#!/usr/bin/env bash
set -e

git init
git branch -M main

git add pom.xml .gitignore
git commit -m "Bootstrap Spring Boot project with Maven"

git add src/main/resources/application.yml src/main/java/com/sidaryilmaz/banking/BankingApplication.java
git commit -m "Add application entry point and H2 datasource config"

git add src/main/java/com/sidaryilmaz/banking/model/
git commit -m "Add Account and Transaction domain entities"

git add src/main/java/com/sidaryilmaz/banking/repository/
git commit -m "Add JPA repositories for accounts and transactions"

git add src/main/java/com/sidaryilmaz/banking/dto/ src/main/java/com/sidaryilmaz/banking/mapper/
git commit -m "Add request/response DTOs and entity mapper"

git add src/main/java/com/sidaryilmaz/banking/service/IbanGenerator.java
git commit -m "Add server-side IBAN generation"

git add src/main/java/com/sidaryilmaz/banking/service/AccountService.java
git commit -m "Implement account creation and transfer logic"

git add src/main/java/com/sidaryilmaz/banking/exception/
git commit -m "Add domain exceptions and RFC 7807 error handling"

git add src/main/java/com/sidaryilmaz/banking/controller/
git commit -m "Expose account and transfer REST endpoints"

git add src/test/java/com/sidaryilmaz/banking/service/
git commit -m "Add service-layer unit tests with Mockito"

git add src/test/java/com/sidaryilmaz/banking/controller/
git commit -m "Add web-layer tests with MockMvc"

git add requests.http README.md
git commit -m "Add HTTP request samples and project documentation"

echo
echo "Done. Now create an empty repo on GitHub and run:"
echo "  git remote add origin https://github.com/<user>/banking-rest-api.git"
echo "  git push -u origin main"
