package api.requests.steps;

import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Data
@Builder
public class TestUserContext {
    private String username;
    private String password;
    private String token;
    private Long userId;
    private String role;

    @Builder.Default
    private Map<Long, String> accounts = new ConcurrentHashMap<>();

    // ========== Методы для обратной совместимости ==========

    /**
     * Получить список ID аккаунтов (для обратной совместимости)
     */
    public List<Long> getAccountsIds() {
        return new ArrayList<>(accounts.keySet());
    }

    /**
     * Добавить ID аккаунта (для обратной совместимости)
     * @deprecated Используйте {@link #addAccount(Long, String)}
     */
    @Deprecated
    public void addAccount(Long accountId) {
        accounts.put(accountId, null);
    }

    /**
     * Добавить аккаунт с ID и номером
     */
    public void addAccount(Long accountId, String accountNumber) {
        accounts.put(accountId, accountNumber);
    }

    /**
     * Получить номер аккаунта по ID
     */
    public String getAccountNumber(Long accountId) {
        return accounts.get(accountId);
    }

    /**
     * Проверить, есть ли у аккаунта номер
     */
    public boolean hasAccountNumber(Long accountId) {
        String number = accounts.get(accountId);
        return number != null && !number.isEmpty();
    }


    // Удобные методы
    public Long getFirstAccountId() {
        if (accounts.size() < 2) {
            throw new IllegalStateException("User [" + username + "] has less than 2 accounts");
        }
        List<Long> ids = new ArrayList<>(accounts.keySet());
        return ids.get(0);
    }

    public Long getSecondAccountId() {
        if (accounts.size() < 2) {
            throw new IllegalStateException("User [" + username + "] has less than 2 accounts");
        }
        List<Long> ids = new ArrayList<>(accounts.keySet());
        return ids.get(1);
    }

    public Long getAccountIdByIndex(int index) {
        if (index >= accounts.size()) {
            throw new IllegalStateException("User [" + username + "] has no account at index " + index);
        }
        List<Long> ids = new ArrayList<>(accounts.keySet());
        return ids.get(index);
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
}