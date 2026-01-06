package uth.edu.vn.lms_user_service.security.oauth2;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;
import uth.edu.vn.lms_user_service.config.JwtUtil;
import uth.edu.vn.lms_user_service.entity.User;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Handler for successful OAuth2 authentication
 * Generates JWT token and redirects to frontend with token
 */
@Component
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private static final Logger log = LoggerFactory.getLogger(OAuth2AuthenticationSuccessHandler.class);

    private final JwtUtil jwtUtil;
    private final ObjectMapper objectMapper;

    public OAuth2AuthenticationSuccessHandler(JwtUtil jwtUtil, ObjectMapper objectMapper) {
        this.jwtUtil = jwtUtil;
        this.objectMapper = objectMapper;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, 
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        
        CustomOAuth2User oAuth2User = (CustomOAuth2User) authentication.getPrincipal();
        User user = oAuth2User.getUser();

        // Add userId and role to JWT claims
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("userId", user.getId());
        extraClaims.put("role", user.getRole().name());
        
        String token = jwtUtil.generateToken(extraClaims, user);
        log.info("OAuth2 login successful for user: {}", user.getEmail());

        // Option 1: Redirect to frontend with token in URL (for web apps)
        String frontendRedirectUrl = determineFrontendUrl(request);
        
        String targetUrl = UriComponentsBuilder.fromUriString(frontendRedirectUrl)
                .queryParam("token", token)
                .queryParam("type", "Bearer")
                .queryParam("expiresIn", jwtUtil.getExpirationTime())
                .build()
                .toUriString();

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }

    private String determineFrontendUrl(HttpServletRequest request) {
        // Get redirect_uri from session or use default
        String redirectUri = (String) request.getSession().getAttribute("redirect_uri");
        
        if (redirectUri != null && !redirectUri.isEmpty()) {
            return redirectUri;
        }

        // Default frontend URL - Angular runs on port 4200
        return "http://localhost:4200/oauth2/callback";
    }
}
