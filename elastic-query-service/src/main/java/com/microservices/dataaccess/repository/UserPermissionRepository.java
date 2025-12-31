package com.microservices.dataaccess.repository;

import com.microservices.dataaccess.entity.UserPermission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserPermissionRepository extends JpaRepository<UserPermission, UUID> {
    @Query(value = "SELECT p.user_permission_id AS id, u.username, d.document_id, p.permission_type " +
            "FROM users u, user_permissions p, documents d " +
            "WHERE u.id = p.user_id AND d.id = p.document_id AND u.username = :username",
            nativeQuery = true)
    Optional<List<UserPermission>> findPermissionsByUsername(@Param("username") String username);
}