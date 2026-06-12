package api.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.*;

import java.util.Arrays;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
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
