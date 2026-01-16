package com.back.user.domain.enums;

public enum Role {
    ADMIN,
    USER;

    public String authority() {
        return "ROLE_" + name();
    }
}
