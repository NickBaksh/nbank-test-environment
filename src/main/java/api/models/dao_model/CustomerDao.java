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
public class CustomerDao {
    public static RowMapper<CustomerDao> rowMapper = (rs) -> CustomerDao.builder()
            .id(rs.getLong("id"))
            .username(rs.getString("username"))
            .name(rs.getString("name"))
            .role(rs.getString("role"))
            .createdAt(rs.getString("created_at"))
            .updatedAt(rs.getString("updated_at"))
            .build();

    private Long id;
    private String username;
    private String name;
    private String role;
    private String createdAt;
    private String updatedAt;
}
