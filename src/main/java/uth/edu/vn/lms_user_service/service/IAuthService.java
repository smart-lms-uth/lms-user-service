package uth.edu.vn.lms_user_service.service;

import uth.edu.vn.lms_user_service.dto.*;

public interface IAuthService {
    
    UserResponse register(RegisterRequest request);
    
    AuthResponse login(LoginRequest request);
    
    AuthResponse refreshToken(String refreshToken);
    
    void logout(String accessToken, String refreshToken);
    
    void forgotPassword(ForgotPasswordRequest request);
    
    void resetPassword(ResetPasswordRequest request);
    
    boolean validateResetToken(String token);
    
    void verifyEmail(VerifyEmailRequest request);
    
    void resendVerificationEmail(String email);
}
