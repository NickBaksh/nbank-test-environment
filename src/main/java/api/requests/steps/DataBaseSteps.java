package api.requests.steps;

import api.models.dao_model.AccountDao;
import api.models.dao_model.CustomerDao;
import api.models.dao_model.TransactionDao;
import api.models.dao_model.TransactionType;
import db.Condition;
import db.DBRequest;
import db.RequestType;
import db.Tables;
import lombok.Getter;
import org.assertj.core.api.SoftAssertions;

import java.util.Comparator;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class DataBaseSteps {

    public enum Columns {
        ID("id"),
        ACCOUNT_NUMBER("account_number"),
        CUSTOMER_ID("customer_id"),
        BALANCE("balance"),
        USERNAME("username"),
        NAME("name"),
        ACCOUNT_ID("account_id"),
        TYPE("type");

        @Getter
        private final String columnName;

        Columns(String columnName) {
            this.columnName = columnName;
        }
    }

    // ============================================
    // ACCOUNTS
    // ============================================

    public static AccountDao getAccountById(Long id) {
        return StepLogger.log("Get account from database by ID: " + id, () ->
                DBRequest.<AccountDao>builder()
                        .requestType(RequestType.SELECT)
                        .table(Tables.ACCOUNTS.getTableName())
                        .where(Condition.equalTo(Columns.ID.getColumnName(), id))
                        .build()
                        .executeOne(AccountDao.rowMapper)
        );
    }

    public static AccountDao getAccountByAccountNumber(String accountNumber) {
        return StepLogger.log("Get account from database by account number: " + accountNumber, () ->
                DBRequest.<AccountDao>builder()
                        .requestType(RequestType.SELECT)
                        .table(Tables.ACCOUNTS.getTableName())
                        .where(Condition.equalTo(Columns.ACCOUNT_NUMBER.getColumnName(), accountNumber))
                        .build()
                        .executeOne(AccountDao.rowMapper)
        );
    }

    public static List<AccountDao> getAccountsByCustomerId(Long customerId) {
        return StepLogger.log("Get accounts from database by customer ID: " + customerId, () ->
                DBRequest.<AccountDao>builder()
                        .requestType(RequestType.SELECT)
                        .table(Tables.ACCOUNTS.getTableName())
                        .where(Condition.equalTo(Columns.CUSTOMER_ID.getColumnName(), customerId))
                        .build()
                        .execute(AccountDao.rowMapper)
        );
    }

    public static List<AccountDao> getAllAccounts() {
        return StepLogger.log("Get all accounts from database", () ->
                DBRequest.<AccountDao>builder()
                        .requestType(RequestType.SELECT)
                        .table(Tables.ACCOUNTS.getTableName())
                        .build()
                        .execute(AccountDao.rowMapper)
        );
    }

    public static int updateAccountBalance(Long id, Double newBalance) {
        return StepLogger.log("Update account balance in database, ID: " + id, () ->
                DBRequest.<AccountDao>builder()
                        .requestType(RequestType.UPDATE)
                        .table(Tables.ACCOUNTS.getTableName())
                        .columns(Columns.BALANCE.getColumnName())
                        .values(newBalance)
                        .where(Condition.equalTo(Columns.ID.getColumnName(), id))
                        .build()
                        .executeUpdate()
        );
    }

    public static int deleteAccountById(Long id) {
        return StepLogger.log("Delete account from database by ID: " + id, () ->
                DBRequest.<AccountDao>builder()
                        .requestType(RequestType.DELETE)
                        .table(Tables.ACCOUNTS.getTableName())
                        .where(Condition.equalTo(Columns.ID.getColumnName(), id))
                        .build()
                        .executeUpdate()
        );
    }

    // ============================================
    // CUSTOMERS
    // ============================================

    public static CustomerDao getCustomerById(Long id) {
        return StepLogger.log("Get customer from database by ID: " + id, () ->
                DBRequest.<CustomerDao>builder()
                        .requestType(RequestType.SELECT)
                        .table(Tables.CUSTOMERS.getTableName())
                        .where(Condition.equalTo(Columns.ID.getColumnName(), id))
                        .build()
                        .executeOne(CustomerDao.rowMapper)
        );
    }

    public static CustomerDao getCustomerByUsername(String username) {
        return StepLogger.log("Get customer from database by username: " + username, () ->
                DBRequest.<CustomerDao>builder()
                        .requestType(RequestType.SELECT)
                        .table(Tables.CUSTOMERS.getTableName())
                        .where(Condition.equalTo(Columns.USERNAME.getColumnName(), username))
                        .build()
                        .executeOne(CustomerDao.rowMapper)
        );
    }

    public static List<CustomerDao> getAllCustomers() {
        return StepLogger.log("Get all customers from database", () ->
                DBRequest.<CustomerDao>builder()
                        .requestType(RequestType.SELECT)
                        .table(Tables.CUSTOMERS.getTableName())
                        .build()
                        .execute(CustomerDao.rowMapper)
        );
    }

    public static int updateCustomerName(Long id, String newName) {
        return StepLogger.log("Update customer name in database, ID: " + id, () ->
                DBRequest.<CustomerDao>builder()
                        .requestType(RequestType.UPDATE)
                        .table(Tables.CUSTOMERS.getTableName())
                        .columns(Columns.NAME.getColumnName())
                        .values(newName)
                        .where(Condition.equalTo(Columns.ID.getColumnName(), id))
                        .build()
                        .executeUpdate()
        );
    }

    public static int deleteCustomerById(Long id) {
        return StepLogger.log("Delete customer from database by ID: " + id, () ->
                DBRequest.<CustomerDao>builder()
                        .requestType(RequestType.DELETE)
                        .table(Tables.CUSTOMERS.getTableName())
                        .where(Condition.equalTo(Columns.ID.getColumnName(), id))
                        .build()
                        .executeUpdate()
        );
    }

    // ============================================
    // TRANSACTIONS
    // ============================================

    public static TransactionDao getTransactionById(Long id) {
        return StepLogger.log("Get transaction from database by ID: " + id, () ->
                DBRequest.<TransactionDao>builder()
                        .requestType(RequestType.SELECT)
                        .table(Tables.TRANSACTIONS.getTableName())
                        .where(Condition.equalTo(Columns.ID.getColumnName(), id))
                        .build()
                        .executeOne(TransactionDao.rowMapper)
        );
    }

    public static List<TransactionDao> getTransactionsByAccountId(Long accountId) {
        return StepLogger.log("Get transactions from database by account ID: " + accountId, () ->
                DBRequest.<TransactionDao>builder()
                        .requestType(RequestType.SELECT)
                        .table(Tables.TRANSACTIONS.getTableName())
                        .where(Condition.equalTo(Columns.ACCOUNT_ID.getColumnName(), accountId))
                        .build()
                        .execute(TransactionDao.rowMapper)
        );
    }

    public static List<TransactionDao> getTransactionsByType(TransactionType type) {
        return StepLogger.log("Get transactions from database by type: " + type, () ->
                DBRequest.<TransactionDao>builder()
                        .requestType(RequestType.SELECT)
                        .table(Tables.TRANSACTIONS.getTableName())
                        .where(Condition.equalTo(Columns.TYPE.getColumnName(), type.name()))
                        .build()
                        .execute(TransactionDao.rowMapper)
        );
    }

    public static List<TransactionDao> getAllTransactions() {
        return StepLogger.log("Get all transactions from database", () ->
                DBRequest.<TransactionDao>builder()
                        .requestType(RequestType.SELECT)
                        .table(Tables.TRANSACTIONS.getTableName())
                        .build()
                        .execute(TransactionDao.rowMapper)
        );
    }

    public static int deleteTransactionById(Long id) {
        return StepLogger.log("Delete transaction from database by ID: " + id, () ->
                DBRequest.<TransactionDao>builder()
                        .requestType(RequestType.DELETE)
                        .table(Tables.TRANSACTIONS.getTableName())
                        .where(Condition.equalTo(Columns.ID.getColumnName(), id))
                        .build()
                        .executeUpdate()
        );
    }

    // ============================================
    // ПРОВЕРКИ (VERIFICATIONS) — DEPOSIT
    // ============================================

    public static void verifyDepositSaved(SoftAssertions softly, Long accountId,
                                          double expectedBalance, double expectedDepositAmount) {
        StepLogger.log("Verify deposit is correctly saved in database, account ID: " + accountId, () -> {
            AccountDao accountFromDb = getAccountById(accountId);
            softly.assertThat(accountFromDb.getBalance())
                    .as("Account balance in database should equal " + expectedBalance)
                    .isEqualTo(expectedBalance);

            TransactionDao lastTransaction = getLastTransactionForAccount(accountId);

            softly.assertThat(lastTransaction.getAmount())
                    .as("Last transaction amount should equal deposit")
                    .isEqualTo(expectedDepositAmount);

            softly.assertThat(lastTransaction.getType())
                    .as("Last transaction type should be DEPOSIT")
                    .isEqualTo(TransactionType.DEPOSIT);
            return null;
        });
    }

    public static void verifyAccountUnchanged(SoftAssertions softly, Long accountId,
                                              double expectedBalance, int expectedTransactionsCount) {
        StepLogger.log("Verify account state is unchanged in database, ID: " + accountId, () -> {
            AccountDao accountFromDb = getAccountById(accountId);
            softly.assertThat(accountFromDb.getBalance())
                    .as("Account balance in database should not change")
                    .isEqualTo(expectedBalance);

            List<TransactionDao> transactionsFromDb = getTransactionsByAccountId(accountId);
            softly.assertThat(transactionsFromDb)
                    .as("Transactions count in database should not change")
                    .hasSize(expectedTransactionsCount);
            return null;
        });
    }

    // ============================================
    // ПРОВЕРКИ (VERIFICATIONS) — TRANSFER
    // ============================================

    /**
     * Проверить, что перевод между разными аккаунтами корректно сохранился в БД:
     * баланс обновился у обоих, появились связанные транзакции TRANSFER_OUT / TRANSFER_IN
     */
    public static void verifyTransferSaved(SoftAssertions softly,
                                           Long senderAccountId, Long receiverAccountId,
                                           double expectedSenderBalance, double expectedReceiverBalance,
                                           double amount) {
        StepLogger.log("Verify transfer is correctly saved in database: "
                + senderAccountId + " -> " + receiverAccountId, () -> {

            AccountDao senderFromDb = getAccountById(senderAccountId);
            AccountDao receiverFromDb = getAccountById(receiverAccountId);

            softly.assertThat(senderFromDb.getBalance())
                    .as("Sender account balance in database should equal " + expectedSenderBalance)
                    .isEqualTo(expectedSenderBalance);

            softly.assertThat(receiverFromDb.getBalance())
                    .as("Receiver account balance in database should equal " + expectedReceiverBalance)
                    .isEqualTo(expectedReceiverBalance);

            TransactionDao lastSenderTransaction = getLastTransactionForAccount(senderAccountId);
            TransactionDao lastReceiverTransaction = getLastTransactionForAccount(receiverAccountId);

            softly.assertThat(lastSenderTransaction.getType())
                    .as("Sender's last transaction type should be TRANSFER_OUT")
                    .isEqualTo(TransactionType.TRANSFER_OUT);

            softly.assertThat(lastSenderTransaction.getAmount())
                    .as("Sender's last transaction amount should equal transfer amount")
                    .isEqualTo(amount);

            softly.assertThat(lastSenderTransaction.getRelatedAccountId())
                    .as("Sender's transaction should reference receiver account")
                    .isEqualTo(receiverAccountId);

            softly.assertThat(lastReceiverTransaction.getType())
                    .as("Receiver's last transaction type should be TRANSFER_IN")
                    .isEqualTo(TransactionType.TRANSFER_IN);

            softly.assertThat(lastReceiverTransaction.getAmount())
                    .as("Receiver's last transaction amount should equal transfer amount")
                    .isEqualTo(amount);

            softly.assertThat(lastReceiverTransaction.getRelatedAccountId())
                    .as("Receiver's transaction should reference sender account")
                    .isEqualTo(senderAccountId);

            return null;
        });
    }

    /**
     * Проверить перевод на тот же самый аккаунт: баланс не меняется,
     * но создаются 2 транзакции (TRANSFER_OUT и TRANSFER_IN) с тем же account_id
     */
    public static void verifySameAccountTransferSaved(SoftAssertions softly, Long accountId,
                                                      double expectedBalance, double amount) {
        StepLogger.log("Verify same-account transfer is correctly saved in database, account ID: " + accountId, () -> {
            AccountDao accountFromDb = getAccountById(accountId);
            softly.assertThat(accountFromDb.getBalance())
                    .as("Account balance in database should not change")
                    .isEqualTo(expectedBalance);

            List<TransactionDao> transactions = getTransactionsByAccountId(accountId);
            List<TransactionDao> lastTwo = transactions.stream()
                    .sorted(Comparator.comparing(TransactionDao::getId).reversed())
                    .limit(2)
                    .toList();

            softly.assertThat(lastTwo)
                    .as("Two new transactions should be created for same-account transfer")
                    .hasSize(2);

            boolean hasOut = lastTwo.stream().anyMatch(t -> t.getType() == TransactionType.TRANSFER_OUT
                    && t.getAmount().equals(amount));
            boolean hasIn = lastTwo.stream().anyMatch(t -> t.getType() == TransactionType.TRANSFER_IN
                    && t.getAmount().equals(amount));

            softly.assertThat(hasOut).as("TRANSFER_OUT transaction should exist").isTrue();
            softly.assertThat(hasIn).as("TRANSFER_IN transaction should exist").isTrue();

            return null;
        });
    }

    /**
     * Получить последнюю (по ID) транзакцию для аккаунта
     */
    private static TransactionDao getLastTransactionForAccount(Long accountId) {
        List<TransactionDao> transactions = getTransactionsByAccountId(accountId);
        return transactions.stream()
                .max(Comparator.comparing(TransactionDao::getId))
                .orElseThrow(() -> new AssertionError("No transactions found for account " + accountId));
    }

    // ============================================
    // ПРОВЕРКИ (VERIFICATIONS) — CUSTOMER PROFILE
    // ============================================

    public static void verifyCustomerProfileName(SoftAssertions softly, String username, String expectedName) {
        StepLogger.log("Verify customer profile name in database, username: " + username, () -> {
            CustomerDao customerFromDb = getCustomerByUsername(username);
            softly.assertThat(customerFromDb.getName())
                    .as("Customer name in database should equal " + expectedName)
                    .isEqualTo(expectedName);
            return null;
        });
    }

    public static void verifyCustomerProfileNameUnchanged(SoftAssertions softly, String username, String expectedName) {
        StepLogger.log("Verify customer profile name is unchanged in database, username: " + username, () -> {
            CustomerDao customerFromDb = getCustomerByUsername(username);
            softly.assertThat(customerFromDb.getName())
                    .as("Customer name in database should not change")
                    .isEqualTo(expectedName);
            return null;
        });
    }

    // ============================================
    // ПРОВЕРКИ (VERIFICATIONS) — NOT EXISTS
    // ============================================

    public static void verifyAccountNotExists(String accountNumber) {
        StepLogger.log("Verify account does NOT exist in database: " + accountNumber, () -> {
            AccountDao accountFromDb = getAccountByAccountNumber(accountNumber);
            assertThat(accountFromDb)
                    .as("Account should not exist in database")
                    .isNull();
            return null;
        });
    }

    public static void verifyCustomerNotExists(String username) {
        StepLogger.log("Verify customer does NOT exist in database: " + username, () -> {
            CustomerDao customerFromDb = getCustomerByUsername(username);
            assertThat(customerFromDb)
                    .as("Customer should not exist in database")
                    .isNull();
            return null;
        });
    }
}