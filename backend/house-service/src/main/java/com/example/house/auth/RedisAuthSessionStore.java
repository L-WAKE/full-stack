package com.example.house.auth;

import com.example.house.common.exception.BusinessException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@ConditionalOnProperty(name = "house.auth.session-store", havingValue = "redis")
public class RedisAuthSessionStore implements AuthSessionStore {

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    public RedisAuthSessionStore(StringRedisTemplate redisTemplate, ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public void save(String token, AuthUser authUser, Duration ttl) {
        try {
            redisTemplate.opsForValue().set(tokenKey(token), objectMapper.writeValueAsString(authUser), ttl);
        } catch (JsonProcessingException ex) {
            throw new BusinessException(5001, "Failed to create login session");
        }
    }

    @Override
    public AuthUser get(String token) {
        String payload = redisTemplate.opsForValue().get(tokenKey(token));
        if (payload == null) {
            throw new BusinessException(4010, "Login session expired");
        }
        try {
            return objectMapper.readValue(payload, AuthUser.class);
        } catch (JsonProcessingException ex) {
            throw new BusinessException(4010, "Login session expired");
        }
    }

    @Override
    public void delete(String token) {
        redisTemplate.delete(tokenKey(token));
    }

    private String tokenKey(String token) {
        return "house:auth:token:" + token;
    }
}
