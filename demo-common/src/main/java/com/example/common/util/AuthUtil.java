package com.example.common.util;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * 인증된 사용자 정보를 가져오는 통합 유틸리티 클래스
 * SecurityContext 또는 JWT 토큰에서 사용자 정보를 추출
 */
public class AuthUtil {

    private AuthUtil() {
        // 유틸리티 클래스는 인스턴스화 방지
    }

    /**
     * 현재 요청의 JWT 토큰에서 userId를 추출
     * SecurityContext에 저장되지 않은 경우에도 사용 가능
     *
     * @return userId (UUID 문자열), 토큰이 없거나 유효하지 않은 경우 null
     */
    public static String getCurrentUserId() {
        String token = getTokenFromRequest();
        if (token != null) {
            return JwtUtil.getUserIdFromToken(token);
        }
        return null;
    }

    /**
     * 현재 인증된 사용자의 이메일을 반환
     * 우선순위: 1. SecurityContext, 2. JWT 토큰
     *
     * @return 인증된 사용자의 이메일, 인증되지 않은 경우 null
     */
    public static String getCurrentUserEmail() {
        // 1. SecurityContext에서 가져오기 시도
        String email = SecurityContextUtil.getCurrentUserEmail();
        if (email != null) {
            return email;
        }

        // 2. JWT 토큰에서 가져오기 시도
        String token = getTokenFromRequest();
        if (token != null) {
            return JwtUtil.getEmailFromToken(token);
        }

        return null;
    }

    /**
     * 현재 요청의 JWT 토큰에서 권한 정보를 추출
     *
     * @return 권한 문자열 (쉼표로 구분), 토큰이 없거나 유효하지 않은 경우 null
     */
    public static String getCurrentUserAuthorities() {
        String token = getTokenFromRequest();
        if (token != null) {
            return JwtUtil.getAuthoritiesFromToken(token);
        }
        return null;
    }

    /**
     * 현재 HTTP 요청에서 JWT 토큰을 추출
     *
     * @return JWT 토큰, 없는 경우 null
     */
    private static String getTokenFromRequest() {
        try {
            ServletRequestAttributes attributes =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                String authorizationHeader = request.getHeader("Authorization");
                return JwtUtil.extractTokenFromHeader(authorizationHeader);
            }
        } catch (Exception e) {
            // RequestContext가 없는 경우 (비동기 작업 등)
            return null;
        }
        return null;
    }

    /**
     * 현재 사용자가 인증되었는지 확인
     *
     * @return 인증 여부
     */
    public static boolean isAuthenticated() {
        return SecurityContextUtil.isAuthenticated() || getCurrentUserId() != null;
    }

    /**
     * 현재 사용자 정보를 포함한 UserContext 객체 반환
     *
     * @return UserContext 객체
     */
    public static UserContext getCurrentUserContext() {
        return UserContext.builder()
                .userId(getCurrentUserId())
                .email(getCurrentUserEmail())
                .authorities(getCurrentUserAuthorities())
                .build();
    }

    /**
     * 사용자 컨텍스트를 담는 DTO 클래스
     */
    public static class UserContext {
        private final String userId;
        private final String email;
        private final String authorities;

        private UserContext(String userId, String email, String authorities) {
            this.userId = userId;
            this.email = email;
            this.authorities = authorities;
        }

        public static Builder builder() {
            return new Builder();
        }

        public String getUserId() {
            return userId;
        }

        public String getEmail() {
            return email;
        }

        public String getAuthorities() {
            return authorities;
        }

        public static class Builder {
            private String userId;
            private String email;
            private String authorities;

            public Builder userId(String userId) {
                this.userId = userId;
                return this;
            }

            public Builder email(String email) {
                this.email = email;
                return this;
            }

            public Builder authorities(String authorities) {
                this.authorities = authorities;
                return this;
            }

            public UserContext build() {
                return new UserContext(userId, email, authorities);
            }
        }
    }
}
