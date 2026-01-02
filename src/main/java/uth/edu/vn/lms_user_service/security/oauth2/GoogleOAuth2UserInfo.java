package uth.edu.vn.lms_user_service.security.oauth2;

import uth.edu.vn.lms_user_service.entity.AuthProvider;

import java.util.Map;

/**
 * Google OAuth2 user info extractor
 */
public class GoogleOAuth2UserInfo extends OAuth2UserInfo {

    public GoogleOAuth2UserInfo(Map<String, Object> attributes) {
        super(attributes);
    }

    @Override
    public String getId() {
        return (String) attributes.get("sub");
    }

    @Override
    public String getName() {
        return (String) attributes.get("name");
    }

    @Override
    public String getEmail() {
        return (String) attributes.get("email");
    }

    @Override
    public String getImageUrl() {
        return (String) attributes.get("picture");
    }

    @Override
    public AuthProvider getProvider() {
        return AuthProvider.GOOGLE;
    }
}
