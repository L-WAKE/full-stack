package com.example.house.auth;

import java.time.Duration;

public interface AuthSessionStore {

    void save(String token, AuthUser authUser, Duration ttl);

    AuthUser get(String token);

    void delete(String token);
}
