package com.microservices.service.impl;

import com.microservices.dataaccess.entity.UserPermission;
import com.microservices.dataaccess.repository.UserPermissionRepository;
import com.microservices.service.QueryUserService;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class TwitterQueryUserService implements QueryUserService {
    private final UserPermissionRepository userPermissionRepository;

    private static final Logger LOGGER = LoggerFactory.getLogger(TwitterQueryUserService.class);

    @Override
    public Optional<List<UserPermission>> findAllPermissionsByUsername(String username) {
        LOGGER.info("Finding permissions by username: {}", username);

        return userPermissionRepository.findPermissionsByUsername(username);
    }
}