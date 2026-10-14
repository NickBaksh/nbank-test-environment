package api.requests.steps;

import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Data
@Builder
public class TestUserContext {
    private String username;
    private String password;
    private String token;
    private Long userId;
    private String role;

    @Builder.Default
    private Map<Long, String> accounts = java.util.Collections.synchronizedMap(new LinkedHashMap<>());

    public List<Long> getAccountsIds() {
        return new ArrayList<>(accounts.keySet());
    }

    @Deprecated
    public void addAccount(Long accountId) {
        accounts.put(accountId, null);
    }

    public void addAccount(Long accountId, String accountNumber) {
        accounts.put(accountId, accountNumber);
    }

    public String getAccountNumber(Long accountId) {
        return accounts.get(accountId);
    }

    public boolean hasAccountNumber(Long accountId) {
        String number = accounts.get(accountId);
        return number != null && !number.isEmpty();
    }

    public Long getFirstAccountId() {
        if (accounts.isEmpty()) {
            throw new IllegalStateException("User [" + username + "] has no accounts");
        }
        return new ArrayList<>(accounts.keySet()).get(0);
    }

    public Long getSecondAccountId() {
        if (accounts.size() < 2) {
            throw new IllegalStateException("User [" + username + "] has less than 2 accounts. Current: " + accounts.size());
        }
        return new ArrayList<>(accounts.keySet()).get(1);
    }

    public Long getAccountIdByIndex(int index) {
        if (index < 0 || index >= accounts.size()) {
            throw new IndexOutOfBoundsException("User [" + username + "] has no account at index " + index +
                    ". Total accounts: " + accounts.size());
        }
        return new ArrayList<>(accounts.keySet()).get(index);
    }

    public List<Long> getAllAccountIds() {
        return new ArrayList<>(accounts.keySet());
    }

    public boolean hasAccounts() {
        return !accounts.isEmpty();
    }

    public int getAccountsCount() {
        return accounts.size();
    }

    public String getDisplayName() {
        return username + " (ID: " + userId + ", role: " + role + ", accounts: " + accounts.size() + ")";
    }

    public boolean isAdmin() {
        return "ADMIN".equalsIgnoreCase(role);
    }

    public boolean isUser() {
        return "USER".equalsIgnoreCase(role);
    }

    public double getFirstAccountBalance() {
        return UserSteps.getAccountBalance(this, getFirstAccountId());
    }

    public int getFirstAccountTransactionsCount() {
        return UserSteps.getAccountTransactionsCount(this, getFirstAccountId());
    }

    public double getAccountBalance(int index) {
        return UserSteps.getAccountBalance(this, getAccountIdByIndex(index));
    }

    public int getAccountTransactionsCount(int index) {
        return UserSteps.getAccountTransactionsCount(this, getAccountIdByIndex(index));
    }

    public double getAccountBalanceByAccountId(Long accountId) {
        return UserSteps.getAccountBalance(this, accountId);
    }

    public int getAccountTransactionsCountByAccountId(Long accountId) {
        return UserSteps.getAccountTransactionsCount(this, accountId);
    }
}