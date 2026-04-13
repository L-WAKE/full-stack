# 接口与前端实现指南

## 接口基线

- 接口统一前缀：`/api`
- 统一返回结构：

```json
{
  "code": 0,
  "message": "success",
  "data": {}
}
```

- 分页返回结构：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "list": [],
    "total": 0,
    "pageNum": 1,
    "pageSize": 10
  }
}
```

## 鉴权流程

1. 用户名密码登录
2. 后端生成 JWT
3. Redis 缓存登录信息与权限快照
4. 前端保存 token
5. 前端请求用户信息与菜单树
6. 前端按菜单动态注册路由
7. 页面按钮根据 `permission code` 控制显示

权限码命名示例：

- `house:add`
- `house:edit`
- `tenant:view`
- `tenant:edit`
- `maintenance:assign`

## 关键接口

### 认证模块

- `POST /api/auth/login`
- `POST /api/auth/logout`
- `GET /api/auth/profile`
- `GET /api/auth/menus`

### 首页看板

- `GET /api/dashboard/overview`
- `GET /api/dashboard/trend`

### 房源管理

- `GET /api/houses`
- `POST /api/houses`
- `PUT /api/houses/{id}`
- `GET /api/houses/{id}`
- `DELETE /api/houses/{id}`
- `PUT /api/houses/{id}/status`

### 房间管理

- `GET /api/houses/{houseId}/rooms`
- `POST /api/houses/{houseId}/rooms`
- `PUT /api/rooms/{id}`
- `DELETE /api/rooms/{id}`

### 租客与房东

- `GET /api/tenants`
- `POST /api/tenants`
- `PUT /api/tenants/{id}`
- `GET /api/landlords`
- `POST /api/landlords`
- `PUT /api/landlords/{id}`

### 员工与权限

- `GET /api/employees`
- `POST /api/employees`
- `PUT /api/employees/{id}`
- `PUT /api/employees/{id}/status`
- `POST /api/roles`
- `PUT /api/roles/{id}`
- `GET /api/menus/tree`

### 工单管理

- `GET /api/maintenance-orders`
- `POST /api/maintenance-orders`
- `PUT /api/maintenance-orders/{id}`
- `GET /api/cleaning-orders`
- `POST /api/cleaning-orders`
- `PUT /api/cleaning-orders/{id}`

## 前端目录结构

推荐目录：

```text
frontend/house-admin/
  src/
    api/
    assets/
    components/
    directives/
    hooks/
    layout/
    router/
    stores/
    styles/
    utils/
    views/
      login/
      dashboard/
      housing/
        whole-rent/
        shared-rent/
        centralized/
      customer/
        tenant/
        landlord/
      workorder/
        maintenance/
        cleaning/
      system/
        employee/
        role/
        menu/
```

## 前端页面形态

后台页默认采用以下形态：

1. 筛选区
2. 操作区
3. 表格区
4. 分页区
5. 弹窗或抽屉表单

优先抽取的通用组件：

1. 通用分页表格
2. 通用查询表单
3. 字典标签
4. 图片或附件预览
5. 权限按钮

## 前端 Store 设计

至少拆分以下 Pinia store：

1. `userStore`：用户信息、token、角色、权限
2. `permissionStore`：菜单树、动态路由
3. `dictStore`：字典缓存
4. `appStore`：侧边栏、主题、标签页

## 路由设计

一级菜单建议：

1. 首页
2. 房源管理
3. 基础资料
4. 工单管理
5. 系统管理

二级菜单建议：

1. 整租管理
2. 合租管理
3. 集中管理
4. 租客管理
5. 房东管理
6. 维修管理
7. 保洁管理
8. 员工管理
9. 角色管理
10. 菜单管理

## 后端包结构设计

推荐结构：

```text
backend/house-service/
  src/main/java/com/example/house/
    common/
    config/
    auth/
    modules/
      dashboard/
      housing/
      tenant/
      landlord/
      employee/
      role/
      permission/
      maintenance/
      cleaning/
      file/
      log/
```

默认采用以下分层：

1. Controller
2. Service
3. Mapper
4. Entity
5. DTO / VO

## 后端实现约束

- 登录鉴权优先使用 Spring Security + JWT + Redis
- 首页统计优先由后端提供聚合接口，不让前端拼很多散接口
- 分页、枚举、异常、校验、统一响应应尽早抽到公共层
- 数据权限在 MVP 阶段只预留扩展位，不要一开始做复杂权限域
