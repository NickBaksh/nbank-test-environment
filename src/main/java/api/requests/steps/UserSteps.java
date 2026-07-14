package api.requests.steps;

import api.generators.RandomModelGenerator;
import api.generators.testdata.InvalidNameCase;
import api.models.comparison.ModelAssertions;
import api.models.dto_model.*;
import api.requests.skelethon.Endpoint;
import api.requests.skelethon.requesters.CrudRequester;
import api.requests.skelethon.requesters.ValidatedCrudRequester;
import api.specs.RequestSpecs;
import api.specs.ResponseSpecs;
import org.assertj.core.api.Assertions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

import static api.generators.testdata.DataProviders.BALANCE_5000;
import static api.generators.testdata.ValidTransferAmounts.validTransferAmount;
import static org.assertj.core.api.Assertions.assertThat;

public class UserSteps {

    // Хранилище для созданных в рамках теста пользователей
    private static final ThreadLocal<List<TestUserContext>> testUsers = ThreadLocal.withInitial(ArrayList::new);

    private static final ThreadLocal<Boolean> isCleaningUp = ThreadLocal.withInitial(() -> false);

    // ==================== Создание пользователей ====================

    /**
     * Сгенерировать запрос на создание пользователя
     */
    private static CreateUserRequest generateUserRequest(String prefix, String role) {
        CreateUserRequest request = RandomModelGenerator.generateWithBuilder(CreateUserRequest.class);
        request.setUsername(prefix + "_" + request.getUsername());
        request.setRole(role);
        return request;
    }

    /**
     * Создать пользователя с кастомными данными
     */
    public static TestUserContext createUser(CreateUserRequest request) {
        // Создаём пользователя через API
        ValidatedCrudRequester<CreateUserResponse> requester = new ValidatedCrudRequester<>(
                RequestSpecs.adminSpec(),
                ResponseSpecs.entityWasCreated(),
                Endpoint.ADMIN_USER_CREATE
        );
        CreateUserResponse response = requester.create(request);

        // Получаем токен
        String token = loginAndGetToken(request.getUsername(), request.getPassword());

        // Создаём контекст пользователя
        TestUserContext context = TestUserContext.builder()
                .username(request.getUsername())
                .password(request.getPassword())
                .token(token)
                .userId(response.getId())
                .role(request.getRole())
                .accounts(new ConcurrentHashMap<>())
                .build();

        // Сохраняем в хранилище текущего теста
        testUsers.get().add(context);

        System.out.println("✅ User created in thread " + Thread.currentThread().getId() +
                ": " + context.getDisplayName());
        return context;
    }

    /**
     * Логин и получение токена
     */
    private static String loginAndGetToken(String username, String password) {
        CrudRequester loginRequester = new CrudRequester(
                RequestSpecs.unauthSpec(),
                ResponseSpecs.requestReturnsOK(),
                Endpoint.LOGIN
        );
        return loginRequester
                .create(LoginUserRequest.builder()
                        .username(username)
                        .password(password)
                        .build())
                .extract()
                .header("Authorization");
    }

    // ==================== Создание аккаунтов ====================

    /**
     * Создать аккаунт для пользователя
     */
    /**
     * Создать аккаунт для пользователя (возвращает ID)
     */
    public static Long createAccount(TestUserContext userContext) {
        ValidatedCrudRequester<CreateAccountResponse> requester = new ValidatedCrudRequester<>(
                RequestSpecs.authWithTokenSpec(userContext.getToken()),
                ResponseSpecs.entityWasCreated(),
                Endpoint.ACCOUNTS_CREATE
        );
        CreateAccountResponse response = requester.create();

        Long accountId = response.getId();
        String accountNumber = response.getAccountNumber();

        // Сохраняем ID и номер аккаунта
        userContext.addAccount(accountId, accountNumber);

        System.out.println("✅ Account created for " + userContext.getUsername() +
                ": " + accountId + " (Number: " + accountNumber + ")");
        return accountId;
    }

