package com.example.house.common.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
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
import com.example.house.modules.customer.entity.LandlordEntity;
import com.example.house.modules.customer.entity.TenantEntity;
import com.example.house.modules.customer.mapper.LandlordMapper;
import com.example.house.modules.customer.mapper.TenantMapper;
import com.example.house.modules.housing.entity.HouseEntity;
import com.example.house.modules.housing.mapper.HouseMapper;
import com.example.house.modules.workorder.entity.CleaningOrderEntity;
import com.example.house.modules.workorder.entity.MaintenanceOrderEntity;
import com.example.house.modules.workorder.mapper.CleaningOrderMapper;
import com.example.house.modules.workorder.mapper.MaintenanceOrderMapper;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
public class DataInitializer implements ApplicationRunner {

    private final SysRoleMapper roleMapper;
    private final SysUserMapper userMapper;
    private final SysUserRoleMapper userRoleMapper;
    private final SysMenuMapper menuMapper;
    private final SysRoleMenuMapper roleMenuMapper;
    private final LandlordMapper landlordMapper;
    private final TenantMapper tenantMapper;
    private final HouseMapper houseMapper;
    private final MaintenanceOrderMapper maintenanceOrderMapper;
    private final CleaningOrderMapper cleaningOrderMapper;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(
        SysRoleMapper roleMapper,
        SysUserMapper userMapper,
        SysUserRoleMapper userRoleMapper,
        SysMenuMapper menuMapper,
        SysRoleMenuMapper roleMenuMapper,
        LandlordMapper landlordMapper,
        TenantMapper tenantMapper,
        HouseMapper houseMapper,
        MaintenanceOrderMapper maintenanceOrderMapper,
        CleaningOrderMapper cleaningOrderMapper,
        PasswordEncoder passwordEncoder
    ) {
        this.roleMapper = roleMapper;
        this.userMapper = userMapper;
        this.userRoleMapper = userRoleMapper;
        this.menuMapper = menuMapper;
        this.roleMenuMapper = roleMenuMapper;
        this.landlordMapper = landlordMapper;
        this.tenantMapper = tenantMapper;
        this.houseMapper = houseMapper;
        this.maintenanceOrderMapper = maintenanceOrderMapper;
        this.cleaningOrderMapper = cleaningOrderMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(ApplicationArguments args) {
        seedRoles();
        seedUsers();
        seedMenus();
        seedBusinessData();
    }

    private void seedRoles() {
        if (roleMapper.selectCount(null) > 0) {
            return;
        }
        insertRole(1L, "ADMIN", "System Admin", "Full access");
        insertRole(2L, "HOUSE_MANAGER", "House Manager", "Manage houses");
        insertRole(3L, "OPERATOR", "Operator", "Manage tenants and workorders");
    }

    private void seedUsers() {
        if (userMapper.selectCount(null) > 0) {
            return;
        }
        insertUser(1L, "admin", "System Admin", "EMP001", "Operations Manager", "13800000000", "ENABLED", 1L);
        insertUser(2L, "emp002", "Liu Butler", "EMP002", "House Butler", "13888880002", "ENABLED", 2L);
        insertUser(3L, "emp003", "Huang Support", "EMP003", "Customer Support", "13888880003", "DISABLED", 3L);
    }

    private void seedMenus() {
        if (menuMapper.selectCount(null) > 0) {
            return;
        }
        insertMenu(1L, 0L, "dashboard", "Dashboard", "MENU", "/dashboard", "dashboard:view", 1);
        insertMenu(2L, 0L, "housing", "Housing", "MENU", "/housing", null, 2);
        insertMenu(3L, 2L, "wholeRent", "Whole Rent", "MENU", "/housing/whole-rent", "house:view", 3);
        insertMenu(4L, 2L, "sharedRent", "Shared Rent", "MENU", "/housing/shared-rent", "house:view", 4);
        insertMenu(5L, 2L, "centralized", "Centralized", "MENU", "/housing/centralized", "house:view", 5);
        insertMenu(6L, 0L, "customer", "Customer", "MENU", "/customer", null, 6);
        insertMenu(7L, 6L, "tenant", "Tenant", "MENU", "/customer/tenant", "tenant:view", 7);
        insertMenu(8L, 6L, "landlord", "Landlord", "MENU", "/customer/landlord", "landlord:view", 8);
        insertMenu(9L, 0L, "workorder", "Workorder", "MENU", "/workorder", null, 9);
        insertMenu(10L, 9L, "maintenance", "Maintenance", "MENU", "/workorder/maintenance", "maintenance:view", 10);
        insertMenu(11L, 9L, "cleaning", "Cleaning", "MENU", "/workorder/cleaning", "cleaning:view", 11);
        insertMenu(12L, 0L, "system", "System", "MENU", "/system", null, 12);
        insertMenu(13L, 12L, "employee", "Employee", "MENU", "/system/employee", "employee:view", 13);
        insertMenu(14L, 12L, "role", "Role", "MENU", "/system/role", "role:view", 14);
        insertMenu(15L, 12L, "menu", "Menu", "MENU", "/system/menu", "menu:view", 15);
        insertMenu(16L, 3L, "houseAdd", "Add House", "BUTTON", "", "house:add", 16);
        insertMenu(17L, 3L, "houseEdit", "Edit House", "BUTTON", "", "house:edit", 17);

        List<Long> adminMenuIds = List.of(1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L, 10L, 11L, 12L, 13L, 14L, 15L, 16L, 17L);
        List<Long> managerMenuIds = List.of(1L, 2L, 3L, 4L, 5L, 9L, 10L, 11L);
        List<Long> operatorMenuIds = List.of(1L, 6L, 7L, 8L, 9L, 10L, 11L);
        insertRoleMenus(1L, adminMenuIds);
        insertRoleMenus(2L, managerMenuIds);
        insertRoleMenus(3L, operatorMenuIds);
    }

    private void seedBusinessData() {
        if (landlordMapper.selectCount(null) == 0) {
            insertLandlord(1L, "Wang Li", "13800138000", "ID001", "ICBC **** 1024", "Guangzhou Tianhe", "VIP landlord");
            insertLandlord(2L, "Li Senior", "13900139000", "ID002", "CCB **** 8899", "Guangzhou Haizhu", "");
            insertLandlord(3L, "Rongyu Asset", "020-88998899", "ORG003", "ABC **** 6677", "Panyu District", "Company landlord");
        }
        if (houseMapper.selectCount(null) == 0) {
            insertHouse(1L, "HZ-001", "Xinghe Bay 2-801", "WHOLE", "Xinghe Bay", "Guangzhou Tianhe 66", new BigDecimal("88.00"), new BigDecimal("5800.00"), "OCCUPIED", 1L, "Wang Li", "Chen Hao");
            insertHouse(2L, "HZ-002", "Youth Apartment A-302", "SHARED", "Youth Apartment", "Guangzhou Haizhu 89", new BigDecimal("26.00"), new BigDecimal("2200.00"), "VACANT", 2L, "Li Senior", null);
            insertHouse(3L, "HZ-003", "City Mansion 5-1602", "CENTRALIZED", "City Mansion", "Panyu 108", new BigDecimal("112.00"), new BigDecimal("7600.00"), "RENOVATING", 3L, "Rongyu Asset", null);
        }
        if (tenantMapper.selectCount(null) == 0) {
            insertTenant(1L, "Chen Hao", "13800111111", "Male", "Xinghe Bay 2-801", "2026-02-01", "2027-02-01", "Alice", "13900000001");
            insertTenant(2L, "Lin Yu", "13800222222", "Female", "Youth Apartment A-302", "2026-03-14", "2027-03-14", "Bob", "13900000002");
            insertTenant(3L, "Zhao Qing", "13800333333", "Female", "City Mansion 5-1602", "2026-04-02", "2027-04-02", "Carol", "13900000003");
        }
        if (maintenanceOrderMapper.selectCount(null) == 0) {
            insertMaintenance(1L, "WX-202604010001", "Xinghe Bay 2-801", "Water heater", "Chen Hao", "Liu Butler", "HIGH", "PENDING");
            insertMaintenance(2L, "WX-202604080001", "Youth Apartment A-302", "Door lock", "Lin Yu", "Chen Manager", "MEDIUM", "PROCESSING");
        }
        if (cleaningOrderMapper.selectCount(null) == 0) {
            insertCleaning(1L, "BJ-202604030001", "City Mansion 5-1602", "Deep clean", "2026-04-15 10:00", "Alice", "ASSIGNED");
            insertCleaning(2L, "BJ-202604060001", "Youth Apartment A-302", "Checkout clean", "2026-04-16 14:00", "Bob", "PENDING_ASSIGN");
        }
    }

    private void insertRole(Long id, String code, String name, String remark) {
        SysRoleEntity role = new SysRoleEntity();
        role.setId(id);
        role.setRoleCode(code);
        role.setRoleName(name);
        role.setRemark(remark);
        roleMapper.insert(role);
    }

    private void insertUser(Long id, String username, String displayName, String employeeCode, String position, String mobile, String status, Long roleId) {
        SysUserEntity user = new SysUserEntity();
        user.setId(id);
        user.setUsername(username);
        user.setPasswordHash(passwordEncoder.encode("Admin@123"));
        user.setDisplayName(displayName);
        user.setEmployeeCode(employeeCode);
        user.setPosition(position);
        user.setMobile(mobile);
        user.setStatus(status);
        user.setOrgId(1L);
        user.setCreatedBy(1L);
        user.setUpdatedBy(1L);
        user.setDeleted(0);
        userMapper.insert(user);

        SysUserRoleEntity relation = new SysUserRoleEntity();
        relation.setUserId(id);
        relation.setRoleId(roleId);
        userRoleMapper.insert(relation);
    }

    private void insertMenu(Long id, Long parentId, String code, String name, String type, String path, String permissionCode, int sortNo) {
        SysMenuEntity menu = new SysMenuEntity();
        menu.setId(id);
        menu.setParentId(parentId);
        menu.setMenuCode(code);
        menu.setMenuName(name);
        menu.setMenuType(type);
        menu.setPath(path);
        menu.setPermissionCode(permissionCode);
        menu.setSortNo(sortNo);
        menuMapper.insert(menu);
    }

    private void insertRoleMenus(Long roleId, List<Long> menuIds) {
        for (Long menuId : menuIds) {
            SysRoleMenuEntity relation = new SysRoleMenuEntity();
            relation.setRoleId(roleId);
            relation.setMenuId(menuId);
            roleMenuMapper.insert(relation);
        }
    }

    private void insertLandlord(Long id, String name, String mobile, String idNo, String bankAccount, String address, String remark) {
        LandlordEntity entity = new LandlordEntity();
        entity.setId(id);
        entity.setName(name);
        entity.setMobile(mobile);
        entity.setIdNo(idNo);
        entity.setBankAccount(bankAccount);
        entity.setAddress(address);
        entity.setRemark(remark);
        entity.setOrgId(1L);
        entity.setCreatedBy(1L);
        entity.setUpdatedBy(1L);
        entity.setDeleted(0);
        landlordMapper.insert(entity);
    }

    private void insertTenant(Long id, String name, String mobile, String gender, String houseName, String checkinDate, String checkoutDate, String emergencyContact, String emergencyMobile) {
        TenantEntity entity = new TenantEntity();
        entity.setId(id);
        entity.setName(name);
        entity.setMobile(mobile);
        entity.setGender(gender);
        entity.setHouseName(houseName);
        entity.setCheckinDate(checkinDate);
        entity.setCheckoutDate(checkoutDate);
        entity.setEmergencyContact(emergencyContact);
        entity.setEmergencyMobile(emergencyMobile);
        entity.setOrgId(1L);
        entity.setCreatedBy(1L);
        entity.setUpdatedBy(1L);
        entity.setDeleted(0);
        tenantMapper.insert(entity);
    }

    private void insertHouse(Long id, String code, String name, String rentalMode, String projectName, String address, BigDecimal area, BigDecimal rentPrice, String status, Long landlordId, String landlordName, String tenantName) {
        HouseEntity entity = new HouseEntity();
        entity.setId(id);
        entity.setHouseCode(code);
        entity.setHouseName(name);
        entity.setRentalMode(rentalMode);
        entity.setProjectName(projectName);
        entity.setAddress(address);
        entity.setArea(area);
        entity.setRentPrice(rentPrice);
        entity.setDepositPrice(rentPrice);
        entity.setStatus(status);
        entity.setLandlordId(landlordId);
        entity.setLandlordName(landlordName);
        entity.setTenantName(tenantName);
        entity.setOrgId(1L);
        entity.setCreatedBy(1L);
        entity.setUpdatedBy(1L);
        entity.setDeleted(0);
        houseMapper.insert(entity);
    }

    private void insertMaintenance(Long id, String orderNo, String houseName, String issueType, String reporter, String assignee, String priority, String status) {
        MaintenanceOrderEntity entity = new MaintenanceOrderEntity();
        entity.setId(id);
        entity.setOrderNo(orderNo);
        entity.setHouseName(houseName);
        entity.setIssueType(issueType);
        entity.setReporterName(reporter);
        entity.setAssigneeName(assignee);
        entity.setPriority(priority);
        entity.setStatus(status);
        entity.setOrgId(1L);
        entity.setCreatedBy(1L);
        entity.setUpdatedBy(1L);
        entity.setDeleted(0);
        maintenanceOrderMapper.insert(entity);
    }

    private void insertCleaning(Long id, String orderNo, String houseName, String type, String appointmentTime, String assignee, String status) {
        CleaningOrderEntity entity = new CleaningOrderEntity();
        entity.setId(id);
        entity.setOrderNo(orderNo);
        entity.setHouseName(houseName);
        entity.setCleaningType(type);
        entity.setAppointmentTime(appointmentTime);
        entity.setAssigneeName(assignee);
        entity.setStatus(status);
        entity.setOrgId(1L);
        entity.setCreatedBy(1L);
        entity.setUpdatedBy(1L);
        entity.setDeleted(0);
        cleaningOrderMapper.insert(entity);
    }
}
