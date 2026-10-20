package iteration_one.api_tests;

import api.BaseTest;
import api.constants.JsonFields;
import api.models.comparison.ModelAssertions;
import api.models.dto_model.TransferWithFraudCheckRequest;
import api.models.dto_model.TransferWithFraudCheckResponse;
import api.requests.skelethon.Endpoint;
import api.requests.skelethon.requesters.CrudRequester;
import api.requests.skelethon.requesters.ValidatedCrudRequester;
import api.requests.steps.DataBaseSteps;
import api.requests.steps.TestUserContext;
import api.requests.steps.UserSteps;
import api.specs.RequestSpecs;
import api.specs.ResponseSpecs;
import common.annotations.FraudCheckMock;
import common.annotations.UserSession;
import common.extensions.FraudCheckMockExtension;
import common.extensions.TimingExtension;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static api.constants.JsonFields.MESSAGE;
import static api.constants.JsonFields.Status;
import static api.generators.testdata.DataProviders.*;
import static api.generators.testdata.InvalidTransferCase.INSUFFICIENT_FUNDS;
import static api.specs.ResponseSpecs.INSUFFICIENT_FUNDS_ERROR_V2;
import static api.specs.ResponseSpecs.TRANSFER_AMOUNT_MIN_ERROR_V_3;
import static org.hamcrest.Matchers.equalTo;

@ExtendWith({TimingExtension.class, FraudCheckMockExtension.class})
@Execution(ExecutionMode.SAME_THREAD)
public class TransferWithFraudCheckTest extends BaseTest {
    @BeforeEach
    public void setupTest() {
        this.softly = new SoftAssertions();
    }

    @ParameterizedTest
    @MethodSource("validTransferAmounts")
    @UserSession(users = 2, accounts = 1)
    @FraudCheckMock(
            status = "SUCCESS",
            decision = "APPROVED",
            riskScore = 0.2,
            reason = "Low risk transaction",
            requiresManualReview = false,
            additionalVerificationRequired = false
    )
    public void testTransferWithFraudCheckApproved(double amount) {
        TestUserContext sender = getFirstUser();
        TestUserContext receiver = getSecondUser();

        UserSteps.setUpBalance(sender);

        long senderAccountId = sender.getFirstAccountId();
        long receiverAccountId = receiver.getFirstAccountId();

        TransferWithFraudCheckRequest request = TransferWithFraudCheckRequest.builder()
                .senderAccountId(senderAccountId)
                .receiverAccountId(receiverAccountId)
                .amount(amount)
                .build();

        TransferWithFraudCheckResponse response = new ValidatedCrudRequester<TransferWithFraudCheckResponse>(
                RequestSpecs.authWithTokenSpec(sender.getToken()),
                ResponseSpecs.requestReturnsOK(),
                Endpoint.ACCOUNTS_TRANSFER_WITH_FRAUD_CHECK)
                .create(request);

        ModelAssertions.assertThatModels(request, response).match();

        TransferWithFraudCheckResponse expectedResponse = TransferWithFraudCheckResponse.builder()
                .status(Status.APPROVED.toString())
                .message(JsonFields.Message.TRANSFER_APPROVED.getMessage())
                .amount(amount)
                .senderAccountId(senderAccountId)
                .receiverAccountId(receiverAccountId)
                .fraudRiskScore(FRAUD_RISK_SCORE_0_2)
                .fraudReason(JsonFields.FraudReason.LOW_RISK_TRANSACTION.getReason())
                .requiresManualReview(false)
                .requiresVerification(false)
                .transactionId(response.getTransactionId())
                .build();

        ModelAssertions.assertThatModels(expectedResponse, response).match();
    }

