package api.models.dto_model;

import api.models.BaseModel;
import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.*;

import java.util.Arrays;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GetAllUsersResponse extends BaseModel {
    private List<Customer> customers;

    // Джексон вызывает этот конструктор когда видит массив
    @JsonCreator
    public GetAllUsersResponse(Customer[] customers) {
        this.customers = customers != null ? Arrays.asList(customers) : null;
    }
}