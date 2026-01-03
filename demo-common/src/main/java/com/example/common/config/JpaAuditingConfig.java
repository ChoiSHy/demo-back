package com.example.common.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import java.util.Optional;

@Configuration
@EnableJpaAuditing
public class JpaAuditingConfig {

    /**
     * 기본 AuditorAware 구현
     * 다른 모듈에서 AuditorAware를 제공하지 않는 경우에만 사용됩니다.
     * demo-security 모듈에서 Security Context 기반 AuditorAware를 제공할 수 있습니다.
     */
    @Bean
    @ConditionalOnMissingBean(AuditorAware.class)
    public AuditorAware<String> auditorProvider() {
        return () -> Optional.of("system");
    }
}
