package iteration_one.api_tests;

import generators.RandomModelGenerator;
import generators.TestUser;
import models.*;
import models.comparison.ModelAssertions;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import requests.skelethon.Endpoint;
import requests.skelethon.requesters.CrudRequester;
import requests.skelethon.requesters.ValidatedCrudRequester;
import specs.RequestSpecs;
import specs.ResponseSpecs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static generators.testdata.ValidTransferAmounts.validTransferAmount;
import static org.assertj.core.api.Assertions.assertThat;
import static specs.ResponseSpecs.PROFILE_UPDATE_SUCCESS;

public class BaseTest {

    //================= Подготовка данных для тестирования ==================
    public static final Long NON_EXISTENT_ACCOUNT_ID = 9999999L;

    protected static Map<String, String> tokens = new HashMap<>();
    protected static Map<String, List<Long>> userAccounts = new HashMap<>();
    protected static Map<String, String> userUsernames = new HashMap<>();
    protected static CrudRequester adminRequester;
    protected static ValidatedCrudRequester<CreateUserResponse> adminCreateUser;
    protected static ValidatedCrudRequester<CustomerAccountsResponse> customerAccountsReader;
    protected SoftAssertions softly;

    @BeforeAll
    public static void init() {
        // Инициализируем requesters
        adminRequester = new CrudRequester(
                RequestSpecs.adminSpec(),
                ResponseSpecs.entityWasCreated(),  // для создания
                Endpoint.ADMIN_USER_CREATE
        );

        adminCreateUser = new ValidatedCrudRequester<>(
                RequestSpecs.adminSpec(),
                ResponseSpecs.entityWasCreated(),
                Endpoint.ADMIN_USER_CREATE
        );

        customerAccountsReader = new ValidatedCrudRequester<>(
                null,  // будет переопределяться под каждого пользователя
                ResponseSpecs.requestReturnsOK(),
                Endpoint.CUSTOMER_ACCOUNTS
        );

        // Создаем пользователей и получаем токены
        tokens.put(TestUser.KATE.getKey(), createUserAndGetToken(TestUser.KATE.createRequest()));
        tokens.put(TestUser.ALEX.getKey(), createUserAndGetToken(TestUser.ALEX.createRequest()));

        // Создаем аккаунты для пользователей в хранилище
        userAccounts.put(TestUser.KATE.getKey(), new ArrayList<>());
        userAccounts.put(TestUser.ALEX.getKey(), new ArrayList<>());

//        userUsernames.put(TestUser.KATE.getKey(), )

        // Создаем через API 2 аккаунта для Kate и 1 аккаунт для Alex
        createUserAccounts(TestUser.KATE.getKey(), 2);
        createUserAccounts(TestUser.ALEX.getKey(), 1);
    }

    //========================= Методы доступа =============================
    protected static String token(String user) {
        return tokens.get(user);
    }

    protected static String getKateToken() {
        return token(TestUser.KATE.getKey());
    }

    protected static String getAlexToken() {
        return token(TestUser.ALEX.getKey());
    }

    protected static void createUserAccounts(String user, int count) {
        for (int i = 0; i < count; i++) {
            Long accountId = createUserAccount(token(user));
            userAccounts.get(user).add(accountId);
        }
    }

    protected static void deleteAllUsers() {
        // Получаем список всех пользователей
        ValidatedCrudRequester<GetAllUsersResponse> getAllUsers = new ValidatedCrudRequester<>(
                RequestSpecs.adminSpec(),
                ResponseSpecs.requestReturnsOK(),
                Endpoint.ADMIN_USERS_GET
        );

        GetAllUsersResponse allUsersResponse = getAllUsers.read();
        List<Customer> allUsers = allUsersResponse.getCustomers();

        // Удаляем тестовых пользователей
        CrudRequester deleteRequester = new CrudRequester(
                RequestSpecs.adminSpec(),
                ResponseSpecs.requestReturnsOK(),
                Endpoint.ADMIN_USER_DELETE
        );

        for (Customer user : allUsers) {
            String username = user.getUsername();
            if (username != null && (username.startsWith("Kate") || username.startsWith("Alex"))) {
                deleteRequester.delete(user.getId());
            }
        }
    }

