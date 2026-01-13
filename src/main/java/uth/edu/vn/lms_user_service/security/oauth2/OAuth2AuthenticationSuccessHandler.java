package uth.edu.vn.lms_user_service.security.oauth2;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;
import uth.edu.vn.lms_user_service.security.JwtUtil;
import uth.edu.vn.lms_user_service.entity.User;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Component
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtUtil jwtUtil;

    public OAuth2AuthenticationSuccessHandler(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, 
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        
        CustomOAuth2User oAuth2User = (CustomOAuth2User) authentication.getPrincipal();
        User user = oAuth2User.getUser();

        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("userId", user.getId());
        extraClaims.put("role", user.getRole().name());
        
        String token = jwtUtil.generateToken(extraClaims, user);

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
