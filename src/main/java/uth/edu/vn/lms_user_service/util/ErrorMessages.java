package uth.edu.vn.lms_user_service.util;

public final class ErrorMessages {

    private ErrorMessages() {}

    public static final String USER_NOT_FOUND = "User not found with id: ";
    public static final String USERNAME_EXISTS = "Username already exists";
    public static final String EMAIL_EXISTS = "Email already exists";
    public static final String INVALID_CREDENTIALS = "Invalid username or password";
    public static final String PASSWORD_ALREADY_SET = "Password already set. Use change-password endpoint instead.";
    public static final String NO_PASSWORD_SET = "No password set. Use set-password endpoint instead.";
    public static final String INCORRECT_PASSWORD = "Current password is incorrect";
    public static final String CANNOT_DELETE_ADMIN = "Cannot delete admin account";
    
    public static String userNotFound(Long id) {
        return USER_NOT_FOUND + id;
    }
}
