package com.example.auth.controller;

import com.example.auth.dto.LoginRequest;
import com.example.auth.dto.SignupRequest;
import com.example.auth.dto.TokenResponse;
import com.example.auth.service.AuthService;
import com.example.common.dto.ApiResponse;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("${base.url}/sign")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Value("${jwt.access-token-validity}")
    private long accessTokenValidity;

    @Value("${jwt.refresh-token-validity}")
    private long refreshTokenValidity;

    @Value("${jwt.cookie.secure:false}")
    private boolean cookieSecure;

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<Void>> signup(@Valid @RequestBody SignupRequest request) {
        authService.signup(request);
        return ResponseEntity.ok(ApiResponse.success("회원가입이 완료되었습니다", null));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<TokenResponse>> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletResponse response) {
        TokenResponse tokenResponse = authService.login(request);

        // Access Token 쿠키 설정
        Cookie accessTokenCookie = createCookie(
                "accessToken",
                tokenResponse.getAccessToken(),
                (int) (accessTokenValidity / 1000), // 밀리초를 초로 변환
                true,         // HttpOnly
                cookieSecure  // Secure (설정 파일에서 관리)
        );

        // Refresh Token 쿠키 설정
        Cookie refreshTokenCookie = createCookie(
                "refreshToken",
                tokenResponse.getRefreshToken(),
                (int) (refreshTokenValidity / 1000), // 밀리초를 초로 변환
                true,         // HttpOnly
                cookieSecure  // Secure (설정 파일에서 관리)
        );

        response.addCookie(accessTokenCookie);
        response.addCookie(refreshTokenCookie);

        log.info("Tokens set in cookies for user: {}", request.getEmail());
        return ResponseEntity.ok(ApiResponse.success("로그인이 완료되었습니다", tokenResponse));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<TokenResponse>> refresh(
            @CookieValue(name = "refreshToken", required = false) String refreshTokenFromCookie,
            @RequestHeader(value = "Refresh-Token", required = false) String refreshTokenFromHeader,
            HttpServletResponse response) {

        // 쿠키 또는 헤더에서 refresh token 가져오기
        String refreshToken = refreshTokenFromCookie != null ? refreshTokenFromCookie : refreshTokenFromHeader;

        if (refreshToken == null) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Refresh token이 필요합니다"));
        }

        TokenResponse tokenResponse = authService.refreshToken(refreshToken);

        // 새로운 토큰을 쿠키에 설정
        Cookie accessTokenCookie = createCookie(
                "accessToken",
                tokenResponse.getAccessToken(),
                (int) (accessTokenValidity / 1000),
                true,
                cookieSecure
        );

        Cookie refreshTokenCookie = createCookie(
                "refreshToken",
                tokenResponse.getRefreshToken(),
                (int) (refreshTokenValidity / 1000),
                true,
                cookieSecure
        );

        response.addCookie(accessTokenCookie);
        response.addCookie(refreshTokenCookie);

        log.info("Tokens refreshed and set in cookies");
        return ResponseEntity.ok(ApiResponse.success("토큰이 갱신되었습니다", tokenResponse));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(HttpServletResponse response) {
        // 쿠키 삭제 (maxAge를 0으로 설정)
        Cookie accessTokenCookie = createCookie("accessToken", "", 0, true, cookieSecure);
        Cookie refreshTokenCookie = createCookie("refreshToken", "", 0, true, cookieSecure);

        response.addCookie(accessTokenCookie);
        response.addCookie(refreshTokenCookie);

        log.info("User logged out, cookies cleared");
        return ResponseEntity.ok(ApiResponse.success("로그아웃이 완료되었습니다", null));
    }

    /**
     * 쿠키 생성 헬퍼 메서드
     */
    private Cookie createCookie(String name, String value, int maxAge, boolean httpOnly, boolean secure) {
        Cookie cookie = new Cookie(name, value);
        cookie.setHttpOnly(httpOnly);  // XSS 공격 방지
        cookie.setSecure(secure);      // HTTPS에서만 전송 (개발 환경에서는 false로 설정 가능)
        cookie.setPath("/");           // 모든 경로에서 접근 가능
        cookie.setMaxAge(maxAge);      // 쿠키 만료 시간 (초 단위)
        // cookie.setDomain("yourdomain.com"); // 필요시 도메인 설정
        cookie.setAttribute("SameSite", "Lax"); // 개발 환경에서 cross-origin 요청 허용

        return cookie;
    }
}
