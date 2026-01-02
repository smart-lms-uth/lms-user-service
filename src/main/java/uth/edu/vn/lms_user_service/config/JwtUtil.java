package uth.edu.vn.lms_user_service.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
public class JwtUtil {

    private static final Logger log = LoggerFactory.getLogger(JwtUtil.class);

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.expiration:86400000}")
    private long jwtExpiration; // 24 hours in milliseconds

    @Value("${jwt.issuer:lms-user-service}")
    private String issuer;

    private SecretKey signingKey;

    @PostConstruct
    public void init() {
        // Validate secret key on startup
        if (secretKey == null || secretKey.isBlank()) {
            throw new IllegalStateException("JWT secret key is not configured. Set JWT_SECRET environment variable.");
        }
        
        try {
            byte[] keyBytes = Decoders.BASE64.decode(secretKey);
            if (keyBytes.length < 64) { // HS512 requires at least 512 bits = 64 bytes
                throw new IllegalStateException("JWT secret key is too short. Use SecretKeyGenerator to generate a secure key.");
            }
            this.signingKey = Keys.hmacShaKeyFor(keyBytes);
            log.info("JWT configuration initialized successfully");
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException("Invalid JWT secret key format. Must be Base64 encoded.", e);
        }
    }

    public String generateToken(UserDetails userDetails) {
        return generateToken(new HashMap<>(), userDetails);
    }

    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        return Jwts.builder()
                .claims(extraClaims)
                .subject(userDetails.getUsername())
                .issuer(issuer)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .signWith(signingKey, Jwts.SIG.HS512)
                .compact();
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public boolean isTokenExpired(String token) {
        try {
            return extractExpiration(token).before(new Date());
        } catch (ExpiredJwtException e) {
            return true;
        }
    }

    public boolean validateToken(String token, UserDetails userDetails) {
        try {
            final String username = extractUsername(token);
            return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("JWT validation failed: {}", e.getMessage());
            return false;
        }
    }

    public long getExpirationTime() {
        return jwtExpiration;
    }
}
