package com.example.house.modules.system;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.house.common.api.PageResponse;
import com.example.house.common.exception.BusinessException;
import com.example.house.modules.auth.entity.SysMenuEntity;
import com.example.house.modules.auth.entity.SysRoleEntity;
import com.example.house.modules.auth.entity.SysUserEntity;
import com.example.house.modules.auth.entity.SysUserRoleEntity;
import com.example.house.modules.auth.mapper.SysMenuMapper;
import com.example.house.modules.auth.mapper.SysRoleMapper;
import com.example.house.modules.auth.mapper.SysUserMapper;
import com.example.house.modules.auth.mapper.SysUserRoleMapper;
import jakarta.validation.constraints.NotBlank;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class SystemService {

    private final SysUserMapper userMapper;
    private final SysRoleMapper roleMapper;
    private final SysUserRoleMapper userRoleMapper;
    private final SysMenuMapper menuMapper;
    private final PasswordEncoder passwordEncoder;

    public SystemService(
        SysUserMapper userMapper,
        SysRoleMapper roleMapper,
        SysUserRoleMapper userRoleMapper,
        SysMenuMapper menuMapper,
        PasswordEncoder passwordEncoder
    ) {
        this.userMapper = userMapper;
        this.roleMapper = roleMapper;
        this.userRoleMapper = userRoleMapper;
        this.menuMapper = menuMapper;
        this.passwordEncoder = passwordEncoder;
    }

    public PageResponse<EmployeeRecord> listEmployees(int pageNum, int pageSize, String keyword) {
        Page<SysUserEntity> page = userMapper.selectPage(
            new Page<>(pageNum, pageSize),
            new LambdaQueryWrapper<SysUserEntity>()
                .and(keyword != null && !keyword.isBlank(), wrapper -> wrapper
                    .like(SysUserEntity::getEmployeeCode, keyword)
                    .or()
                    .like(SysUserEntity::getDisplayName, keyword))
                .orderByDesc(SysUserEntity::getId)
        );
        Map<Long, SysRoleEntity> roleMap = roleMapper.selectList(new LambdaQueryWrapper<SysRoleEntity>())
            .stream()
            .collect(Collectors.toMap(SysRoleEntity::getId, Function.identity()));
        Map<Long, Long> userRoleMap = userRoleMapper.selectList(new LambdaQueryWrapper<SysUserRoleEntity>())
            .stream()
            .collect(Collectors.toMap(SysUserRoleEntity::getUserId, SysUserRoleEntity::getRoleId, (left, right) -> right));
        List<EmployeeRecord> list = page.getRecords().stream()
            .map(item -> {
                Long roleId = userRoleMap.get(item.getId());
                SysRoleEntity role = roleMap.get(roleId);
                return new EmployeeRecord(
                    item.getId(),
                    item.getEmployeeCode(),
                    item.getDisplayName(),
                    item.getMobile(),
                    item.getPosition(),
                    item.getStatus(),
                    role == null ? "" : role.getRoleCode()
                );
            })
            .toList();
        return new PageResponse<>(list, page.getTotal(), pageNum, pageSize);
    }

    public EmployeeRecord createEmployee(EmployeeUpsertRequest request) {
        SysUserEntity entity = new SysUserEntity();
        entity.setUsername(request.code().toLowerCase());
        entity.setPasswordHash(passwordEncoder.encode("Admin@123"));
        fillUser(entity, request);
        entity.setOrgId(1L);
        entity.setCreatedBy(1L);
        entity.setUpdatedBy(1L);
        entity.setDeleted(0);
        userMapper.insert(entity);
        upsertUserRole(entity.getId(), request.roleCode());
        return new EmployeeRecord(entity.getId(), entity.getEmployeeCode(), entity.getDisplayName(), entity.getMobile(), entity.getPosition(), entity.getStatus(), request.roleCode());
    }

    public EmployeeRecord updateEmployee(Long id, EmployeeUpsertRequest request) {
        SysUserEntity entity = requireUser(id);
        fillUser(entity, request);
        entity.setUpdatedBy(1L);
        userMapper.updateById(entity);
        upsertUserRole(id, request.roleCode());
        return new EmployeeRecord(entity.getId(), entity.getEmployeeCode(), entity.getDisplayName(), entity.getMobile(), entity.getPosition(), entity.getStatus(), request.roleCode());
    }

    public EmployeeRecord updateEmployeeStatus(Long id, String status) {
        SysUserEntity entity = requireUser(id);
        entity.setStatus(status);
        userMapper.updateById(entity);
        String roleCode = currentRoleCode(id);
        return new EmployeeRecord(entity.getId(), entity.getEmployeeCode(), entity.getDisplayName(), entity.getMobile(), entity.getPosition(), entity.getStatus(), roleCode);
    }

    public List<RoleRecord> listRoles() {
        return roleMapper.selectList(new LambdaQueryWrapper<SysRoleEntity>().orderByAsc(SysRoleEntity::getId))
            .stream()
            .map(item -> new RoleRecord(item.getId(), item.getRoleCode(), item.getRoleName(), item.getRemark()))
            .toList();
    }

    public RoleRecord createRole(RoleUpsertRequest request) {
        SysRoleEntity entity = new SysRoleEntity();
        entity.setRoleCode(request.code());
        entity.setRoleName(request.name());
        entity.setRemark(request.remark());
        roleMapper.insert(entity);
        return new RoleRecord(entity.getId(), entity.getRoleCode(), entity.getRoleName(), entity.getRemark());
    }

    public RoleRecord updateRole(Long id, RoleUpsertRequest request) {
        SysRoleEntity entity = requireRole(id);
        entity.setRoleCode(request.code());
        entity.setRoleName(request.name());
        entity.setRemark(request.remark());
        roleMapper.updateById(entity);
        return new RoleRecord(entity.getId(), entity.getRoleCode(), entity.getRoleName(), entity.getRemark());
    }

    public List<MenuRecord> listMenus() {
        return menuMapper.selectList(new LambdaQueryWrapper<SysMenuEntity>().orderByAsc(SysMenuEntity::getSortNo))
            .stream()
            .map(item -> new MenuRecord(item.getId(), item.getParentId(), item.getMenuCode(), item.getMenuName(), item.getPath(), item.getMenuType(), item.getPermissionCode()))
            .toList();
    }

    public MenuRecord createMenu(MenuUpsertRequest request) {
        SysMenuEntity entity = new SysMenuEntity();
        fillMenu(entity, request);
        entity.setSortNo(99);
        menuMapper.insert(entity);
        return new MenuRecord(entity.getId(), entity.getParentId(), entity.getMenuCode(), entity.getMenuName(), entity.getPath(), entity.getMenuType(), entity.getPermissionCode());
    }

    public MenuRecord updateMenu(Long id, MenuUpsertRequest request) {
        SysMenuEntity entity = requireMenu(id);
        fillMenu(entity, request);
        menuMapper.updateById(entity);
        return new MenuRecord(entity.getId(), entity.getParentId(), entity.getMenuCode(), entity.getMenuName(), entity.getPath(), entity.getMenuType(), entity.getPermissionCode());
    }

    private void fillUser(SysUserEntity entity, EmployeeUpsertRequest request) {
        entity.setEmployeeCode(request.code());
        entity.setDisplayName(request.name());
        entity.setMobile(request.mobile());
        entity.setPosition(request.position());
        entity.setStatus(request.status());
    }

    private void fillMenu(SysMenuEntity entity, MenuUpsertRequest request) {
        entity.setParentId(request.parentId() == null ? 0L : request.parentId());
        entity.setMenuCode(request.name());
        entity.setMenuName(request.title());
        entity.setPath(request.path());
        entity.setMenuType(request.type());
        entity.setPermissionCode(request.permissionCode());
    }

    private void upsertUserRole(Long userId, String roleCode) {
        SysRoleEntity role = roleMapper.selectOne(new LambdaQueryWrapper<SysRoleEntity>().eq(SysRoleEntity::getRoleCode, roleCode).last("limit 1"));
        if (role == null) {
            throw new BusinessException("Role not found");
        }
        userRoleMapper.delete(new LambdaQueryWrapper<SysUserRoleEntity>().eq(SysUserRoleEntity::getUserId, userId));
        SysUserRoleEntity relation = new SysUserRoleEntity();
        relation.setUserId(userId);
        relation.setRoleId(role.getId());
        userRoleMapper.insert(relation);
    }

    private String currentRoleCode(Long userId) {
        SysUserRoleEntity relation = userRoleMapper.selectOne(new LambdaQueryWrapper<SysUserRoleEntity>().eq(SysUserRoleEntity::getUserId, userId).last("limit 1"));
        if (relation == null) {
            return "";
        }
        SysRoleEntity role = roleMapper.selectById(relation.getRoleId());
        return role == null ? "" : role.getRoleCode();
    }

    private SysUserEntity requireUser(Long id) {
        SysUserEntity entity = userMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException("Employee not found");
        }
        return entity;
    }

    private SysRoleEntity requireRole(Long id) {
        SysRoleEntity entity = roleMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException("Role not found");
        }
        return entity;
    }

    private SysMenuEntity requireMenu(Long id) {
        SysMenuEntity entity = menuMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException("Menu not found");
        }
        return entity;
    }

    public record EmployeeRecord(Long id, String code, String name, String mobile, String position, String status, String roleCode) {
    }

    public record RoleRecord(Long id, String code, String name, String remark) {
    }

    public record MenuRecord(Long id, Long parentId, String name, String title, String path, String type, String permissionCode) {
    }

    public record EmployeeUpsertRequest(
        @NotBlank String code,
        @NotBlank String name,
        @NotBlank String mobile,
        @NotBlank String position,
        @NotBlank String status,
        @NotBlank String roleCode
    ) {
    }

    public record RoleUpsertRequest(@NotBlank String code, @NotBlank String name, String remark) {
    }

    public record MenuUpsertRequest(Long parentId, @NotBlank String name, @NotBlank String title, String path, @NotBlank String type, String permissionCode) {
    }
}
