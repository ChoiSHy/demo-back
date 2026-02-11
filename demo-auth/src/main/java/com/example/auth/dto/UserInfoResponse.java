package com.example.auth.dto;

import com.example.auth.domain.UserInfo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserInfoResponse {

    private UUID userId;
    private String userName;
    private String email;
    private LocalDate birthDate;

    public static UserInfoResponse from(UserInfo userInfo) {
        return UserInfoResponse.builder()
                .userId(userInfo.getUserId())
                .userName(userInfo.getUserName())
                .email(userInfo.getLoginInfo().getEmail())
                .birthDate(userInfo.getBirthDate())
                .build();
    }
}