    @ParameterizedTest
    @MethodSource("validTransferAmounts")
    @UserSession(users = 2, accounts = 1)
    @FraudCheckMock(
            status = "SUCCESS",
            decision = "BLOCKED",
            riskScore = 0.2,
            reason = "Low risk transaction",
            requiresManualReview = false,
            additionalVerificationRequired = false
    )
    public void testTransferWithFraudCheckBlocked(double amount) {
        TestUserContext sender = getFirstUser();
        TestUserContext receiver = getSecondUser();

        UserSteps.setUpBalance(sender);

        long senderAccountId = sender.getFirstAccountId();
        long receiverAccountId = receiver.getFirstAccountId();

        TransferWithFraudCheckRequest request = TransferWithFraudCheckRequest.builder()
                .senderAccountId(senderAccountId)
                .receiverAccountId(receiverAccountId)
                .amount(amount)
                .build();


        TransferWithFraudCheckResponse response = new ValidatedCrudRequester<TransferWithFraudCheckResponse>(
                RequestSpecs.authWithTokenSpec(sender.getToken()),
                ResponseSpecs.requestReturnsOK(),
                Endpoint.ACCOUNTS_TRANSFER_WITH_FRAUD_CHECK)
                .create(request);

        ModelAssertions.assertThatModels(request, response).match();

        TransferWithFraudCheckResponse expectedResponse = TransferWithFraudCheckResponse.builder()
                .status(Status.BLOCKED.toString())
                .message(JsonFields.Message.TRANSFER_BLOCKED.getMessage())
                .amount(amount)
                .senderAccountId(senderAccountId)
                .receiverAccountId(receiverAccountId)
                .fraudRiskScore(FRAUD_RISK_SCORE_0_2)
                .fraudReason(JsonFields.FraudReason.LOW_RISK_TRANSACTION.getReason())
                .requiresManualReview(false)
                .requiresVerification(false)
                .transactionId(response.getTransactionId())
                .build();

        ModelAssertions.assertThatModels(expectedResponse, response).match();
    }

    @ParameterizedTest
    @MethodSource("validTransferAmounts")
    @UserSession(users = 2, accounts = 1)
    @FraudCheckMock(
            status = "SUCCESS",
            decision = "REVIEW_REQUIRED",
            riskScore = 0.2,
            reason = "Low risk transaction",
            requiresManualReview = true,
            additionalVerificationRequired = false
    )
    public void testTransferWithFraudCheckReviewRequired(double amount) {
        TestUserContext sender = getFirstUser();
        TestUserContext receiver = getSecondUser();

        UserSteps.setUpBalance(sender);

        long senderAccountId = sender.getFirstAccountId();
        long receiverAccountId = receiver.getFirstAccountId();

        TransferWithFraudCheckRequest request = TransferWithFraudCheckRequest.builder()
                .senderAccountId(senderAccountId)
                .receiverAccountId(receiverAccountId)
                .amount(amount)
                .build();


        TransferWithFraudCheckResponse response = new ValidatedCrudRequester<TransferWithFraudCheckResponse>(
                RequestSpecs.authWithTokenSpec(sender.getToken()),
                ResponseSpecs.requestReturnsOK(),
                Endpoint.ACCOUNTS_TRANSFER_WITH_FRAUD_CHECK)
                .create(request);

        ModelAssertions.assertThatModels(request, response).match();

        TransferWithFraudCheckResponse expectedResponse = TransferWithFraudCheckResponse.builder()
                .status(Status.MANUAL_REVIEW_REQUIRED.toString())
                .message(JsonFields.Message.TRANSFER_REVIEW_REQUIRED.getMessage())
                .amount(amount)
                .senderAccountId(senderAccountId)
                .receiverAccountId(receiverAccountId)
                .fraudRiskScore(FRAUD_RISK_SCORE_0_2)
                .fraudReason(JsonFields.FraudReason.LOW_RISK_TRANSACTION.getReason())
                .requiresManualReview(true)
                .requiresVerification(false)
                .transactionId(response.getTransactionId())
                .build();

        ModelAssertions.assertThatModels(expectedResponse, response).match();
    }

    @ParameterizedTest
    @MethodSource("validTransferAmounts")
    @UserSession(users = 2, accounts = 1)
    @FraudCheckMock(
            status = "SUCCESS",
            decision = "VERIFICATION_REQUIRED",
            riskScore = 0.2,
            reason = "Low risk transaction",
            requiresManualReview = false,
            additionalVerificationRequired = true
    )
    public void testTransferWithFraudCheckVerificationRequired(double amount) {
        TestUserContext sender = getFirstUser();
        TestUserContext receiver = getSecondUser();

        UserSteps.setUpBalance(sender);

        long senderAccountId = sender.getFirstAccountId();
        long receiverAccountId = receiver.getFirstAccountId();

        TransferWithFraudCheckRequest request = TransferWithFraudCheckRequest.builder()
                .senderAccountId(senderAccountId)
                .receiverAccountId(receiverAccountId)
                .amount(amount)
                .build();


        TransferWithFraudCheckResponse response = new ValidatedCrudRequester<TransferWithFraudCheckResponse>(
                RequestSpecs.authWithTokenSpec(sender.getToken()),
                ResponseSpecs.requestReturnsOK(),
                Endpoint.ACCOUNTS_TRANSFER_WITH_FRAUD_CHECK)
                .create(request);

        ModelAssertions.assertThatModels(request, response).match();

        TransferWithFraudCheckResponse expectedResponse = TransferWithFraudCheckResponse.builder()
                .status(Status.VERIFICATION_REQUIRED.toString())
                .message(JsonFields.Message.VERIFICATION_REQUIRED.getMessage())
                .amount(amount)
                .senderAccountId(senderAccountId)
                .receiverAccountId(receiverAccountId)
                .fraudRiskScore(FRAUD_RISK_SCORE_0_2)
                .fraudReason(JsonFields.FraudReason.LOW_RISK_TRANSACTION.getReason())
                .requiresManualReview(false)
                .requiresVerification(true)
                .transactionId(response.getTransactionId())
                .build();

        ModelAssertions.assertThatModels(expectedResponse, response).match();
    }

