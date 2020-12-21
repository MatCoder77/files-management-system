package com.awscourse.filesmanagementsystem.infrastructure.configuration;

import com.awscourse.filesmanagementsystem.domain.user.entity.User;
import com.awscourse.filesmanagementsystem.domain.user.control.UserService;
import com.awscourse.filesmanagementsystem.infrastructure.security.UserInfo;
import com.awscourse.filesmanagementsystem.infrastructure.security.UserInfoProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

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
            return UserInfoProvider.getAuthenticatedUser()
                    .map(UserInfo::getId)
                    .map(userService::getExistingUser);
        }
    }

}
