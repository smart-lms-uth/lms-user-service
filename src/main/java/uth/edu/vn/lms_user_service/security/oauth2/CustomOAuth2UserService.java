package uth.edu.vn.lms_user_service.security.oauth2;

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

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    public CustomOAuth2UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
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
            
            if (user.getAuthProvider() == AuthProvider.LOCAL) {
                user = updateExistingUser(user, userInfo);
            } else if (user.getAuthProvider() != userInfo.getProvider()) {
                throw new OAuth2AuthenticationException(
                        "You have already signed up with " + user.getAuthProvider() + 
                        ". Please use your " + user.getAuthProvider() + " account to login.");
            } else {
                user = updateExistingUser(user, userInfo);
            }
        } else {
            user = registerNewUser(userInfo);
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
        user.setRole(Role.STUDENT); // Mặc định đăng ký là STUDENT
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
