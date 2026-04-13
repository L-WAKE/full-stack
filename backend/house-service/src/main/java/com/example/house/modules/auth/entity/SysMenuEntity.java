package com.example.house.modules.auth.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

@TableName("sys_menu")
public class SysMenuEntity {

    @TableId
    private Long id;
    private Long parentId;
    private String menuCode;
    private String menuName;
    private String menuType;
    private String path;
    private String permissionCode;
    private Integer sortNo;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getParentId() { return parentId; }
    public void setParentId(Long parentId) { this.parentId = parentId; }
    public String getMenuCode() { return menuCode; }
    public void setMenuCode(String menuCode) { this.menuCode = menuCode; }
    public String getMenuName() { return menuName; }
    public void setMenuName(String menuName) { this.menuName = menuName; }
    public String getMenuType() { return menuType; }
    public void setMenuType(String menuType) { this.menuType = menuType; }
    public String getPath() { return path; }
    public void setPath(String path) { this.path = path; }
    public String getPermissionCode() { return permissionCode; }
    public void setPermissionCode(String permissionCode) { this.permissionCode = permissionCode; }
    public Integer getSortNo() { return sortNo; }
    public void setSortNo(Integer sortNo) { this.sortNo = sortNo; }
}
