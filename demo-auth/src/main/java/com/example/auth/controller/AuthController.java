package com.example.auth.controller;

import com.example.auth.dto.SignupRequest;
import com.example.auth.dto.TokenResponse;
import com.example.auth.dto.UserInfoResponse;
import com.example.auth.service.AuthService;
import com.example.common.dto.ApiResponse;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
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

    /**
     * 회원가입
     */
    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<Void>> signup(@Valid @RequestBody SignupRequest request) {
        authService.signup(request);
        return ResponseEntity.ok(ApiResponse.success("회원가입이 완료되었습니다", null));
    }

    /**
     * 토큰 갱신
     */
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<TokenResponse>> refresh(
            @CookieValue(name = "refreshToken", required = false) String refreshTokenFromCookie,
            @RequestHeader(value = "Refresh-Token", required = false) String refreshTokenFromHeader,
            HttpServletResponse response) {

        String refreshToken = refreshTokenFromCookie != null ? refreshTokenFromCookie : refreshTokenFromHeader;

        if (refreshToken == null) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Refresh token이 필요합니다"));
        }

        TokenResponse tokenResponse = authService.refreshToken(refreshToken);

        response.addCookie(createCookie("accessToken", tokenResponse.getAccessToken(), (int) (accessTokenValidity / 1000)));
        response.addCookie(createCookie("refreshToken", tokenResponse.getRefreshToken(), (int) (refreshTokenValidity / 1000)));

        log.info("Tokens refreshed and set in cookies");
        return ResponseEntity.ok(ApiResponse.success("토큰이 갱신되었습니다", tokenResponse));
    }

    /**
     * 로그아웃
     */
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(HttpServletResponse response) {
        response.addCookie(createCookie("accessToken", "", 0));
        response.addCookie(createCookie("refreshToken", "", 0));

        log.info("User logged out, cookies cleared");
        return ResponseEntity.ok(ApiResponse.success("로그아웃이 완료되었습니다", null));
    }

    /**
     * 현재 사용자 정보 조회
     */
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserInfoResponse>> getCurrentUser(
            @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.ok(ApiResponse.success("로그인 정보 없음", null));
        }
        UserInfoResponse userInfo = authService.getCurrentUser(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("사용자 정보 조회 성공", userInfo));
    }

    /**
     * 쿠키 생성 헬퍼 메서드
     */
    private Cookie createCookie(String name, String value, int maxAge) {
        Cookie cookie = new Cookie(name, value);
        cookie.setHttpOnly(true);
        cookie.setSecure(cookieSecure);
        cookie.setPath("/");
        cookie.setMaxAge(maxAge);
        cookie.setAttribute("SameSite", "Lax");
        return cookie;
    }
}
