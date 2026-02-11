package com.example.auth.service;

import com.example.auth.domain.LoginInfo;
import com.example.auth.domain.UserInfo;
import com.example.auth.domain.UserRole;
import com.example.auth.dto.LoginRequest;
import com.example.auth.dto.SignupRequest;
import com.example.auth.dto.TokenResponse;
import com.example.auth.dto.UserInfoResponse;
import com.example.auth.repository.LoginInfoRepository;
import com.example.auth.repository.UserInfoRepository;
import com.example.common.exception.BusinessException;
import com.example.common.exception.ErrorCode;
import com.example.security.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserInfoRepository userInfoRepository;
    private final LoginInfoRepository loginInfoRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final LoginSessionService loginSessionService;

    @Transactional
    public void signup(SignupRequest request) {
        if (loginInfoRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException(ErrorCode.DUPLICATE_EMAIL);
        }

        LoginInfo loginInfo = LoginInfo.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .userRole(UserRole.USER)
                .build();

        UserInfo userInfo = UserInfo.builder()
                .userName(request.getName())
                .birthDate(request.getBirthDate())
                .loginInfo(loginInfo)
                .build();

        userInfoRepository.save(userInfo);
        log.info("User signed up successfully: {}", request.getEmail());
    }

    @Transactional(readOnly = true)
    public TokenResponse login(LoginRequest request) {
        LoginInfo loginInfo = loginInfoRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_CREDENTIALS));

        if (!passwordEncoder.matches(request.getPassword(), loginInfo.getPassword())) {
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
        }

        // UserInfo를 로드하여 userId 가져오기
        UserInfo userInfo = userInfoRepository.findByLoginInfo_Email(request.getEmail())
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND));

        String accessToken = jwtTokenProvider.createAccessToken(
                loginInfo.getEmail(),
                userInfo.getUserId().toString(),
                Collections.singletonList(new SimpleGrantedAuthority(loginInfo.getUserRole().getKey()))
        );

        String refreshToken = jwtTokenProvider.createRefreshToken(loginInfo.getEmail());

        // Redis에 로그인 세션 저장
        loginSessionService.saveLoginSession(
                loginInfo.getEmail(),
                accessToken,
                refreshToken
        );

        log.info("User logged in successfully: {}", request.getEmail());
        return TokenResponse.of(accessToken, refreshToken);
    }

    @Transactional(readOnly = true)
    public TokenResponse refreshToken(String refreshToken) {
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new BusinessException(ErrorCode.INVALID_TOKEN);
        }

        String email = jwtTokenProvider.getEmailFromToken(refreshToken);
        LoginInfo loginInfo = loginInfoRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND));

        // UserInfo를 로드하여 userId 가져오기
        UserInfo userInfo = userInfoRepository.findByLoginInfo_Email(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND));

        String newAccessToken = jwtTokenProvider.createAccessToken(
                loginInfo.getEmail(),
                userInfo.getUserId().toString(),
                Collections.singletonList(new SimpleGrantedAuthority(loginInfo.getUserRole().getKey()))
        );

        String newRefreshToken = jwtTokenProvider.createRefreshToken(loginInfo.getEmail());

        log.info("Token refreshed successfully: {}", email);
        return TokenResponse.of(newAccessToken, newRefreshToken);
    }

    @Transactional(readOnly = true)
    public UserInfoResponse getCurrentUser(String email) {
        UserInfo userInfo = userInfoRepository.findByLoginInfo_Email(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND));
        return UserInfoResponse.from(userInfo);
    }
}
