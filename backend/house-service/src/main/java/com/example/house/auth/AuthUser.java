package com.example.house.auth;

import java.util.List;

public record AuthUser(Long id, String username, String displayName, Long roleId, String roleCode, List<String> permissions) {
}
