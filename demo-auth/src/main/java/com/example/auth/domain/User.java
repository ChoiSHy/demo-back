package com.example.auth.domain;

import com.example.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "tbl_users")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "user_id", nullable = false, updatable = false, columnDefinition = "BINARY(16)")
    private UUID userId;

    @Column(nullable = false, unique = true, length = 100)
    private String userEmail;

    @Column(nullable = false)
    private String userPassword;

    @Column(nullable = false, length = 50)
    private String userName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserRole userRole;

    public void updatePassword(String password) {
        this.userPassword = password;
    }
}
