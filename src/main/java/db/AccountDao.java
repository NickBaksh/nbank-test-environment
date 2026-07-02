package db;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountDao {
    // RowMapper для преобразования ResultSet в AccountDao
    public static RowMapper<AccountDao> rowMapper = (rs) -> AccountDao.builder()
            .id(rs.getLong("id"))
            .accountNumber(rs.getString("account_number"))
            .balance(rs.getDouble("balance"))
            .customerId(rs.getLong("customer_id"))
            .createdAt(rs.getString("created_at"))
            .updatedAt(rs.getString("updated_at"))
            .build();
    private Long id;
    private String accountNumber;
    private Double balance;
    private Long customerId;
    private String createdAt;
    private String updatedAt;
}
