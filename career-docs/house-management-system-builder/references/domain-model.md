# 领域模型

## 核心业务规则

默认采用“统一房源主模型 + 房间子模型”的建模方式：

- `house` 保存房源主信息
- `house_room` 处理合租与集中式下的房间粒度
- `rental_mode` 用于区分 `WHOLE`、`SHARED`、`CENTRALIZED`

整租场景可以不创建 `house_room`；合租和集中式场景默认需要房间层。

## 核心实体

1. `org`
2. `sys_user`
3. `sys_role`
4. `sys_permission`
5. `sys_menu`
6. `landlord`
7. `tenant`
8. `house`
9. `house_room`
10. `maintenance_order`
11. `cleaning_order`

## 系统表

1. `sys_user`
2. `sys_role`
3. `sys_user_role`
4. `sys_menu`
5. `sys_role_menu`
6. `sys_permission`
7. `sys_role_permission`
8. `sys_login_log`
9. `sys_operation_log`

## 业务表

1. `landlord`
2. `tenant`
3. `house`
4. `house_room`
5. `tenant_house_relation`
6. `maintenance_order`
7. `cleaning_order`
8. `file_attachment`

## 公共字段

业务表默认统一包含以下字段，除非明确不需要：

- `id`
- `org_id`
- `created_by`
- `created_at`
- `updated_by`
- `updated_at`
- `deleted`

## 房源主表

关键字段：

- `id`
- `org_id`
- `house_code`
- `house_name`
- `rental_mode`
- `project_name`
- `community_name`
- `address`
- `building_no`
- `unit_no`
- `floor_no`
- `room_no`
- `layout_desc`
- `area`
- `rent_price`
- `deposit_price`
- `status`
- `landlord_id`
- `created_by`
- `created_at`
- `updated_at`
- `deleted`

默认状态应能覆盖空置、在租、下架等管理场景，并在前后端使用统一字典。

## 房间表

关键字段：

- `id`
- `house_id`
- `room_code`
- `room_name`
- `room_area`
- `rent_price`
- `status`
- `tenant_id`
- `checkin_date`
- `checkout_date`

## 租客

建议字段：

- 姓名
- 手机号
- 身份证号
- 性别
- 紧急联系人
- 联系方式
- 租住开始时间
- 租住结束时间
- 关联房源
- 备注

功能侧重点：

1. 新增与编辑
2. 查看关联房源
3. 查看历史租住记录

## 房东

建议字段：

- 姓名
- 手机号
- 身份证号或统一社会信用代码
- 银行卡信息
- 联系地址
- 备注

功能侧重点：

1. 新增与编辑
2. 查看名下房源
3. 统计房源数量

## 员工与 RBAC

员工建议字段：

- 员工编号
- 姓名
- 手机号
- 邮箱
- 部门
- 岗位
- 状态
- 角色

MVP 的 RBAC 最少覆盖：

1. 菜单可见权限
2. 页面按钮权限

## 维修工单

建议字段：

- 工单编号
- 房源
- 报修类型
- 问题描述
- 报修人
- 指派人
- 优先级
- 状态
- 创建时间
- 完成时间

推荐状态：

- `PENDING`
- `PROCESSING`
- `COMPLETED`
- `CLOSED`

## 保洁工单

建议字段：

- 工单编号
- 房源
- 保洁类型
- 预约时间
- 指派人
- 状态
- 备注

推荐状态：

- `PENDING_ASSIGN`
- `ASSIGNED`
- `COMPLETED`
- `CANCELLED`

## 业务模块映射

- 登录与权限：`auth`、`sys_user`、`sys_role`、`sys_menu`、`sys_permission`
- 首页看板：`dashboard`
- 房源管理：`housing`、`house`、`house_room`
- 租客管理：`tenant`
- 房东管理：`landlord`
- 员工与授权：`employee`、`role`、`permission`
- 维修工单：`maintenance`
- 保洁工单：`cleaning`

## 建模约束

- 不要为整租、合租、集中管理各建一套完全独立的大表
- 优先通过统一主模型 + 枚举 + 子表来减少重复
- 如果某个页面只是展示维度不同，优先复用底层实体和接口，再在页面层做差异化筛选
