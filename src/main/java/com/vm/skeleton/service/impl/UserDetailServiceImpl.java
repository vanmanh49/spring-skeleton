package com.vm.skeleton.service.impl;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.ObjectUtils;
import org.springframework.context.annotation.Primary;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vm.skeleton.entity.Role;
import com.vm.skeleton.entity.User;
import com.vm.skeleton.repository.UserDetailRepository;

import lombok.RequiredArgsConstructor;

@Service
@Primary
@RequiredArgsConstructor
public class UserDetailServiceImpl implements UserDetailsService {

    private final UserDetailRepository userDetailRepository;

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<User> userOptional = userDetailRepository.findByUserName(username);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            List<Role> roles = user.getRoles();
            Set<GrantedAuthority> authorities = Collections.emptySet();
            if (!ObjectUtils.isEmpty(roles)) {
                authorities = roles.stream().map(r -> new SimpleGrantedAuthority(r.getRoleCode()))
                        .collect(Collectors.toSet());
            }
            return new org.springframework.security.core.userdetails.User(user.getUserName(), user.getHashedPassword(),
                    authorities);
        }
        throw new UsernameNotFoundException("username not found");
    }

}
