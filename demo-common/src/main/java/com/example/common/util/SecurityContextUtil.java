package com.example.common.util;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Optional;

/**
 * SecurityContext에서 인증된 사용자 정보를 가져오는 유틸리티 클래스
 */
public class SecurityContextUtil {

    private SecurityContextUtil() {
        // 유틸리티 클래스는 인스턴스화 방지
    }

    /**
     * 현재 인증된 사용자의 이메일을 반환
     *
     * @return 인증된 사용자의 이메일, 인증되지 않은 경우 null
     */
    public static String getCurrentUserEmail() {
        return getAuthentication()
                .map(auth -> {
                    Object principal = auth.getPrincipal();
                    if (principal instanceof UserDetails) {
                        return ((UserDetails) principal).getUsername();
                    }
                    return principal.toString();
                })
                .orElse(null);
    }

    /**
     * 현재 인증 정보를 반환
     *
     * @return Optional로 감싸진 Authentication 객체
     */
    public static Optional<Authentication> getAuthentication() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()
                && !"anonymousUser".equals(authentication.getPrincipal())) {
            return Optional.of(authentication);
        }
        return Optional.empty();
    }

    /**
     * 현재 사용자가 인증되었는지 확인
     *
     * @return 인증 여부
     */
    public static boolean isAuthenticated() {
        return getAuthentication().isPresent();
    }
}
