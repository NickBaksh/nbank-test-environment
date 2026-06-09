package api.models;

import lombok.*;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Transaction extends BaseModel {
    private Long id;
    private double amount;
    private String type;
    private String timestamp;
    private Long relatedAccountId;
}