    /**
     * Создать несколько аккаунтов для пользователя
     */
    public static List<Long> createAccounts(TestUserContext userContext, int count) {
        List<Long> accountIds = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            accountIds.add(createAccount(userContext));
        }
        return accountIds;
    }

    // ==================== Комбинированные методы ====================

    /**
     * Создать пользователя с аккаунтами
     */
    public static TestUserContext createUserWithAccounts(String prefix, String role, int accountCount) {
        TestUserContext user = createUser(generateUserRequest(prefix, role));
        createAccounts(user, accountCount);
        return user;
    }

    // ==================== Новые методы для работы с accountNumber ====================

    /**
     * Получить номер аккаунта пользователя по ID
     */
    public static String getAccountNumber(TestUserContext userContext, Long accountId) {
        String accountNumber = userContext.getAccountNumber(accountId);
        if (accountNumber == null) {
            // Если номер не сохранен, получаем его из API
            accountNumber = fetchAccountNumberFromApi(userContext, accountId);
            userContext.addAccount(accountId, accountNumber);
        }
        return accountNumber;
    }

    /**
     * Получить номер первого аккаунта пользователя
     */
    public static String getFirstAccountNumber(TestUserContext userContext) {
        return getAccountNumber(userContext, userContext.getFirstAccountId());
    }

    /**
     * Получить номер второго аккаунта пользователя
     */
    public static String getSecondAccountNumber(TestUserContext userContext) {
        return getAccountNumber(userContext, userContext.getSecondAccountId());
    }

    /**
     * Получить номер аккаунта из API (если не сохранен)
     */
    private static String fetchAccountNumberFromApi(TestUserContext userContext, Long accountId) {
        List<Account> accounts = getAllAccounts(userContext);
        return accounts.stream()
                .filter(a -> a.getId().equals(accountId))
                .findFirst()
                .map(Account::getAccountNumber)
                .orElseThrow(() -> new AssertionError("Account not found: " + accountId));
    }

    // ==================== Методы доступа к данным ====================

    /**
     * Получить баланс аккаунта пользователя
     */
    public static double getAccountBalance(TestUserContext userContext, Long accountId) {
        CustomerAccountsResponse response = new ValidatedCrudRequester<CustomerAccountsResponse>(
                RequestSpecs.authWithTokenSpec(userContext.getToken()),
                ResponseSpecs.requestReturnsOK(),
                Endpoint.CUSTOMER_ACCOUNTS
        ).read();

        double balance = response.getAccounts().stream()
                .filter(a -> a.getId().equals(accountId))
                .findFirst()
                .map(Account::getBalance)
                .orElseThrow(() -> new AssertionError("Account not found: " + accountId));

        // Округляем до 2 знаков после запятой
        return Math.round(balance * 100.0) / 100.0;
    }

    /**
     * Получить баланс первого аккаунта
     */
    public static double getFirstAccountBalance(TestUserContext userContext) {
        return getAccountBalance(userContext, userContext.getFirstAccountId());
    }

    /**
     * Получить баланс второго аккаунта
     */
    public static double getSecondAccountBalance(TestUserContext userContext) {
        return getAccountBalance(userContext, userContext.getSecondAccountId());
    }

    /**
     * Получить профиль пользователя
     */
    public static CustomerProfileResponse getProfile(TestUserContext userContext) {
        return new ValidatedCrudRequester<CustomerProfileResponse>(
                RequestSpecs.authWithTokenSpec(userContext.getToken()),
                ResponseSpecs.requestReturnsOK(),
                Endpoint.CUSTOMER_PROFILE_GET
        ).read();
    }

    /**
     * Получить имя профиля
     */
    public static String getProfileName(TestUserContext userContext) {
        CustomerProfileResponse profile = getProfile(userContext);
        return profile != null ? profile.getName() : null;
    }

    /**
     * Сгенерировать валидное имя профиля
     */
    public static String generateValidProfileName() {
        return RandomModelGenerator.generateWithBuilder(UpdateCustomerProfileRequest.class).getName();
    }

    /**
     * Сгенерировать невалидное имя профиля
     */
    public static String generateInvalidProfileName() {
        InvalidNameCase[] cases = InvalidNameCase.values();

        List<InvalidNameCase> invalidNameCases = Arrays.stream(cases)
                .filter(c -> c != InvalidNameCase.EMPTY && c != InvalidNameCase.SPACE)
                .toList();

        int randomIndex = ThreadLocalRandom.current().nextInt(invalidNameCases.size());
        String invalidName = invalidNameCases.get(randomIndex).generate();

        Assertions.assertThat(invalidName).isNotNull().isNotEmpty();
        return invalidName;
    }


    /**
     * Обновить имя профиля
     */
    public static String updateProfileNameToRandomName(TestUserContext userContext) {
        String generatedProfileName = generateValidProfileName();

        UpdateCustomerProfileRequest request = UpdateCustomerProfileRequest.builder()
                .name(generatedProfileName)
                .build();

        new ValidatedCrudRequester<UpdateCustomerProfileResponse>(
                RequestSpecs.authWithTokenSpec(userContext.getToken()),
                ResponseSpecs.requestReturnsOK(),
                Endpoint.CUSTOMER_PROFILE_UPDATE
        ).update(request);

        return generatedProfileName;
    }

    /**
     * Получить все аккаунты пользователя
     */
    public static List<Account> getAllAccounts(TestUserContext userContext) {
        // Получаем массив аккаунтов
        ValidatedCrudRequester<CustomerAccountsResponse> requester = new ValidatedCrudRequester<>(
                RequestSpecs.authWithTokenSpec(userContext.getToken()),
                ResponseSpecs.requestReturnsOK(),
                Endpoint.CUSTOMER_ACCOUNTS
        );
        return requester.read().getAccounts();
    }

    /**
     * Получить все транзакции аккаунта
     */
    public static List<Transaction> getAccountTransactions(TestUserContext user, Long accountId) {
        return getAllAccounts(user).stream()
                .filter(a -> a.getId().equals(accountId))
                .findFirst()
                .map(Account::getTransactions)
                .orElseThrow(() -> new AssertionError("Account not found: " + accountId));
    }

    /**
     * Получить количество транзакций аккаунта
     */
    public static int getAccountTransactionsCount(TestUserContext user, Long accountId) {
        List<Transaction> transactions = getAccountTransactions(user, accountId);
        return transactions != null ? transactions.size() : 0;
    }

    /**
     * Получить количество транзакций первого аккаунта
     */
    public static int getFirstAccountTransactionsCount(TestUserContext user) {
        List<Transaction> transactions = getAccountTransactions(user, user.getFirstAccountId());
        return transactions != null ? transactions.size() : 0;
    }

    /**
     * Получить количество транзакций второго аккаунта
     */
    public static int getSecondAccountTransactionsCount(TestUserContext user) {
        List<Transaction> transactions = getAccountTransactions(user, user.getSecondAccountId());
        return transactions != null ? transactions.size() : 0;
    }

    // ==================== Очистка ====================

    /**
     * Получить всех пользователей, созданных в текущем тесте
     */
    public static List<TestUserContext> getCurrentTestUsers() {
        return new ArrayList<>(testUsers.get());
    }

    /**
     * Очистить только одного пользователя, созданного в тесте
     */
    public static void cleanupTestUser(TestUserContext user) {
        if (user == null || isCleaningUp.get()) {
            return;
        }

        try {
            CrudRequester deleteRequester = new CrudRequester(
                    RequestSpecs.adminSpec(),
                    ResponseSpecs.requestReturnsOK(),
                    Endpoint.ADMIN_USER_DELETE
            );

            deleteRequester.delete(user.getUserId());
            System.out.println("🗑️ User deleted in thread " + Thread.currentThread().getId() +
                    ": " + user.getDisplayName());

            // Удаляем из хранилища текущего потока
            testUsers.get().removeIf(u -> u.getUserId().equals(user.getUserId()));

        } catch (Exception e) {
            System.err.println("Failed to delete user: " + user.getUsername() + " - " + e.getMessage());
        }
    }


    /**
     * Очистить всех пользователей, созданных в текущем тесте
     */
    public static void cleanupTestUsers() {
        // Защита от рекурсивного вызова
        if (isCleaningUp.get()) {
            return;
        }

        try {
            isCleaningUp.set(true);
            Long threadId = Thread.currentThread().getId();

            CrudRequester deleteRequester = new CrudRequester(
                    RequestSpecs.adminSpec(),
                    ResponseSpecs.requestReturnsOK(),
                    Endpoint.ADMIN_USER_DELETE
            );

            // Сохраняем список ID пользователей для последующей проверки
            List<Long> deletedUserIds = new ArrayList<>();
            int deletedCount = 0;

            // Очищаем всех пользователей текущего потока
            for (TestUserContext context : testUsers.get()) {
                try {
                    deleteRequester.delete(context.getUserId());
                    System.out.println("User deleted from thread " + threadId + ": " + context.getDisplayName());
                    deletedUserIds.add(context.getUserId());
                    deletedCount++;
                } catch (Exception e) {
                    System.err.println("Failed to delete user: " + context.getUsername() + " - " + e.getMessage());
                }
            }

            // Проверяем, что пользователи действительно удалились
            if (!deletedUserIds.isEmpty()) {
                verifyUsersAreDeleted(deletedUserIds);
            }

            System.out.println("Total deleted in thread " + threadId + ": " + deletedCount + " users");

            // Очищаем хранилище текущего потока
            testUsers.get().clear();

        } finally {
            isCleaningUp.remove();
        }
    }

    /**
     * Проверить, что пользователи удалены
     */
    private static void verifyUsersAreDeleted(List<Long> deletedUserIds) {
        // Получаем всех пользователей после удаления
        ValidatedCrudRequester<GetAllUsersResponse> getAllUsers = new ValidatedCrudRequester<>(
                RequestSpecs.adminSpec(),
                ResponseSpecs.requestReturnsOK(),
                Endpoint.ADMIN_USERS_GET
        );

        GetAllUsersResponse response = getAllUsers.read();
        List<Customer> remainingUsers = response.getCustomers();

        // Проверяем, что удалённых пользователей нет в списке
        for (Long userId : deletedUserIds) {
            boolean userExists = remainingUsers.stream()
                    .anyMatch(user -> user.getId().equals(userId));

            if (userExists) {
                System.err.println("⚠️ User " + userId + " still exists after deletion!");
            } else {
                System.out.println("✅ Verified: User " + userId + " successfully removed from system");
            }

            assertThat(userExists).as("User " + userId + " should be deleted").isFalse();
        }
    }

    /**
     * Проверить, что пользователь существует
     */
    public static boolean userExists(TestUserContext userContext) {
        try {
            getProfile(userContext);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Пополнить баланс аккаунтов перед тестом
     */
    public static void setUpBalance(TestUserContext userContext) {
        long firstAccountId = userContext.getFirstAccountId();
        long secondAccountId = userContext.getSecondAccountId();

        String token = userContext.getToken();

        //Пополняем счёт на 20000 перед каждым тестом перевода
        //Вызов несколько раз, т.к. есть ограничение на пополнение в 5000
        repeat(4, () -> {
            DepositRequest request = DepositRequest.builder()
                    .id(firstAccountId)
                    .balance(BALANCE_5000)
                    .build();

            new CrudRequester(
                    RequestSpecs.authWithTokenSpec(token),
                    ResponseSpecs.requestReturnsOK(),
                    Endpoint.ACCOUNTS_DEPOSIT)
                    .create(request);
        });

        DepositRequest request = DepositRequest.builder()
                .id(secondAccountId)
                .balance(BALANCE_5000)
                .build();

        new CrudRequester(
                RequestSpecs.authWithTokenSpec(token),
                ResponseSpecs.requestReturnsOK(),
                Endpoint.ACCOUNTS_DEPOSIT)
                .create(request);
    }

    protected static void repeat(int times, Runnable action) {
        for (int i = 0; i < times; i++) {
            action.run();
        }
    }

    public static double transferMoneyFromFirstToSecondUserAccount(TestUserContext context) {
        double transferAmount = validTransferAmount();
        String token = context.getToken();

        TransferRequest request = TransferRequest.builder()
                .senderAccountId(context.getFirstAccountId())
                .receiverAccountId(context.getSecondAccountId())
                .amount(transferAmount)
                .build();

        TransferResponse response = new ValidatedCrudRequester<TransferResponse>(
                RequestSpecs.authWithTokenSpec(token),
                ResponseSpecs.requestReturnsOK(),
                Endpoint.ACCOUNTS_TRANSFER)
                .create(request);

        ModelAssertions.assertThatModels(request, response).match();
        return transferAmount;
    }
}