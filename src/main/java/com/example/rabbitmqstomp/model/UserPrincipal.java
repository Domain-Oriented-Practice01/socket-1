package com.example.rabbitmqstomp.model;

import java.security.Principal;

public class UserPrincipal implements Principal {
    // 用于生成一个用户，PrincipalHandshakeHandler生成
    private final String name;
    
    public UserPrincipal(String name) {
        this.name = name;
    }
    @Override
    public String getName() {
        return name;
    }
}
