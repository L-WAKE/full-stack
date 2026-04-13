# 项目概述

## 项目目标

本项目是一个前后端分离的 B 端房源管理后台，用于展示全栈开发能力。交付目标不是业务无限扩张，而是做出一个结构清晰、权限完整、可以演示和上线的 MVP。

## MVP 范围

必须优先完成以下能力：

1. 登录与退出
2. 首页看板
3. 整租管理
4. 合租管理
5. 集中管理
6. 租客登记
7. 房东登记
8. 员工管理
9. 员工授权
10. 维修工单管理
11. 保洁工单管理
12. 菜单权限与按钮权限控制

## 默认不做的范围

以下内容默认为二期或扩展项，不主动开做：

1. 合同管理
2. 收租提醒
3. 报修评价
4. 房态时间轴
5. 导入导出
6. 操作日志完善
7. 数据大屏
8. 多组织隔离的完整实现

## 架构方向

- 前后端分离
- 单体后端服务，不拆微服务
- 业务模块化清晰划分
- Redis 负责登录态与热点缓存
- 文件存储首期可用本地磁盘，二期再考虑 MinIO

## 工作区结构

```text
all-project/
  frontend/
    house-admin/
  backend/
    house-service/
  career-docs/
```

默认要求：

- 新建前端项目时放在 `frontend/house-admin`
- 新建后端项目时放在 `backend/house-service`
- 补充的 SQL、接口文档、部署说明、阶段总结放在 `career-docs/`

## 技术栈

### 前端

- Vue 3
- Vite
- Pinia
- Vue Router
- Element Plus
- ECharts
- Axios
- Sass

### 后端

- Java 17
- Spring Boot 3.x
- Spring Security
- JWT
- MyBatis-Plus
- MySQL 8
- Redis
- Hibernate Validator
- Knife4j 或 SpringDoc OpenAPI

### 工程化与部署

- Maven
- Docker / Docker Compose
- Nginx
- Git

## 成功标准

如果用户没有另行定义验收标准，默认以以下结果视为成功：

1. 前后端目录结构完整，能本地运行或至少成功构建
2. 登录、权限、菜单、核心业务模块具备联调能力
3. 房源、租客、房东、工单、首页看板至少完成 MVP 级页面和接口
4. 数据库设计与接口命名统一，便于简历展示和后续扩展
5. 项目具备基础部署说明，能说明 Nginx、MySQL、Redis、后端、前端的关系
