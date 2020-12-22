package com.awscourse.filesmanagementsystem.infrastructure.startup;

import com.awscourse.filesmanagementsystem.domain.user.control.UserService;
import com.awscourse.filesmanagementsystem.domain.user.entity.User;
import com.awscourse.filesmanagementsystem.domain.user.entity.UserRole;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class SystemAccountHandler implements CommandLineRunner {

    private final String systemAccountUsername;
    private final String systemAccountEmail;
    private final String systemAccountPassword;
    private final UserService userService;

    public SystemAccountHandler(@Value("${app.system-account.username}") String systemAccountUsername,
                                @Value("${app.system-account.email}") String systemAccountEmail,
                                @Value("${app.system-account.password}")  String systemAccountPassword,
                                UserService userService) {
        this.systemAccountUsername = systemAccountUsername;
        this.systemAccountEmail = systemAccountEmail;
        this.systemAccountPassword = systemAccountPassword;
        this.userService = userService;
    }

    @Override
    public void run(String... args) {
        initializeSystemUserAccount();
    }

    public void initializeSystemUserAccount() {
        if (!userService.userExistsByUsername(systemAccountUsername)) {
            User defaultUser = User.builder()
                    .name(systemAccountUsername)
                    .surname(systemAccountUsername)
                    .username(systemAccountUsername)
                    .password(systemAccountPassword)
                    .email(systemAccountEmail)
                    .phoneNumber("000000000")
                    .role(UserRole.ROLE_ADMIN)
                    .build();
            userService.createUser(defaultUser);
        }
    }

}
