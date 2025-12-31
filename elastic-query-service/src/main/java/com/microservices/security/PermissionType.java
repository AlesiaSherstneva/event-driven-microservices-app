package com.microservices.security;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PermissionType {
    READ("READ"), WRITE("WRITE"), ADMIN("ADMIN");

    private final String type;
}