package com.example.auth.service;

import com.example.auth.domain.User;
import com.example.auth.domain.UserRole;
import com.example.auth.dto.LoginRequest;
import com.example.auth.dto.SignupRequest;
import com.example.auth.dto.TokenResponse;
import com.example.auth.repository.UserRepository;
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

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Transactional
    public void signup(SignupRequest request) {
        if (userRepository.existsByUserEmail(request.getEmail())) {
            throw new BusinessException(ErrorCode.DUPLICATE_EMAIL);
        }

        User user = User.builder()
                .userEmail(request.getEmail())
                .userPassword(passwordEncoder.encode(request.getPassword()))
                .userName(request.getName())
                .userRole(UserRole.USER)
                .build();

        userRepository.save(user);
        log.info("User signed up successfully: {}", request.getEmail());
    }

    @Transactional(readOnly = true)
    public TokenResponse login(LoginRequest request) {
        User user = userRepository.findByUserEmail(request.getEmail())
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_CREDENTIALS));

        if (!passwordEncoder.matches(request.getPassword(), user.getUserPassword())) {
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
        }

        String accessToken = jwtTokenProvider.createAccessToken(
                user.getUserEmail(),
                Collections.singletonList(new SimpleGrantedAuthority(user.getUserRole().getKey()))
        );

        String refreshToken = jwtTokenProvider.createRefreshToken(user.getUserEmail());

        log.info("User logged in successfully: {}", request.getEmail());
        return TokenResponse.of(accessToken, refreshToken);
    }

    @Transactional(readOnly = true)
    public TokenResponse refreshToken(String refreshToken) {
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new BusinessException(ErrorCode.INVALID_TOKEN);
        }

        String email = jwtTokenProvider.getEmailFromToken(refreshToken);
        User user = userRepository.findByUserEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND));

        String newAccessToken = jwtTokenProvider.createAccessToken(
                user.getUserEmail(),
                Collections.singletonList(new SimpleGrantedAuthority(user.getUserRole().getKey()))
        );

        String newRefreshToken = jwtTokenProvider.createRefreshToken(user.getUserEmail());

        log.info("Token refreshed successfully: {}", email);
        return TokenResponse.of(newAccessToken, newRefreshToken);
    }
}