    //===================== Методы для получения id аккаунта клиента ================
    protected static Long firstAccountId(String user) {
        List<Long> accounts = userAccounts.get(user);
        if (accounts == null || accounts.isEmpty()) {
            throw new IllegalStateException("User " + user + " has no accounts");
        }
        return accounts.get(0);
    }

    protected static Long secondAccountId(String user) {
        List<Long> accounts = userAccounts.get(user);
        if (accounts == null || accounts.size() < 2) {
            throw new IllegalStateException("User " + user + " has no second account");
        }
        return accounts.get(1);
    }

    protected static Long getKateFirstAccountId() {
        return firstAccountId(TestUser.KATE.getKey());
    }

    protected static Long getKateSecondAccountId() {
        return secondAccountId(TestUser.KATE.getKey());
    }

    protected static Long getAlexFirstAccountId() {
        return firstAccountId(TestUser.ALEX.getKey());
    }

    //================== Методы для получения объекта аккаунта клиента ==============
    protected static List<Account> getCustomerAccounts(String user) {
        ValidatedCrudRequester<CustomerAccountsResponse> requester = new ValidatedCrudRequester<>(
                RequestSpecs.authWithTokenSpec(token(user)),
                ResponseSpecs.requestReturnsOK(),
                Endpoint.CUSTOMER_ACCOUNTS
        );
        return requester.read().getAccounts();
    }

    protected static Account getAccountById(String user, Long accountId) {
        return getCustomerAccounts(user).stream()
                .filter(a -> a.getId().equals(accountId))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Account not found: " + accountId));
    }

    protected static Account getFirstAccount(String user) {
        List<Account> accounts = getCustomerAccounts(user);
        if (accounts.isEmpty()) {
            throw new AssertionError("User " + user + " has no accounts");
        }
        return accounts.get(0);
    }

    protected static Account getSecondAccount(String user) {
        List<Account> accounts = getCustomerAccounts(user);
        if (accounts.size() < 2) {
            throw new AssertionError("User " + user + " has no second account");
        }
        return accounts.get(1);
    }

    protected static Account getFirstKateAccount() {
        return getFirstAccount(TestUser.KATE.getKey());
    }

    protected static Account getSecondKateAccount() {
        return getSecondAccount(TestUser.KATE.getKey());
    }

    protected static Account getFirstAlexAccount() {
        return getFirstAccount(TestUser.ALEX.getKey());
    }

    // ================= Методы для получения баланса аккаунта ================
    protected static double getBalance(String user, Long accountId) {
        return getCustomerAccounts(user).stream()
                .filter(a -> a.getId().equals(accountId))
                .findFirst()
                .map(Account::getBalance)
                .orElseThrow(() -> new AssertionError("Account not found: " + accountId));
    }

    protected static double getKateFirstAccountBalance() {
        return getBalance(TestUser.KATE.getKey(), getKateFirstAccountId());
    }

    protected static double getAlexFirstAccountBalance() {
        return getBalance(TestUser.ALEX.getKey(), getAlexFirstAccountId());
    }

    protected static double getKateSecondAccountBalance() {
        return getBalance(TestUser.KATE.getKey(), getKateSecondAccountId());
    }

    // ============= Методы для получения количества транзакций ===============
    protected static int getAccountTransactionsCount(String user, Long accountId) {
        Account account = getCustomerAccounts(user).stream()
                .filter(a -> a.getId().equals(accountId))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Account not found: " + accountId));
        return account.getTransactions() != null ? account.getTransactions().size() : 0;
    }

    protected static int getKateFirstAccountTransactionsCount() {
        return getAccountTransactionsCount(TestUser.KATE.getKey(), getKateFirstAccountId());
    }

    protected static int getAlexFirstAccountTransactionsCount() {
        return getAccountTransactionsCount(TestUser.ALEX.getKey(), getAlexFirstAccountId());
    }