    @Test
    @UserSession(users = 2, accounts = 1)
    @FraudCheckMock(
            simulateTimeout = true
    )
    public void testFraudCheckTimeoutFallbackToReview() {
        TestUserContext sender = getFirstUser();
        TestUserContext receiver = getSecondUser();

        UserSteps.setUpBalance(sender);

        long senderAccountId = sender.getFirstAccountId();
        long receiverAccountId = receiver.getFirstAccountId();

        TransferWithFraudCheckRequest request = TransferWithFraudCheckRequest.builder()
                .senderAccountId(senderAccountId)
                .receiverAccountId(receiverAccountId)
                .amount(TRANSACTION_100)
                .build();

        TransferWithFraudCheckResponse response = new ValidatedCrudRequester<TransferWithFraudCheckResponse>(
                RequestSpecs.authWithTokenSpec(sender.getToken()),
                ResponseSpecs.requestReturnsOK(),
                Endpoint.ACCOUNTS_TRANSFER_WITH_FRAUD_CHECK)
                .create(request);

        ModelAssertions.assertThatModels(request, response).match();


        TransferWithFraudCheckResponse expectedResponse = TransferWithFraudCheckResponse.builder()
                .status(Status.MANUAL_REVIEW_REQUIRED.toString())
                .message(JsonFields.Message.TRANSFER_REVIEW_REQUIRED.getMessage())
                .amount(TRANSACTION_100)
                .senderAccountId(senderAccountId)
                .receiverAccountId(receiverAccountId)
                .fraudRiskScore(FRAUD_RISK_SCORE_0_5)
                .fraudReason(JsonFields.FraudReason.UNEXPECTED_ERROR.getReason())
                .requiresManualReview(true)
                .requiresVerification(false)
                .transactionId(response.getTransactionId())
                .build();

        ModelAssertions.assertThatModels(expectedResponse, response).match();
    }

    @Test
    @UserSession(users = 2, accounts = 1)
    @FraudCheckMock(
            simulateConnectionError = true
    )
    public void testFraudCheckConnectionErrorFallbackToReview() {
        TestUserContext sender = getFirstUser();
        TestUserContext receiver = getSecondUser();

        UserSteps.setUpBalance(sender);

        long senderAccountId = sender.getFirstAccountId();
        long receiverAccountId = receiver.getFirstAccountId();

        TransferWithFraudCheckRequest request = TransferWithFraudCheckRequest.builder()
                .senderAccountId(senderAccountId)
                .receiverAccountId(receiverAccountId)
                .amount(TRANSACTION_1000)
                .build();

        TransferWithFraudCheckResponse response = new ValidatedCrudRequester<TransferWithFraudCheckResponse>(
                RequestSpecs.authWithTokenSpec(sender.getToken()),
                ResponseSpecs.requestReturnsOK(),
                Endpoint.ACCOUNTS_TRANSFER_WITH_FRAUD_CHECK)
                .create(request);

        ModelAssertions.assertThatModels(request, response).match();

        TransferWithFraudCheckResponse expectedResponse = TransferWithFraudCheckResponse.builder()
                .status(Status.MANUAL_REVIEW_REQUIRED.toString())
                .message(JsonFields.Message.TRANSFER_REVIEW_REQUIRED.getMessage())
                .amount(TRANSACTION_1000)
                .senderAccountId(senderAccountId)
                .receiverAccountId(receiverAccountId)
                .fraudRiskScore(FRAUD_RISK_SCORE_0_5)
                .fraudReason(JsonFields.FraudReason.UNEXPECTED_ERROR.getReason())
                .requiresManualReview(true)
                .requiresVerification(false)
                .transactionId(response.getTransactionId())
                .build();

        ModelAssertions.assertThatModels(expectedResponse, response).match();
    }

