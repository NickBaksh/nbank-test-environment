package api.requests.skelethon;

import api.models.*;
import api.models.dto_model.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
//import models.*;

@Getter
@AllArgsConstructor
public enum Endpoint {
    // Admin endpoints
    ADMIN_USERS_GET(
            "/admin/users",
            null,  // GET запрос без body
            GetAllUsersResponse.class,
            null
    ),
    ADMIN_USER_CREATE(
            "/admin/users",
            CreateUserRequest.class,
            CreateUserResponse.class,
            null
    ),
    ADMIN_USER_DELETE(
            "/admin/users/{id}",
            null,  // DELETE запрос без body
            DeleteUserResponse.class,
            "id"
    ),

    // Accounts endpoints
    ACCOUNTS_CREATE(
            "/accounts",
            null,  // POST может быть без body согласно spec
            CreateAccountResponse.class,
            null
    ),
    ACCOUNTS_DEPOSIT(
            "/accounts/deposit",
            DepositRequest.class,
            DepositResponse.class,
            null
    ),
    ACCOUNTS_TRANSFER(
            "/accounts/transfer",
            TransferRequest.class,
            TransferResponse.class,
            null
    ),
    ACCOUNTS_TRANSACTIONS(
            "/accounts/{accountId}/transactions",
            null,  // GET запрос без body
            GetTransactionsResponse.class,
            "accountId"
    ),

    // Authentication endpoints
    LOGIN(
            "/auth/login",
            LoginUserRequest.class,
            LoginUserResponse.class,
            null
    ),

    // Customer endpoints
    CUSTOMER_PROFILE_GET(
            "/customer/profile",
            null,
            CustomerProfileResponse.class,
            null
    ),
    CUSTOMER_PROFILE_UPDATE(
            "/customer/profile",
            UpdateCustomerProfileRequest.class,
            UpdateCustomerProfileResponse.class,
            null
    ),
    CUSTOMER_ACCOUNTS(
            "/customer/accounts",
            null,
            CustomerAccountsResponse.class,
            null
    );

    private final String url;
    private final Class<? extends BaseModel> requestModel;
    private final Class<? extends BaseModel> responseModel;
    private final String pathParam;
}