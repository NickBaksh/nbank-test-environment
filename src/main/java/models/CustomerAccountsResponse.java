package models;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Arrays;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CustomerAccountsResponse extends BaseModel {
    private List<Account> accounts;

    // Джексон вызывает этот конструктор когда видит массив
    @JsonCreator
    public CustomerAccountsResponse(Account[] accounts) {
        this.accounts = accounts != null ? Arrays.asList(accounts) : null;
    }
}
