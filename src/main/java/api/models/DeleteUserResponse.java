package api.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class DeleteUserResponse extends BaseModel {
    private String message;

    @JsonCreator
    public DeleteUserResponse(String message) {
        this.message = message;
    }

    @JsonValue
    public String getMessage() {
        return message;
    }

    public boolean isDeletedSuccessfully() {
        return message != null && message.contains("deleted successfully");
    }

    public Long getDeletedUserId() {
        if (message == null) return null;

        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("User with ID (\\d+)");
        java.util.regex.Matcher matcher = pattern.matcher(message);

        if (matcher.find()) {
            return Long.parseLong(matcher.group(1));
        }
        return null;
    }
}