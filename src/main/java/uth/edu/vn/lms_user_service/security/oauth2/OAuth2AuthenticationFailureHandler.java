package uth.edu.vn.lms_user_service.security.oauth2;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;
import uth.edu.vn.lms_user_service.dto.ApiResponse;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Handler for failed OAuth2 authentication
 */
@Component
public class OAuth2AuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler {

    private static final Logger log = LoggerFactory.getLogger(OAuth2AuthenticationFailureHandler.class);

    private final ObjectMapper objectMapper;

    public OAuth2AuthenticationFailureHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, 
                                        HttpServletResponse response,
                                        AuthenticationException exception) throws IOException {
        
        log.error("OAuth2 authentication failed: {}", exception.getMessage());

        String frontendRedirectUrl = "http://localhost:3000/oauth2/callback";

        String targetUrl = UriComponentsBuilder.fromUriString(frontendRedirectUrl)
                .queryParam("error", URLEncoder.encode(exception.getMessage(), StandardCharsets.UTF_8))
                .build()
                .toUriString();

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}
