package com.example.auth.repository;

import com.example.auth.domain.LoginInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface LoginInfoRepository extends JpaRepository<LoginInfo, UUID> {

    Optional<LoginInfo> findByEmail(String email);

    boolean existsByEmail(String email);
}
