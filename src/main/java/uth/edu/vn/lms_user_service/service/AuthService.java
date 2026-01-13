package uth.edu.vn.lms_user_service.service;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uth.edu.vn.lms_user_service.security.JwtUtil;
import uth.edu.vn.lms_user_service.dto.*;
import uth.edu.vn.lms_user_service.entity.*;
import uth.edu.vn.lms_user_service.exception.ApiException;
import uth.edu.vn.lms_user_service.repository.EmailVerificationTokenRepository;
import uth.edu.vn.lms_user_service.repository.PasswordResetTokenRepository;
import uth.edu.vn.lms_user_service.repository.UserRepository;
import uth.edu.vn.lms_user_service.util.ErrorMessages;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.time.LocalDateTime;

@Service
public class AuthService implements IAuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final ActivityService activityService;
    private final TokenBlacklistService tokenBlacklistService;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final EmailVerificationTokenRepository emailVerificationTokenRepository;
    private final EmailService emailService;

    public AuthService(UserRepository userRepository, 
                       PasswordEncoder passwordEncoder, 
                       JwtUtil jwtUtil,
                       AuthenticationManager authenticationManager,
                       ActivityService activityService,
                       TokenBlacklistService tokenBlacklistService,
                       PasswordResetTokenRepository passwordResetTokenRepository,
                       EmailVerificationTokenRepository emailVerificationTokenRepository,
                       EmailService emailService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.authenticationManager = authenticationManager;
        this.activityService = activityService;
        this.tokenBlacklistService = tokenBlacklistService;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.emailVerificationTokenRepository = emailVerificationTokenRepository;
        this.emailService = emailService;
    }

    @Override
    @Transactional
    public UserResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw ApiException.conflict(ErrorMessages.USERNAME_EXISTS);
        }

        if (userRepository.existsByEmail(request.email())) {
            throw ApiException.conflict(ErrorMessages.EMAIL_EXISTS);
        }

        User user = new User();
        user.setUsername(request.username());
        user.setEmail(request.email());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setFullName(request.fullName());
        user.setPhoneNumber(request.phoneNumber());
        user.setRole(Role.STUDENT);
        user.setEnabled(true);
        user.setEmailVerified(false);

        User savedUser = userRepository.save(user);
        
        // Send verification email
        sendEmailVerification(savedUser);
        
        return UserResponse.fromUser(savedUser);
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.username(),
                        request.password()
                )
        );

        User user = (User) authentication.getPrincipal();
        
        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);
        
        // Log login activity
        activityService.logSystemActivity(user.getId(), ActivityType.LOGIN, "User logged in", 
                "{\"username\":\"" + user.getUsername() + "\",\"role\":\"" + user.getRole() + "\"}");
        
        return generateAuthResponse(user);
    }

    @Override
    public AuthResponse refreshToken(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            throw ApiException.badRequest("Refresh token is required");
        }
        
        String username;
        try {
            username = jwtUtil.extractUsername(refreshToken);
        } catch (Exception e) {
            throw ApiException.unauthorized("Invalid refresh token format");
        }
        
        if (username == null) {
            throw ApiException.unauthorized("Invalid refresh token");
        }
        
        if (!jwtUtil.isRefreshToken(refreshToken)) {
            throw ApiException.unauthorized("Invalid refresh token");
        }
        
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> ApiException.unauthorized("User not found"));
        
        if (!jwtUtil.validateToken(refreshToken, user)) {
            throw ApiException.unauthorized("Refresh token expired or invalid");
        }
        
        return generateAuthResponse(user);
    }
    
    @Override
    @Transactional
    public void logout(String accessToken, String refreshToken) {
        // Blacklist both tokens
        if (accessToken != null && !accessToken.isBlank()) {
            tokenBlacklistService.blacklistToken(accessToken, jwtUtil.getAccessTokenExpiration());
        }
        if (refreshToken != null && !refreshToken.isBlank()) {
            tokenBlacklistService.blacklistToken(refreshToken, jwtUtil.getRefreshTokenExpiration());
        }
        
        // Log logout activity
        try {
            String username = jwtUtil.extractUsername(accessToken != null ? accessToken : refreshToken);
            userRepository.findByUsername(username).ifPresent(user -> 
                activityService.logSystemActivity(user.getId(), ActivityType.LOGOUT, "User logged out", null));
        } catch (Exception ignored) {}
    }

    @Override
    @Transactional
    public void forgotPassword(ForgotPasswordRequest request) {
        userRepository.findByEmail(request.email()).ifPresent(user -> {
            // Delete old tokens
            passwordResetTokenRepository.deleteByUser(user);
            
            // Create new token
            String token = UUID.randomUUID().toString();
            PasswordResetToken resetToken = new PasswordResetToken(token, user, 30); // 30 minutes
            passwordResetTokenRepository.save(resetToken);
            
            // Send email
            emailService.sendPasswordResetEmail(user.getEmail(), token);
            
            activityService.logSystemActivity(user.getId(), ActivityType.PASSWORD_RESET, 
                    "Password reset requested", null);
        });
        // Always return success to prevent email enumeration
    }

    @Override
    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        if (!request.isPasswordMatch()) {
            throw ApiException.badRequest("Passwords do not match");
        }
        
        PasswordResetToken resetToken = passwordResetTokenRepository.findByTokenAndUsedFalse(request.token())
                .orElseThrow(() -> ApiException.badRequest("Invalid or expired reset token"));
        
        if (!resetToken.isValid()) {
            throw ApiException.badRequest("Reset token has expired");
        }
        
        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);
        
        resetToken.setUsed(true);
        passwordResetTokenRepository.save(resetToken);
        
        activityService.logSystemActivity(user.getId(), ActivityType.PASSWORD_SET, 
                "Password reset completed", null);
    }

    @Override
    public boolean validateResetToken(String token) {
        return passwordResetTokenRepository.findByTokenAndUsedFalse(token)
                .map(PasswordResetToken::isValid)
                .orElse(false);
    }

    @Override
    @Transactional
    public void verifyEmail(VerifyEmailRequest request) {
        EmailVerificationToken verificationToken = emailVerificationTokenRepository
                .findByTokenAndUsedFalse(request.token())
                .orElseThrow(() -> ApiException.badRequest("Invalid or expired verification token"));
        
        if (!verificationToken.isValid()) {
            throw ApiException.badRequest("Verification token has expired");
        }
        
        User user = verificationToken.getUser();
        user.setEmailVerified(true);
        userRepository.save(user);
        
        verificationToken.setUsed(true);
        emailVerificationTokenRepository.save(verificationToken);
    }

    @Override
    @Transactional
    public void resendVerificationEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> ApiException.notFound("User not found"));
        
        if (user.isEmailVerified()) {
            throw ApiException.badRequest("Email is already verified");
        }
        
        sendEmailVerification(user);
    }

    private void sendEmailVerification(User user) {
        emailVerificationTokenRepository.deleteByUser(user);
        
        String token = UUID.randomUUID().toString();
        EmailVerificationToken verificationToken = new EmailVerificationToken(token, user, 24); // 24 hours
        emailVerificationTokenRepository.save(verificationToken);
        
        emailService.sendEmailVerification(user.getEmail(), token);
    }
    
    private AuthResponse generateAuthResponse(User user) {
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("userId", user.getId());
        extraClaims.put("role", user.getRole().name());
        
        String accessToken = jwtUtil.generateAccessToken(extraClaims, user);
        String refreshToken = jwtUtil.generateRefreshToken(user);

        return new AuthResponse(
                accessToken,
                "Bearer",
                jwtUtil.getAccessTokenExpiration(),
                refreshToken,
                new AuthResponse.UserInfo(
                        user.getId(),
                        user.getUsername(),
                        user.getEmail(),
                        user.getFullName(),
                        user.getAuthorities().stream()
                                .map(GrantedAuthority::getAuthority)
                                .toList()
                )
        );
    }
}
