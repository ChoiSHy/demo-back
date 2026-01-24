package com.example.common.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

/**
 * JWT 토큰에서 정보를 추출하는 유틸리티 클래스
 */
@Component
public class JwtUtil {

    private static String secretKey;

    @Value("${jwt.secret}")
    public void setSecretKey(String secret) {
        JwtUtil.secretKey = secret;
    }

    private JwtUtil() {
        // Spring Bean으로만 생성
    }

    /**
     * JWT 토큰에서 userId를 추출
     *
     * @param token JWT 토큰
     * @return userId (UUID 문자열)
     */
    public static String getUserIdFromToken(String token) {
        Claims claims = parseClaims(token);
        return (String) claims.get("userId");
    }

    /**
     * JWT 토큰에서 이메일을 추출
     *
     * @param token JWT 토큰
     * @return 이메일
     */
    public static String getEmailFromToken(String token) {
        Claims claims = parseClaims(token);
        return claims.getSubject();
    }

    /**
     * JWT 토큰에서 권한 정보를 추출
     *
     * @param token JWT 토큰
     * @return 권한 문자열 (쉼표로 구분)
     */
    public static String getAuthoritiesFromToken(String token) {
        Claims claims = parseClaims(token);
        return (String) claims.get("authorities");
    }

    /**
     * JWT 토큰 파싱
     *
     * @param token JWT 토큰
     * @return Claims
     */
    private static Claims parseClaims(String token) {
        try {
            SecretKey key = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
            return Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException e) {
            return e.getClaims();
        }
    }

    /**
     * Authorization 헤더에서 Bearer 토큰 추출
     *
     * @param authorizationHeader Authorization 헤더 값
     * @return JWT 토큰 (Bearer 제거), 형식이 잘못된 경우 null
     */
    public static String extractTokenFromHeader(String authorizationHeader) {
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            return authorizationHeader.substring(7);
        }
        return null;
    }
}
