package api.models.dto_model;

import api.models.BaseModel;
import lombok.*;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Account extends BaseModel {
    private Long id;
    private String accountNumber;
    private double balance;
    private List<Transaction> transactions;
}

