package com.microservices.transformer;

import com.microservices.dataaccess.entity.UserPermission;
import com.microservices.security.PermissionType;
import com.microservices.security.TwitterQueryUser;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class UserPermissionsToDetailTransformer {
    public TwitterQueryUser getUserDetails(List<UserPermission> userPermissions) {
        return TwitterQueryUser.builder()
                .username(userPermissions.get(0).getUsername())
                .permissions(userPermissions.stream()
                        .collect(Collectors.toMap(
                                UserPermission::getDocumentId,
                                p -> PermissionType.valueOf(p.getPermissionType()))
                        )
                )
                .build();
    }
}