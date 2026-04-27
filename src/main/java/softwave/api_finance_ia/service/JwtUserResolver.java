package softwave.api_finance_ia.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import softwave.api_finance_ia.exception.UnauthorizedException;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

@Component
public class JwtUserResolver {

    private final String jwtSecret;

    public JwtUserResolver(@Value("${jwt.secret:}") String jwtSecret) {
        this.jwtSecret = jwtSecret;
    }

    public Long resolveUserIdOptional(HttpServletRequest request) {
        String token = extractBearerToken(request);
        if (token == null || token.isBlank() || jwtSecret == null || jwtSecret.isBlank()) {
            return null;
        }
        try {
            SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
            Jws<Claims> jws = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token);
            Object userIdClaim = jws.getPayload().get("id");
            if (userIdClaim == null) {
                return null;
            }
            if (userIdClaim instanceof Number number) {
                return number.longValue();
            }
            return Long.parseLong(userIdClaim.toString());
        } catch (Exception ex) {
            throw new UnauthorizedException("Token invalido ou expirado.");
        }
    }

    private String extractBearerToken(HttpServletRequest request) {
        String authorization = request.getHeader("Authorization");
        if (authorization == null || authorization.isBlank()) {
            return null;
        }
        if (!authorization.startsWith("Bearer ")) {
            return null;
        }
        return authorization.substring(7).trim();
    }
}
