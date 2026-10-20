package api;

import api.generators.testdata.DataProviders;
import api.requests.steps.TestUserContext;
import api.requests.steps.UserSteps;
import common.extensions.AdminSessionExtension;
import common.extensions.UserSessionExtension;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.provider.Arguments;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

@ExtendWith({UserSessionExtension.class, AdminSessionExtension.class})
public class BaseTest {

    protected SoftAssertions softly;

    private TestUserContext currentUser;
    private List<TestUserContext> users = new ArrayList<>();

    @BeforeEach
    public void setUp() {
        softly = new SoftAssertions();
        if (users == null) {
            users = new ArrayList<>();
        }
    }

    @AfterEach
    public void afterTest() {
        try {
            // Очищаем всех пользователей, созданных в тесте
            UserSteps.cleanupTestUsers();
        } catch (Exception e) {
            System.err.println("Error during cleanup: " + e.getMessage());
        }
        users = null;
        currentUser = null;
        softly = null;
    }


    public static Stream<Double> validTransferAmounts() {
        return DataProviders.validTransferAmounts();
    }

    public static Stream<Arguments> invalidTransferAmountsApiV1() {
        return DataProviders.invalidTransferAmountsApiV1();
    }

    public static Stream<Arguments> invalidTransferAmountsApiV2() {
        return DataProviders.invalidTransferAmountsApiV2();
    }

    public static Stream<Double> validDepositAmounts() {
        return DataProviders.validDepositAmounts();
    }

    public static Stream<Arguments> invalidDepositAmountsV1Api() {
        return DataProviders.invalidDepositAmountsV1Api();
    }

    public static Stream<Arguments> invalidDepositAmountsV2Api() {
        return DataProviders.invalidDepositAmountsV2Api();
    }

    public static Stream<String> validProfileNames() {
        return DataProviders.validProfileNames();
    }

    public static Stream<String> invalidProfileNames() {
        return DataProviders.invalidProfileNames();
    }

    public static Stream<Arguments> invalidProfileNamesUi() {
        return DataProviders.invalidProfileNamesUi();
    }

    public static Stream<Arguments> invalidDepositAmountsUi() {
        return DataProviders.invalidDepositAmountsUi();
    }

    public static Stream<Arguments> invalidTransferAmountsUi() {
        return DataProviders.invalidTransferAmountsUi();
    }

    // ========== Getters и Setters ==========

    public TestUserContext getCurrentUser() {
        if (currentUser == null && users != null && !users.isEmpty()) {
            return users.get(0);
        }
        return currentUser;
    }

    public void setCurrentUser(TestUserContext currentUser) {
        this.currentUser = currentUser;
        if (currentUser != null && (users == null || users.isEmpty())) {
            users = new ArrayList<>();
            users.add(currentUser);
        }
    }

    public List<TestUserContext> getUsers() {
        return users != null ? users : new ArrayList<>();
    }

    public void setUsers(List<TestUserContext> users) {
        this.users = users != null ? users : new ArrayList<>();
        if (!this.users.isEmpty() && currentUser == null) {
            this.currentUser = this.users.get(0);
        }
    }

    public TestUserContext getUser(int index) {
        if (users != null && index >= 0 && index < users.size()) {
            return users.get(index);
        }
        throw new IndexOutOfBoundsException("User at index " + index + " not found. Total users: " +
                (users != null ? users.size() : 0));
    }

    public int getUserCount() {
        return users != null ? users.size() : 0;
    }

    public TestUserContext getFirstUser() {
        return getUser(0);
    }

    public TestUserContext getSecondUser() {
        if (users == null || users.size() < 2) {
            throw new IllegalStateException("Less than 2 users available. Current: " +
                    (users != null ? users.size() : 0));
        }
        return getUser(1);
    }
}