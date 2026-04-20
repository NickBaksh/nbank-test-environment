package iteration_one;

import generators.RandomData;
import models.*;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import requests.requesters.delete.DeleteUserRequester;
import requests.requesters.get.GetAllUsersRequester;
import requests.requesters.get.GetCustomerAccountsRequester;
import requests.requesters.get.GetCustomerProfileRequester;
import requests.requesters.post.CreateUserAccountRequester;
import requests.requesters.post.CreateUserRequester;
import requests.requesters.post.LoginUserRequester;
import specs.RequestSpecs;
import specs.ResponseSpecs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BaseTest {

    //================= Подготовка данных для тестирования ==================
    public static final String USER_KATE = "kate";
    public static final String USER_ALEX = "alex";
    public static final int NON_EXISTENT_ACCOUNT_ID = 9999999;

    protected static Map<String, String> tokens = new HashMap<>();
    protected static Map<String, List<Integer>> userAccounts = new HashMap<>();

    protected SoftAssertions softly;

    @BeforeAll
    public static void init() {

        // Создаем пользователей и получаем токены
        tokens.put(USER_KATE, createUserAndGetToken("Kate_" + RandomData.getUsername(), RandomData.getPassword()));
        tokens.put(USER_ALEX, createUserAndGetToken("Alex_" + RandomData.getUsername(), RandomData.getPassword()));

        // Создаем аккаунты для пользователей в хранилище
        userAccounts.put(USER_KATE, new ArrayList<>());
        userAccounts.put(USER_ALEX, new ArrayList<>());

        // Создаем через API 2 аккаунта для Kate и 1 аккаунт для Alex
        createUserAccounts(USER_KATE, 2);
        createUserAccounts(USER_ALEX, 1);
    }

    //========================= Методы доступа =============================
    protected static String token(String user) {
        return tokens.get(user);
    }

    protected static String getKateToken() {
        return token(USER_KATE);
    }

    protected static String getAlexToken() {
        return token(USER_ALEX);
    }

    protected static void createUserAccounts(String user, int count) {
        for (int i = 0; i < count; i++) {
            Integer accountId = createUserAccount(token(user));
            userAccounts.get(user).add(accountId);
        }
    }

    protected static void deleteAllUsers() {
        List<Customer> allUsers = new GetAllUsersRequester(
                RequestSpecs.adminSpec(),
                ResponseSpecs.requestReturnsOK())
                .get()
                .extract()
                .jsonPath()
                .getList(".", Customer.class);

        for (Customer user : allUsers) {
            String username = user.getUsername();
            if (username != null && (username.startsWith("Kate") || username.startsWith("Alex"))) {
                new DeleteUserRequester(
                        RequestSpecs.adminSpec(),
                        ResponseSpecs.requestReturnsOK(),
                        user.getId())
                        .delete();
            }
        }
    }

    //===================== Методы для получения id аккаунта клиента ================
    protected static Integer firstAccountId(String user) {
        return userAccounts.get(user).get(0);
    }

    protected static Integer secondAccountId(String user) {
        List<Integer> accounts = userAccounts.get(user);
        if (accounts == null || accounts.size() < 2) {
            throw new IllegalStateException("User " + user + " has no second account");
        }
        return accounts.get(1);
    }

    protected static Integer getKateFirstAccountId() {
        return firstAccountId(USER_KATE);
    }

    protected static Integer getKateSecondAccountId() {
        return secondAccountId(USER_KATE);
    }

    protected static Integer getAlexFirstAccountId() {
        return firstAccountId(USER_ALEX);
    }

    //================== Методы для получения объекта аккаунта клиента ==============
    protected static List<Account> getCustomerAccounts(String user) {
        return new GetCustomerAccountsRequester(
                RequestSpecs.authWithTokenSpec(token(user)),
                ResponseSpecs.requestReturnsOK())
                .get()
                .extract()
                .jsonPath()
                .getList(".", Account.class);
    }

    protected static Account getAccountById(String user, int accountId) {
        return getCustomerAccounts(user).stream()
                .filter(a -> a.getId().equals(accountId))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Account not found: " + accountId));
    }

    protected static Account getFirstAccount(String user) {
        List<Account> accounts = getCustomerAccounts(user);
        return accounts.get(0);
    }

    protected static Account getSecondAccount(String user) {
        List<Account> accounts = getCustomerAccounts(user);
        return accounts.get(1);
    }

    protected static Account getFirstKateAccount() {
        return getFirstAccount(USER_KATE);
    }

    protected static Account getSecondKateAccount() {
        return getSecondAccount(USER_KATE);
    }

    protected static Account getFirstAlexAccount() {
        return getFirstAccount(USER_ALEX);
    }

    // ================= Методы для получения баланса аккаунта ================
    protected static double getBalance(String user, int accountId) {
        return getCustomerAccounts(user).stream()
                .filter(a -> a.getId().equals(accountId))
                .findFirst()
                .map(Account::getBalance)
                .orElseThrow(() -> new AssertionError("Account not found: " + accountId));
    }

    protected static double getKateFirstAccountBalance() {
        return getBalance(USER_KATE, getKateFirstAccountId());
    }

    protected static double getAlexFirstAccountBalance() {
        return getBalance(USER_ALEX, getAlexFirstAccountId());
    }

    protected static double getKateSecondAccountBalance() {
        return getBalance(USER_KATE, getKateSecondAccountId());
    }

    // ============= Методы для получения количества транзакций на аккаунте ===============
    protected static int getKateFirstAccountTransactionsCount() {
        int accountId = getKateFirstAccountId();
        Account account = getCustomerAccounts(USER_KATE).stream()
                .filter(a -> a.getId().equals(accountId))
                .findFirst()
                .orElseThrow();
        return account.getTransactions() != null ? account.getTransactions().size() : 0;
    }

    protected static int getAlexFirstAccountTransactionsCount() {
        int accountId = getAlexFirstAccountId();
        Account account = getCustomerAccounts(USER_ALEX).stream()
                .filter(a -> a.getId().equals(accountId))
                .findFirst()
                .orElseThrow();
        return account.getTransactions() != null ? account.getTransactions().size() : 0;
    }

    //============== Методы для получения данных профиля клиента ============
    protected static Customer getCustomerProfile(String customerName) {
        return new GetCustomerProfileRequester(
                RequestSpecs.authWithTokenSpec(token(customerName)),
                ResponseSpecs.requestReturnsOK())
                .get()
                .extract()
                .as(Customer.class);
    }

    protected static String getCustomerProfileName(String customerName) {
        return getCustomerProfile(customerName).getName();
    }

    //========================= Методы создания =============================
    //Метод создает нового пользователя и сохраняет токен, полученный при авторизации
    protected static String createUserAndGetToken(String username, String password) {
        CreateUserRequest createUserRequest = CreateUserRequest.builder()
                .username(username)
                .password(password)
                .role(UserRole.USER.toString())
                .build();

        new CreateUserRequester(
                RequestSpecs.adminSpec(),
                ResponseSpecs.entityWasCreated())
                .post(createUserRequest);

        LoginUserRequest loginUserRequest = LoginUserRequest.builder()
                .username(username)
                .password(password)
                .build();

        return new LoginUserRequester(
                RequestSpecs.unauthSpec(),
                ResponseSpecs.requestReturnsOK())
                .post(loginUserRequest)
                .extract()
                .header("authorization");
    }

    protected static Integer createUserAccount(String authToken) {
        return new CreateUserAccountRequester(
                RequestSpecs.authWithTokenSpec(authToken),
                ResponseSpecs.entityWasCreated())
                .post(new EmptyRequest())
                .extract()
                .path("id");
    }

    @BeforeEach
    public void setupTest() {
        this.softly = new SoftAssertions();
    }

    @AfterEach
    public void afterTest() {
        softly.assertAll();
    }

    @AfterAll
    public static void afterTests() {
        deleteAllUsers();
    }
}
