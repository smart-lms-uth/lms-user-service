package uth.edu.vn.lms_user_service.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uth.edu.vn.lms_user_service.config.JwtUtil;
import uth.edu.vn.lms_user_service.dto.AuthResponse;
import uth.edu.vn.lms_user_service.dto.LoginRequest;
import uth.edu.vn.lms_user_service.dto.RegisterRequest;
import uth.edu.vn.lms_user_service.dto.UserResponse;
import uth.edu.vn.lms_user_service.entity.Role;
import uth.edu.vn.lms_user_service.entity.User;
import uth.edu.vn.lms_user_service.exception.ApiException;
import uth.edu.vn.lms_user_service.repository.UserRepository;

import java.util.HashMap;
import java.util.Map;

import java.time.LocalDateTime;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    public AuthService(UserRepository userRepository, 
                       PasswordEncoder passwordEncoder, 
                       JwtUtil jwtUtil,
                       AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.authenticationManager = authenticationManager;
    }

    @Transactional
    public UserResponse register(RegisterRequest request) {
        log.info("Registering new user: {}", request.username());
        
        // Check if username exists
        if (userRepository.existsByUsername(request.username())) {
            throw ApiException.conflict("Username already exists");
        }

        // Check if email exists
        if (userRepository.existsByEmail(request.email())) {
            throw ApiException.conflict("Email already exists");
        }

        // Create new user
        User user = new User();
        user.setUsername(request.username());
        user.setEmail(request.email());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setFullName(request.fullName());
        user.setPhoneNumber(request.phoneNumber());
        user.setRole(Role.STUDENT); // Mặc định đăng ký là STUDENT
        user.setEnabled(true);

        User savedUser = userRepository.save(user);
        log.info("User registered successfully: {}", savedUser.getUsername());
        
        return UserResponse.fromUser(savedUser);
    }

    public AuthResponse login(LoginRequest request) {
        log.info("Authenticating user: {}", request.username());
        
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.username(),
                        request.password()
                )
        );

        User user = (User) authentication.getPrincipal();
        
        // Cập nhật lastLoginAt
        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);
        
        // Add userId and role to JWT claims
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("userId", user.getId());
        extraClaims.put("role", user.getRole().name());
        
        String token = jwtUtil.generateToken(extraClaims, user);

        log.info("User authenticated successfully: {}", user.getUsername());

        return new AuthResponse(
                token,
                jwtUtil.getExpirationTime(),
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