    @Test
    @UserSession(users = 2, accounts = 1)
    @FraudCheckMock(
            errorHttpStatus = 500
    )
    public void testFraudCheckInternalServerErrorFallbackToReview() {
        TestUserContext sender = getFirstUser();
        TestUserContext receiver = getSecondUser();

        UserSteps.setUpBalance(sender);

        long senderAccountId = sender.getFirstAccountId();
        long receiverAccountId = receiver.getFirstAccountId();

        TransferWithFraudCheckRequest request = TransferWithFraudCheckRequest.builder()
                .senderAccountId(senderAccountId)
                .receiverAccountId(receiverAccountId)
                .amount(TRANSACTION_1000)
                .build();

        TransferWithFraudCheckResponse response = new ValidatedCrudRequester<TransferWithFraudCheckResponse>(
                RequestSpecs.authWithTokenSpec(sender.getToken()),
                ResponseSpecs.requestReturnsOK(),
                Endpoint.ACCOUNTS_TRANSFER_WITH_FRAUD_CHECK)
                .create(request);

        ModelAssertions.assertThatModels(request, response).match();

        TransferWithFraudCheckResponse expectedResponse = TransferWithFraudCheckResponse.builder()
                .status(Status.MANUAL_REVIEW_REQUIRED.toString())
                .message(JsonFields.Message.TRANSFER_REVIEW_REQUIRED.getMessage())
                .amount(TRANSACTION_1000)
                .senderAccountId(senderAccountId)
                .receiverAccountId(receiverAccountId)
                .fraudRiskScore(FRAUD_RISK_SCORE_0_5)
                .fraudReason(JsonFields.FraudReason.UNEXPECTED_ERROR.getReason())
                .requiresManualReview(true)
                .requiresVerification(false)
                .transactionId(response.getTransactionId())
                .build();

        ModelAssertions.assertThatModels(expectedResponse, response).match();
    }


    @ParameterizedTest
    @MethodSource("invalidTransferAmountsApiV1")
    @UserSession(users = 2, accounts = 1)
    @FraudCheckMock(
            status = "SUCCESS",
            decision = "APPROVED",
            riskScore = 0.2,
            reason = "Low risk transaction",
            requiresManualReview = false,
            additionalVerificationRequired = false
    )
    public void userCannotTransferInvalidSumWithFraudCheckApiV1Test(double amount, String expectedError) {
        TestUserContext sender = getFirstUser();
        TestUserContext receiver = getSecondUser();

        UserSteps.setUpBalance(sender);

        long senderAccountId = sender.getFirstAccountId();
        long receiverAccountId = receiver.getFirstAccountId();

        double firstAccountBalanceExpected = UserSteps.getFirstAccountBalance(sender);
        int firstAccountTransactionsCountExpected = UserSteps.getFirstAccountTransactionsCount(sender);

        double secondAccountBalanceExpected = UserSteps.getFirstAccountBalance(receiver);
        int secondAccountTransactionsCountExpected = UserSteps.getFirstAccountTransactionsCount(receiver);

        TransferWithFraudCheckRequest request = TransferWithFraudCheckRequest.builder()
                .senderAccountId(senderAccountId)
                .receiverAccountId(receiverAccountId)
                .amount(amount)
                .build();

        new CrudRequester(
                RequestSpecs.authWithTokenSpec(sender.getToken()),
                ResponseSpecs.returnsBadRequest(),
                Endpoint.ACCOUNTS_TRANSFER_WITH_FRAUD_CHECK)
                .create(request)
                .body(MESSAGE, equalTo(expectedError));

        double firstAccountBalanceActual = UserSteps.getFirstAccountBalance(sender);
        int firstAccountTransactionsCountActual = UserSteps.getFirstAccountTransactionsCount(sender);

        double secondAccountBalanceActual = UserSteps.getFirstAccountBalance(receiver);
        int secondAccountTransactionsCountActual = UserSteps.getFirstAccountTransactionsCount(receiver);

        softly.assertThat(firstAccountBalanceActual)
                .as("Balance should not decrease")
                .isEqualTo(firstAccountBalanceExpected);

        softly.assertThat(firstAccountTransactionsCountActual)
                .as("Transaction count should not increase")
                .isEqualTo(firstAccountTransactionsCountExpected);

        softly.assertThat(secondAccountBalanceActual)
                .as("Balance should not decrease")
                .isEqualTo(secondAccountBalanceExpected);

        softly.assertThat(secondAccountTransactionsCountActual)
                .as("Transaction count should not increase")
                .isEqualTo(secondAccountTransactionsCountExpected);

        DataBaseSteps.verifyAccountUnchanged(softly, senderAccountId,
                firstAccountBalanceExpected, firstAccountTransactionsCountExpected);
        DataBaseSteps.verifyAccountUnchanged(softly, receiverAccountId,
                secondAccountBalanceExpected, secondAccountTransactionsCountExpected);
    }

