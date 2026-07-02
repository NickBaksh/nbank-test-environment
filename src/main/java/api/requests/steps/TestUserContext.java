package api.requests.steps;

import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
public class TestUserContext {
    private String username;
    private String password;
    private String token;
    private Long userId;
    private String role;

    @Builder.Default
    private List<Long> accountIds = new ArrayList<>();

    // Удобные методы
    public Long getFirstAccountId() {
        if (accountIds.isEmpty()) {
            throw new IllegalStateException("User [" + username + "] has no accounts");
        }
        return accountIds.get(0);
    }

    public Long getSecondAccountId() {
        if (accountIds.size() < 2) {
            throw new IllegalStateException("User [" + username + "] has less than 2 accounts");
        }
        return accountIds.get(1);
    }

    public Long getAccountIdByIndex(int index) {
        if (index >= accountIds.size()) {
            throw new IllegalStateException("User [" + username + "] has no account at index " + index);
        }
        return accountIds.get(index);
    }

    public boolean hasAccounts() {
        return !accountIds.isEmpty();
    }

    public int getAccountsCount() {
        return accountIds.size();
    }

    public String getDisplayName() {
        return username + " (ID: " + userId + ", role: " + role + ", accounts: " + accountIds.size() + ")";
    }

    public boolean isAdmin() {
        return "ADMIN".equalsIgnoreCase(role);
    }

    public boolean isUser() {
        return "USER".equalsIgnoreCase(role);
    }
}