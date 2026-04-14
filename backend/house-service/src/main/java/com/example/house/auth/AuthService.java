package com.example.house.auth;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.house.common.exception.BusinessException;
import com.example.house.modules.auth.entity.SysMenuEntity;
import com.example.house.modules.auth.entity.SysRoleEntity;
import com.example.house.modules.auth.entity.SysRoleMenuEntity;
import com.example.house.modules.auth.entity.SysUserEntity;
import com.example.house.modules.auth.entity.SysUserRoleEntity;
import com.example.house.modules.auth.mapper.SysMenuMapper;
import com.example.house.modules.auth.mapper.SysRoleMapper;
import com.example.house.modules.auth.mapper.SysRoleMenuMapper;
import com.example.house.modules.auth.mapper.SysUserMapper;
import com.example.house.modules.auth.mapper.SysUserRoleMapper;
import com.example.house.modules.system.MenuItem;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AuthService {

    private final SysUserMapper userMapper;
    private final SysRoleMapper roleMapper;
    private final SysUserRoleMapper userRoleMapper;
    private final SysMenuMapper menuMapper;
    private final SysRoleMenuMapper roleMenuMapper;
    private final PasswordEncoder passwordEncoder;
    private final AuthSessionStore authSessionStore;
    private final long tokenTtlMinutes;

    public AuthService(
        SysUserMapper userMapper,
        SysRoleMapper roleMapper,
        SysUserRoleMapper userRoleMapper,
        SysMenuMapper menuMapper,
        SysRoleMenuMapper roleMenuMapper,
        PasswordEncoder passwordEncoder,
        AuthSessionStore authSessionStore,
        @Value("${house.auth.token-ttl-minutes:120}") long tokenTtlMinutes
    ) {
        this.userMapper = userMapper;
        this.roleMapper = roleMapper;
        this.userRoleMapper = userRoleMapper;
        this.menuMapper = menuMapper;
        this.roleMenuMapper = roleMenuMapper;
        this.passwordEncoder = passwordEncoder;
        this.authSessionStore = authSessionStore;
        this.tokenTtlMinutes = tokenTtlMinutes;
    }

    public LoginResult login(LoginCommand command) {
        SysUserEntity user = userMapper.selectOne(new LambdaQueryWrapper<SysUserEntity>()
            .eq(SysUserEntity::getUsername, command.username())
            .eq(SysUserEntity::getStatus, "ENABLED"));
        if (user == null || !passwordEncoder.matches(command.password(), user.getPasswordHash())) {
            throw new BusinessException(4002, "Invalid username or password");
        }

        AuthUser authUser = buildAuthUser(user);
        String token = UUID.randomUUID().toString().replace("-", "");
        authSessionStore.save(token, authUser, Duration.ofMinutes(tokenTtlMinutes));
        return new LoginResult(token, authUser);
    }

    public AuthUser getUserByToken(String token) {
        return authSessionStore.get(token);
    }

    public void logout(String token) {
        authSessionStore.delete(token);
    }

    public List<MenuItem> getMenus(AuthUser user) {
        List<SysMenuEntity> menus = listRoleMenus(user.roleId()).stream()
            .filter(item -> "MENU".equals(item.getMenuType()))
            .toList();
        Map<Long, List<SysMenuEntity>> childrenByParentId = menus.stream()
            .collect(Collectors.groupingBy(SysMenuEntity::getParentId));
        return childrenByParentId.getOrDefault(0L, List.of()).stream()
            .sorted(Comparator.comparing(SysMenuEntity::getSortNo))
            .map(root -> toMenuItem(root, childrenByParentId))
            .toList();
    }

    private AuthUser buildAuthUser(SysUserEntity user) {
        SysUserRoleEntity userRole = userRoleMapper.selectOne(new LambdaQueryWrapper<SysUserRoleEntity>()
            .eq(SysUserRoleEntity::getUserId, user.getId())
            .last("limit 1"));
        if (userRole == null) {
            throw new BusinessException(4003, "User has no assigned role");
        }
        SysRoleEntity role = roleMapper.selectById(userRole.getRoleId());
        List<String> permissions = listRoleMenus(role.getId()).stream()
            .map(SysMenuEntity::getPermissionCode)
            .filter(Objects::nonNull)
            .distinct()
            .toList();
        return new AuthUser(user.getId(), user.getUsername(), user.getDisplayName(), role.getId(), role.getRoleCode(), permissions);
    }

    private List<SysMenuEntity> listRoleMenus(Long roleId) {
        List<Long> menuIds = roleMenuMapper.selectList(new LambdaQueryWrapper<SysRoleMenuEntity>()
                .eq(SysRoleMenuEntity::getRoleId, roleId))
            .stream()
            .map(SysRoleMenuEntity::getMenuId)
            .toList();
        if (menuIds.isEmpty()) {
            return List.of();
        }
        return menuMapper.selectList(new LambdaQueryWrapper<SysMenuEntity>()
            .in(SysMenuEntity::getId, menuIds)
            .orderByAsc(SysMenuEntity::getSortNo));
    }

    private MenuItem toMenuItem(SysMenuEntity menu, Map<Long, List<SysMenuEntity>> childrenByParentId) {
        List<MenuItem> children = childrenByParentId.getOrDefault(menu.getId(), List.of()).stream()
            .sorted(Comparator.comparing(SysMenuEntity::getSortNo))
            .map(child -> toMenuItem(child, childrenByParentId))
            .toList();
        return new MenuItem(menu.getMenuCode(), menu.getMenuName(), menu.getPath(), null, menu.getPermissionCode(), children);
    }

    public record LoginCommand(String username, String password) {
    }

    public record LoginResult(String token, AuthUser user) {
    }
}
