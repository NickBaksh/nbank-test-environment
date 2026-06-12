package ui.pages;

import lombok.Getter;

@Getter
public enum BankAlert {
    USER_CREATED_SUCCESSFULLY("✅ User created successfully!"),
    NAME_UPDATED_SUCCESSFULLY("✅ Name updated successfully!"),
    NAME_MUST_CONTAIN_TWO_WORDS("Name must contain two words with letters only"),
    PLEASE_SELECT_AN_ACCOUNT("❌ Please select an account."),
    PLEASE_ENTER_A_VALID_AMOUNT("❌ Please enter a valid amount."),
    PLEASE_DEPOSIT_LESS_OR_EQUAL_TO_5000("❌ Please deposit less or equal to 5000$."),
    PLEASE_FILL_ALL_FIELDS("❌ Please fill all fields and confirm."),
    THE_RECIPIENT_NAME_DOES_NOT_MATCH("❌ The recipient name does not match the registered name."),
    NO_MATCHING_USERS_FOUND("❌ No matching users found.");


    private final String message;

    BankAlert(String message) {
        this.message = message;
    }
}
