package com.awscourse.filesmanagementsystem.infrastructure.security;

import io.jsonwebtoken.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
@Slf4j
public class TokenHandler {

    private final String jwtSecret;
    private final int jwtExpirationInMs;

    public TokenHandler(@Value("${app.jwtSecret}") String jwtSecret,
                        @Value("${app.jwtExpirationInMs}") int jwtExpirationInMs) {
        this.jwtSecret = jwtSecret;
        this.jwtExpirationInMs = jwtExpirationInMs;
    }

    public String generateToken(Authentication authentication) {
        UserInfo userInfo = (UserInfo) authentication.getPrincipal();
        return Jwts.builder()
                .setSubject(Long.toString(userInfo.getId()))
                .setIssuedAt(new Date())
                .setExpiration(getTokenExpirationDate())
                .signWith(SignatureAlgorithm.HS512, jwtSecret)
                .compact();
    }

    private Date getTokenExpirationDate() {
        Date currentDate = new Date();
        return new Date(currentDate.getTime() + jwtExpirationInMs);
    }

    public Long getUserIdFromJWT(String token) {
        Claims claims = Jwts.parser()
                .setSigningKey(jwtSecret)
                .parseClaimsJws(token)
                .getBody();
        return Long.parseLong(claims.getSubject());
    }

    public boolean validateToken(String authToken) {
        try {
            Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(authToken);
            return true;
        } catch (SignatureException ex) {
            log.error("Invalid JWT signature");
        } catch (MalformedJwtException ex) {
            log.error("Invalid JWT accessToken");
        } catch (ExpiredJwtException ex) {
            log.error("Expired JWT accessToken");
        } catch (UnsupportedJwtException ex) {
            log.error("Unsupported JWT accessToken");
        } catch (IllegalArgumentException ex) {
            log.error("JWT claims string is empty.");
        }
        return false;
    }
}
