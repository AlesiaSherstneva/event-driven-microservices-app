package com.microservices.dataaccess.entity;

public interface BaseEntity<PK> {
    PK getId();
}