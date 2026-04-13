package com.example.house.modules.auth.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.example.house.common.entity.BaseEntity;

@TableName("sys_user")
public class SysUserEntity extends BaseEntity {

    @TableId
    private Long id;
    private String username;
    private String passwordHash;
    private String displayName;
    private String mobile;
    private String employeeCode;
    private String position;
    private String status;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }
    public String getMobile() { return mobile; }
    public void setMobile(String mobile) { this.mobile = mobile; }
    public String getEmployeeCode() { return employeeCode; }
    public void setEmployeeCode(String employeeCode) { this.employeeCode = employeeCode; }
    public String getPosition() { return position; }
    public void setPosition(String position) { this.position = position; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
