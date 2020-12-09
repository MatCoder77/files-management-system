package com.awscourse.filesmanagementsystem.infrastructure.configuration;


import com.awscourse.filesmanagementsystem.domain.user.entity.User;
import com.awscourse.filesmanagementsystem.domain.user.control.UserService;
import com.awscourse.filesmanagementsystem.infrastructure.security.UserInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

@Configuration
@EnableJpaAuditing
@RequiredArgsConstructor
public class AuditingConfig {

    private final UserService userService;

    @Bean
    public AuditorAware<User> auditorProvider() {
        return new SpringSecurityAuditAwareImpl();
    }

    class SpringSecurityAuditAwareImpl implements AuditorAware<User> {

        @Override
        public Optional<User> getCurrentAuditor() {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated() || authentication instanceof AnonymousAuthenticationToken) {
                return Optional.empty();
            }
            UserInfo userInfo = (UserInfo) authentication.getPrincipal();
            return Optional.ofNullable(userInfo.getId())
                    .map(userService::getExistingUser);
        }
    }

}
