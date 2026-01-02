package com.microservices.security;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum PermissionType {
    READ("READ"), WRITE("WRITE"), ADMIN("ADMIN");

    private final String type;
}