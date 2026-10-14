package api.models.dao_model;

import db.RowMapper;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionDao {
    public static RowMapper<TransactionDao> rowMapper = (rs) -> TransactionDao.builder()
            .id(rs.getLong("id"))
            .amount(rs.getDouble("amount"))
            .type(TransactionType.valueOf(rs.getString("type")))
            .timestamp(rs.getString("timestamp"))
            .accountId(rs.getLong("account_id"))
            .relatedAccountId(rs.getObject("related_account_id", Long.class))
            .createdAt(rs.getString("created_at"))
            .build();

    private Long id;
    private Double amount;
    private TransactionType type;
    private String timestamp;
    private Long accountId;
    private Long relatedAccountId;
    private String createdAt;
}