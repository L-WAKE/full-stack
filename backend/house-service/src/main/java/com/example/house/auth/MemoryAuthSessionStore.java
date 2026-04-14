package com.example.house.auth;

import com.example.house.common.exception.BusinessException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@ConditionalOnProperty(name = "house.auth.session-store", havingValue = "memory", matchIfMissing = true)
public class MemoryAuthSessionStore implements AuthSessionStore {

    private final Map<String, SessionRecord> sessions = new ConcurrentHashMap<>();

    @Override
    public void save(String token, AuthUser authUser, Duration ttl) {
        sessions.put(token, new SessionRecord(authUser, Instant.now().plus(ttl)));
    }

    @Override
    public AuthUser get(String token) {
        SessionRecord session = sessions.get(token);
        if (session == null || session.expiresAt().isBefore(Instant.now())) {
            sessions.remove(token);
            throw new BusinessException(4010, "Login session expired");
        }
        return session.authUser();
    }

    @Override
    public void delete(String token) {
        sessions.remove(token);
    }

    private record SessionRecord(AuthUser authUser, Instant expiresAt) {
    }
}
