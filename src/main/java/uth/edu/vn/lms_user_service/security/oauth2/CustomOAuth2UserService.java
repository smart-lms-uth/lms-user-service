package uth.edu.vn.lms_user_service.security.oauth2;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import uth.edu.vn.lms_user_service.entity.AuthProvider;
import uth.edu.vn.lms_user_service.entity.Role;
import uth.edu.vn.lms_user_service.entity.User;
import uth.edu.vn.lms_user_service.repository.UserRepository;

import java.util.Optional;

/**
 * Custom OAuth2 User Service to process OAuth2 login
 */
@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private static final Logger log = LoggerFactory.getLogger(CustomOAuth2UserService.class);

    private final UserRepository userRepository;

    public CustomOAuth2UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        log.info("OAuth2 login attempt from provider: {}", registrationId);

        return processOAuth2User(registrationId, oAuth2User);
    }

    private OAuth2User processOAuth2User(String registrationId, OAuth2User oAuth2User) {
        OAuth2UserInfo userInfo = OAuth2UserInfo.of(registrationId, oAuth2User.getAttributes());

        if (userInfo.getEmail() == null || userInfo.getEmail().isEmpty()) {
            throw new OAuth2AuthenticationException("Email not found from OAuth2 provider");
        }

        Optional<User> existingUser = userRepository.findByEmail(userInfo.getEmail());

        User user;
        if (existingUser.isPresent()) {
            user = existingUser.get();
            
            // Check if user signed up with different provider
            if (user.getAuthProvider() == AuthProvider.LOCAL) {
                // Link the OAuth2 account to existing local account
                user = updateExistingUser(user, userInfo);
                log.info("Linked OAuth2 account to existing local user: {}", user.getEmail());
            } else if (user.getAuthProvider() != userInfo.getProvider()) {
                throw new OAuth2AuthenticationException(
                        "You have already signed up with " + user.getAuthProvider() + 
                        ". Please use your " + user.getAuthProvider() + " account to login.");
            } else {
                // Update user info
                user = updateExistingUser(user, userInfo);
            }
        } else {
            // Register new user
            user = registerNewUser(userInfo);
            log.info("Registered new OAuth2 user: {}", user.getEmail());
        }

        return new CustomOAuth2User(user, oAuth2User.getAttributes());
    }

    private User registerNewUser(OAuth2UserInfo userInfo) {
        User user = new User();
        user.setEmail(userInfo.getEmail());
        user.setUsername(generateUsername(userInfo.getEmail()));
        user.setFullName(userInfo.getName());
        user.setAvatarUrl(userInfo.getImageUrl());
        user.setAuthProvider(userInfo.getProvider());
        user.setProviderId(userInfo.getId());
        user.setEmailVerified(true); // OAuth2 emails are verified
        user.setRole(Role.USER);
        user.setEnabled(true);
        // Password is null for OAuth2 users

        return userRepository.save(user);
    }

    private User updateExistingUser(User user, OAuth2UserInfo userInfo) {
        user.setFullName(userInfo.getName());
        user.setAvatarUrl(userInfo.getImageUrl());
        
        if (user.getAuthProvider() == AuthProvider.LOCAL) {
            user.setAuthProvider(userInfo.getProvider());
            user.setProviderId(userInfo.getId());
            user.setEmailVerified(true);
        }

        return userRepository.save(user);
    }

    private String generateUsername(String email) {
        String baseUsername = email.split("@")[0];
        String username = baseUsername;
        int counter = 1;

        while (userRepository.existsByUsername(username)) {
            username = baseUsername + counter++;
        }

        return username;
    }
}
