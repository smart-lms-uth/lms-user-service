package uth.edu.vn.lms_user_service.security.oauth2;

import uth.edu.vn.lms_user_service.entity.AuthProvider;

import java.util.Map;

/**
 * Abstract class to extract user info from OAuth2 providers
 */
public abstract class OAuth2UserInfo {

    protected Map<String, Object> attributes;

    public OAuth2UserInfo(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    public Map<String, Object> getAttributes() {
        return attributes;
    }

    public abstract String getId();
    public abstract String getName();
    public abstract String getEmail();
    public abstract String getImageUrl();
    public abstract AuthProvider getProvider();

    /**
     * Factory method to create OAuth2UserInfo based on provider
     */
    public static OAuth2UserInfo of(String registrationId, Map<String, Object> attributes) {
        return switch (registrationId.toLowerCase()) {
            case "google" -> new GoogleOAuth2UserInfo(attributes);
            default -> throw new IllegalArgumentException("Unsupported OAuth2 provider: " + registrationId);
        };
    }
}