    //============== Методы для получения данных профиля клиента ============
    protected static CustomerProfileResponse getCustomerProfile(String customerName) {
        ValidatedCrudRequester<CustomerProfileResponse> requester = new ValidatedCrudRequester<>(
                RequestSpecs.authWithTokenSpec(token(customerName)),
                ResponseSpecs.requestReturnsOK(),
                Endpoint.CUSTOMER_PROFILE_GET
        );
        return requester.read();
    }

    public static String getCustomerProfileName(String customerName) {
        CustomerProfileResponse profile = getCustomerProfile(customerName);
        return profile != null ? profile.getName() : null;
    }

    public static String getCustomerUsername(String customerName) {
        CustomerProfileResponse profile = getCustomerProfile(customerName);
        return profile != null ? profile.getUsername() : null;
    }

    //========================= Методы создания =============================
    public static String createUserAndGetToken(CreateUserRequest request) {
        // Создаем пользователя
        ValidatedCrudRequester<CreateUserResponse> requester = new ValidatedCrudRequester<>(
                RequestSpecs.adminSpec(),
                ResponseSpecs.entityWasCreated(),
                Endpoint.ADMIN_USER_CREATE
        );
        requester.create(request);

        // Логинимся и получаем токен
        LoginUserRequest loginUserRequest = LoginUserRequest.builder()
                .username(request.getUsername())
                .password(request.getPassword())
                .build();


        CrudRequester loginRequester = new CrudRequester(
                RequestSpecs.unauthSpec(),
                ResponseSpecs.requestReturnsOK(),
                Endpoint.LOGIN
        );

        return loginRequester.create(loginUserRequest)
                .extract()
                .header("Authorization");
    }

    protected static Long createUserAccount(String authToken) {
        ValidatedCrudRequester<CreateAccountResponse> requester = new ValidatedCrudRequester<>(
                RequestSpecs.authWithTokenSpec(authToken),
                ResponseSpecs.entityWasCreated(),
                Endpoint.ACCOUNTS_CREATE
        );
        return requester.create().getId();  // тело запроса пустое
    }

    public static String updateProfileNameToValidRandomValue(TestUser user) {

        String profileName = Stream.generate(() ->
                    RandomModelGenerator.generateWithBuilder(UpdateCustomerProfileRequest.class).getName()
            ).limit(1).findFirst().orElseThrow();


        UpdateCustomerProfileRequest request = UpdateCustomerProfileRequest
                .builder()
                .name(profileName)
                .build();

        UpdateCustomerProfileResponse response = new ValidatedCrudRequester<UpdateCustomerProfileResponse>(
                RequestSpecs.authWithTokenSpec(token(user.getKey())),
                ResponseSpecs.requestReturnsOK(),
                Endpoint.CUSTOMER_PROFILE_UPDATE)
                .update(request);

        ModelAssertions.assertThatModels(request, response).match();

        // Сравниваю message с ожидаемым значением
        assertThat(response.getMessage()).isEqualTo(PROFILE_UPDATE_SUCCESS);

        return profileName;
    }


    public static double transferMoneyFromFirstToSecondUserAccount() {
        double transferAmount = validTransferAmount();

        TransferRequest request = TransferRequest.builder()
                .senderAccountId(getKateFirstAccountId())
                .receiverAccountId(getKateSecondAccountId())
                .amount(transferAmount)
                .build();

        TransferResponse response = new ValidatedCrudRequester<TransferResponse>(
                RequestSpecs.authWithTokenSpec(token(TestUser.KATE.getKey())),
                ResponseSpecs.requestReturnsOK(),
                Endpoint.ACCOUNTS_TRANSFER)
                .create(request);

        ModelAssertions.assertThatModels(request, response).match();

        return transferAmount;
    }

    // Метод repeat вместо цикла
    protected static void repeat(int times, Runnable action) {
        for (int i = 0; i < times; i++) {
            action.run();
        }
    }

    @AfterAll
    public static void afterTests() {
        deleteAllUsers();
    }

    @BeforeEach
    public void setupTest() {
        this.softly = new SoftAssertions();
    }

    @AfterEach
    public void afterTest() {
        softly.assertAll();
    }
}