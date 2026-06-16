package api.models;

import lombok.*;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GetTransactionsResponse extends BaseModel {
    private List<Transaction> transactions;
}
