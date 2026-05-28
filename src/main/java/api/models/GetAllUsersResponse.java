package api.models;

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
public class GetAllUsersResponse extends BaseModel {
    private List<Customer> customers;

    // Джексон вызывает этот конструктор когда видит массив
    @JsonCreator
    public GetAllUsersResponse(Customer[] customers) {
        this.customers = customers != null ? Arrays.asList(customers) : null;
    }
}