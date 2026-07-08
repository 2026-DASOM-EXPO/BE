package com.worksafe.backend.domain.auth.service;

import com.worksafe.backend.domain.auth.entity.User;
import com.worksafe.backend.domain.auth.repository.UserRepository;
import com.worksafe.backend.global.common.exception.BusinessException;
import com.worksafe.backend.global.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String loginId) throws UsernameNotFoundException {
        User user = userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        return new org.springframework.security.core.userdetails.User(
                user.getLoginId(),
                user.getPassword(),
                user.isEnabled(),
                true,
                true,
                true,
                List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()))
        );
    }
}
