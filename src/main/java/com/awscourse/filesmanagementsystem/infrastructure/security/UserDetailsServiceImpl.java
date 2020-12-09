package com.awscourse.filesmanagementsystem.infrastructure.security;


import com.awscourse.filesmanagementsystem.domain.user.entity.User;
import com.awscourse.filesmanagementsystem.domain.user.control.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.text.MessageFormat;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    private static final String USER_WITH_LOGIN_NOT_FOUND_MSG = "User with login {0} not found";
    private static final String USER_WITH_ID_NOT_FOUND = "User with id {0} not found";

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String usernameOrEmail) {
        User user = userRepository.findByUsernameOrEmail(usernameOrEmail, usernameOrEmail)
                .orElseThrow(() -> new UsernameNotFoundException(MessageFormat.format(USER_WITH_LOGIN_NOT_FOUND_MSG, usernameOrEmail)));
        return UserInfo.fromUser(user);
    }

    @Transactional
    public UserDetails loadUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException(MessageFormat.format(USER_WITH_ID_NOT_FOUND, id)));
        return UserInfo.fromUser(user);
    }

}