    @Test
    @UserSession
    public void userCannotTransferMoreMoneyThanHaveTest() {
        TestUserContext user = getCurrentUser();

        String token = user.getToken();
        UserSteps.setUpBalance(user);
        long firstAccountId = user.getFirstAccountId();
        long secondAccountId = user.getSecondAccountId();

        double firstAccountBalanceExpected = UserSteps.getFirstAccountBalance(user);
        int firstAccountTransactionsCountExpected = UserSteps.getFirstAccountTransactionsCount(user);

        double secondAccountBalanceExpected = UserSteps.getSecondAccountBalance(user);
        int secondAccountTransactionsCountExpected = UserSteps.getSecondAccountTransactionsCount(user);

        TransferWithFraudCheckRequest request = TransferWithFraudCheckRequest.builder()
                .senderAccountId(secondAccountId)
                .receiverAccountId(firstAccountId)
                .amount(TRANSACTION_10000)
                .build();


        new CrudRequester(
                RequestSpecs.authWithTokenSpec(token),
                ResponseSpecs.returnsBadRequest(),
                Endpoint.ACCOUNTS_TRANSFER_WITH_FRAUD_CHECK)
                .create(request)
                .body(MESSAGE, equalTo(INSUFFICIENT_FUNDS_ERROR_V2));

        double firstAccountBalanceActual = UserSteps.getFirstAccountBalance(user);
        int firstAccountTransactionsCountActual = UserSteps.getFirstAccountTransactionsCount(user);

        double secondAccountBalanceActual = UserSteps.getSecondAccountBalance(user);
        int secondAccountTransactionsCountActual = UserSteps.getSecondAccountTransactionsCount(user);

        softly.assertThat(firstAccountBalanceActual)
                .as("Balance should not decrease")
                .isEqualTo(firstAccountBalanceExpected);

        softly.assertThat(firstAccountTransactionsCountActual)
                .as("Transaction count should not increase")
                .isEqualTo(firstAccountTransactionsCountExpected);

        softly.assertThat(secondAccountBalanceActual)
                .as("Balance should not decrease")
                .isEqualTo(secondAccountBalanceExpected);

        softly.assertThat(secondAccountTransactionsCountActual)
                .as("Transaction count should not increase")
                .isEqualTo(secondAccountTransactionsCountExpected);

        DataBaseSteps.verifyAccountUnchanged(softly, firstAccountId,
                firstAccountBalanceExpected, firstAccountTransactionsCountExpected);
        DataBaseSteps.verifyAccountUnchanged(softly, secondAccountId,
                secondAccountBalanceExpected, secondAccountTransactionsCountExpected);
    }

    @Test
    @UserSession
    public void userCannotTransferMoneyToNonExistentAccountTest() {
        TestUserContext user = getCurrentUser();

        String token = user.getToken();
        UserSteps.setUpBalance(user);
        long firstAccountId = user.getFirstAccountId();
        double firstAccountBalanceExpected = UserSteps.getFirstAccountBalance(user);
        int firstAccountTransactionsCountExpected = UserSteps.getFirstAccountTransactionsCount(user);

        TransferWithFraudCheckRequest request = TransferWithFraudCheckRequest.builder()
                .senderAccountId(firstAccountId)
                .receiverAccountId(NON_EXISTENT_ACCOUNT_ID)
                .amount(getValidTransferAmount())
                .build();

        new CrudRequester(
                RequestSpecs.authWithTokenSpec(token),
                ResponseSpecs.returnsBadRequest(),
                Endpoint.ACCOUNTS_TRANSFER_WITH_FRAUD_CHECK)
                .create(request)
                .body(MESSAGE, equalTo(TRANSFER_AMOUNT_MIN_ERROR_V_3));


        double firstAccountBalanceActual = UserSteps.getFirstAccountBalance(user);
        int firstAccountTransactionsCountActual = UserSteps.getFirstAccountTransactionsCount(user);


        softly.assertThat(firstAccountBalanceActual)
                .as("Balance should not decrease")
                .isEqualTo(firstAccountBalanceExpected);

        softly.assertThat(firstAccountTransactionsCountActual)
                .as("Transaction count should not increase")
                .isEqualTo(firstAccountTransactionsCountExpected);

        DataBaseSteps.verifyAccountUnchanged(softly, firstAccountId,
                firstAccountBalanceExpected, firstAccountTransactionsCountExpected);
    }

