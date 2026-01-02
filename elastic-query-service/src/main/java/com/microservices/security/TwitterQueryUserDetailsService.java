package com.microservices.security;

import com.microservices.service.QueryUserService;
import com.microservices.transformer.UserPermissionsToDetailTransformer;
import lombok.AllArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class TwitterQueryUserDetailsService implements UserDetailsService {
    private final QueryUserService queryUserService;
    private final UserPermissionsToDetailTransformer transformer;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return queryUserService
                .findAllPermissionsByUsername(username)
                .map(transformer::getUserDetails)
                .orElseThrow(() -> new UsernameNotFoundException("No user found with name: %s".formatted(username)));
    }
}