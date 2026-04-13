# 房源管理系统 Stage 1 交付说明

## 本阶段完成内容

1. 创建前端项目 `frontend/house-admin`
2. 创建后端项目 `backend/house-service`
3. 完成登录、退出、个人信息、菜单接口
4. 完成首页看板接口与 ECharts 图表页面
5. 完成统一房源主模型下的整租、合租、集中管理列表与新增编辑
6. 接入租客、房东、维修、保洁、员工、角色、菜单的基础列表入口

## 当前实现特点

- 后端采用 Spring Boot 3 + Spring Security 的单体骨架
- 为了先跑通前后端联调，当前登录态使用内存会话存储
- 接口统一使用 `/api` 前缀与 `code/message/data` 响应结构
- 前端采用 Vue 3 + Vite + Pinia + Vue Router + Element Plus + ECharts
- 房源页按 `rentalMode` 复用统一页面，避免重复建三套大表和页面

## 下一阶段建议

1. 将登录态和权限快照从内存迁移到 Redis
2. 将房源、租客、房东、工单数据迁移到 MySQL + MyBatis-Plus
3. 完善角色授权、菜单树维护与按钮级权限控制
4. 为租客、房东、工单补新增、编辑、状态流转
5. 增加 Docker Compose、Nginx 与部署说明

## 启动方式

### 后端

```bash
cd backend/house-service
mvn spring-boot:run
```

### 前端

```bash
cd frontend/house-admin
npm install
npm run dev
```

默认登录账号：

- 用户名：`admin`
- 密码：`Admin@123`
