package com.example.auth.controller;

import com.example.auth.dto.LoginRequest;
import com.example.auth.dto.TokenResponse;
import com.example.auth.service.AuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Slf4j
@Controller
@RequiredArgsConstructor
public class PageController {

    private final AuthService authService;

    @Value("${jwt.access-token-validity}")
    private long accessTokenValidity;

    @Value("${jwt.refresh-token-validity}")
    private long refreshTokenValidity;

    @Value("${jwt.cookie.secure:false}")
    private boolean cookieSecure;

    // 로그인 성공 후 기본 리다이렉트 URL (demo-front Dashboard)
    @Value("${service.demo-front.prefix}")
    private String redirectPrefix;
    @Value("${service.demo-front.uri}")
    private String redirectUri;
    @Value("${service.demo-front.port}")
    private String redirectPort;

    private static final String DEFAULT_REDIRECT_PATH = "/dashboard";

    @GetMapping("/login")
    public String loginPage(@RequestParam(value = "error", required = false) String error,
                            @RequestParam(value = "message", required = false) String message,
                            @RequestParam(value = "redirect", required = false) String redirectUrl,
                            Model model) {
        if (error != null) {
            model.addAttribute("error", message != null ? message : "로그인에 실패했습니다.");
        }

        // redirect URL을 모델에 추가하여 폼에서 사용
        // 이미 전체 URL인 경우 그대로 사용, 아니면 buildUrl로 생성
        String targetUrl;
        if (redirectUrl != null && (redirectUrl.startsWith("http://") || redirectUrl.startsWith("https://"))) {
            targetUrl = redirectUrl;
        } else {
            targetUrl = buildUrl(redirectUrl);
        }
        model.addAttribute("redirectUrl", targetUrl);
        model.addAttribute("signUpUrl", buildUrl("signup"));
        return "login";
    }

    @PostMapping("/login")
    public String login(@RequestParam String email,
                        @RequestParam String password,
                        @RequestParam(value = "redirectUrl", required = false) String redirectUrl,
                        HttpServletResponse response,
                        RedirectAttributes redirectAttributes) {
        try {
            LoginRequest request = new LoginRequest(email, password);
            TokenResponse tokenResponse = authService.login(request);

            // Access Token 쿠키 설정
            Cookie accessTokenCookie = createCookie(
                    "accessToken",
                    tokenResponse.getAccessToken(),
                    (int) (accessTokenValidity / 1000),
                    true,
                    cookieSecure
            );

            // Refresh Token 쿠키 설정
            Cookie refreshTokenCookie = createCookie(
                    "refreshToken",
                    tokenResponse.getRefreshToken(),
                    (int) (refreshTokenValidity / 1000),
                    true,
                    cookieSecure
            );

            response.addCookie(accessTokenCookie);
            response.addCookie(refreshTokenCookie);

            log.info("Login successful for user: {}", email);

            // 로그인 성공 시 redirect URL로 이동 (없으면 기본 Dashboard)
            String targetUrl = (redirectUrl != null && !redirectUrl.isBlank()) ? redirectUrl : buildUrl(null);

            // 인증 성공 파라미터 추가 (Vue에서 localStorage 설정용)
            String separator = targetUrl.contains("?") ? "&" : "?";
            targetUrl = targetUrl + separator + "authenticated=true";

            return "redirect:" + targetUrl;

        } catch (Exception e) {
            log.error("Login failed for user: {}", email, e);
            redirectAttributes.addAttribute("error", true);
            redirectAttributes.addAttribute("message", e.getMessage());
            // 실패 시에도 redirect URL 유지
            if (redirectUrl != null && !redirectUrl.isBlank()) {
                redirectAttributes.addAttribute("redirect", redirectUrl);
            }
            return "redirect:/login";
        }
    }

    private Cookie createCookie(String name, String value, int maxAge, boolean httpOnly, boolean secure) {
        Cookie cookie = new Cookie(name, value);
        cookie.setHttpOnly(httpOnly);
        cookie.setSecure(secure);
        cookie.setPath("/");
        cookie.setMaxAge(maxAge);
        cookie.setAttribute("SameSite", "Lax");
        return cookie;
    }

    private String buildUrl(String path){
        String targetPath = (path == null || path.isEmpty()) ? DEFAULT_REDIRECT_PATH : path;
        // path가 /로 시작하지 않으면 추가
        if (!targetPath.startsWith("/")) {
            targetPath = "/" + targetPath;
        }
        return String.format("%s://%s:%s%s", redirectPrefix, redirectUri, redirectPort, targetPath);
    }
}