    @Test
    @UserSession
    public void userCannotTransferMoneyWithoutAuthorizationTest() {
        TestUserContext user = getCurrentUser();
        UserSteps.setUpBalance(user);

        long firstAccountId = user.getFirstAccountId();
        long secondAccountId = user.getSecondAccountId();

        double firstAccountBalanceExpected = UserSteps.getFirstAccountBalance(user);
        int firstAccountTransactionsCountExpected = UserSteps.getFirstAccountTransactionsCount(user);

        double secondAccountBalanceExpected = UserSteps.getSecondAccountBalance(user);
        int secondAccountTransactionsCountExpected = UserSteps.getSecondAccountTransactionsCount(user);

        TransferWithFraudCheckRequest request = TransferWithFraudCheckRequest.builder()
                .senderAccountId(firstAccountId)
                .receiverAccountId(secondAccountId)
                .amount(INSUFFICIENT_FUNDS.generate())
                .build();

        new CrudRequester(
                RequestSpecs.unauthSpec(),
                ResponseSpecs.returnsUnauthorize(),
                Endpoint.ACCOUNTS_TRANSFER_WITH_FRAUD_CHECK)
                .create(request);

        double firstAccountBalanceActual = UserSteps.getFirstAccountBalance(user);
        int firstAccountTransactionsCountActual = UserSteps.getFirstAccountTransactionsCount(user);

        double secondAccountBalanceActual = UserSteps.getSecondAccountBalance(user);
        int secondAccountTransactionsCountActual = UserSteps.getSecondAccountTransactionsCount(user);

        softly.assertThat(firstAccountBalanceActual)
                .as("Balance should not decrease")
                .isEqualTo(firstAccountBalanceExpected);

        softly.assertThat(firstAccountTransactionsCountActual)
                .as("Transaction count should not increase")
                .isEqualTo(firstAccountTransactionsCountExpected);

        softly.assertThat(secondAccountBalanceActual)
                .as("Balance should not decrease")
                .isEqualTo(secondAccountBalanceExpected);

        softly.assertThat(secondAccountTransactionsCountActual)
                .as("Transaction count should not increase")
                .isEqualTo(secondAccountTransactionsCountExpected);

        DataBaseSteps.verifyAccountUnchanged(softly, firstAccountId,
                firstAccountBalanceExpected, firstAccountTransactionsCountExpected);
        DataBaseSteps.verifyAccountUnchanged(softly, secondAccountId,
                secondAccountBalanceExpected, secondAccountTransactionsCountExpected);
    }

    @Test
    @UserSession
    public void userCannotTransferMoneyToSameAccountTest() {
        TestUserContext user = getCurrentUser();
        UserSteps.setUpBalance(user);

        String token = user.getToken();
        long firstAccountId = user.getFirstAccountId();

        double firstAccountBalanceExpected = UserSteps.getFirstAccountBalance(user);
        int firstAccountTransactionsCountExpected = UserSteps.getFirstAccountTransactionsCount(user) + 1;

        TransferWithFraudCheckRequest request = TransferWithFraudCheckRequest.builder()
                .senderAccountId(firstAccountId)
                .receiverAccountId(firstAccountId)
                .amount(TRANSACTION_100)
                .build();

        TransferWithFraudCheckResponse response = new ValidatedCrudRequester<TransferWithFraudCheckResponse>(
                RequestSpecs.authWithTokenSpec(token),
                ResponseSpecs.requestReturnsOK(),
                Endpoint.ACCOUNTS_TRANSFER_WITH_FRAUD_CHECK)
                .create(request);

        double firstAccountBalanceActual = UserSteps.getFirstAccountBalance(user);
        int firstAccountTransactionsCountActual = UserSteps.getFirstAccountTransactionsCount(user);

        ModelAssertions.assertThatModels(request, response).match();

        softly.assertThat(firstAccountBalanceActual)
                .as("Balance should not change")
                .isEqualTo(firstAccountBalanceExpected);

        softly.assertThat(firstAccountTransactionsCountActual)
                .as("Transaction count should increase by 1")
                .isEqualTo(firstAccountTransactionsCountExpected);
    }
}
