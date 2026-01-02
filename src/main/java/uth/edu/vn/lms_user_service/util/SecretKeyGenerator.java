package uth.edu.vn.lms_user_service.util;

import io.jsonwebtoken.Jwts;
import javax.crypto.SecretKey;
import java.util.Base64;

/**
 * Utility class để generate JWT Secret Key an toàn.
 * Chạy main method để generate key mới.
 */
public class SecretKeyGenerator {

    public static void main(String[] args) {
        System.out.println("=".repeat(60));
        System.out.println("JWT SECRET KEY GENERATOR");
        System.out.println("=".repeat(60));
        
        // Generate secure key using jjwt built-in method (HS512)
        SecretKey key = Jwts.SIG.HS512.key().build();
        String base64Key = Base64.getEncoder().encodeToString(key.getEncoded());
        
        System.out.println("\n🔐 Generated HS512 Secret Key (Base64 encoded):\n");
        System.out.println(base64Key);
        
        System.out.println("\n" + "=".repeat(60));
        System.out.println("📋 USAGE INSTRUCTIONS:");
        System.out.println("=".repeat(60));
        
        System.out.println("\n1. For Docker (docker-compose.yml):");
        System.out.println("   environment:");
        System.out.println("     JWT_SECRET: " + base64Key);
        
        System.out.println("\n2. For .env file:");
        System.out.println("   JWT_SECRET=" + base64Key);
        
        System.out.println("\n3. For application.properties (NOT RECOMMENDED for production):");
        System.out.println("   jwt.secret=" + base64Key);
        
        System.out.println("\n⚠️  SECURITY WARNINGS:");
        System.out.println("   - NEVER commit secret keys to version control");
        System.out.println("   - Use environment variables in production");
        System.out.println("   - Rotate keys periodically");
        System.out.println("   - Keep different keys for different environments");
        
        System.out.println("\n" + "=".repeat(60));
    }
}
