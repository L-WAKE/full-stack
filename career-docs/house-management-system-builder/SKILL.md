---
name: house-management-system-builder
description: 用于在当前工作区构建或扩展房源管理系统 MVP。适用于创建或迭代 `frontend/house-admin` 与 `backend/house-service`，实现 Vue 3 管理后台与 Spring Boot API，设计数据库表和 REST 接口，接入 RBAC 菜单与按钮权限，开发首页看板、房源和房间、租客和房东、员工、角色、菜单、维修与保洁模块，以及完善部署与交付文档。
---

# 房源管理系统开发 Skill

## 目标

交付一个可运行、可演示、适合写进简历的房源管理系统 MVP，而不是停留在高层方案或伪代码层面。默认目标是在当前工作区落地可运行的项目结构、后端接口契约、前端页面、权限接入与交付文档。

## 工作区约定

- 前端应用固定放在 `frontend/house-admin`
- 后端服务固定放在 `backend/house-service`
- SQL、交付说明、部署文档和补充材料统一放在 `career-docs/`
- 如果目标目录为空，先完成项目初始化，再进入业务开发
- 如果目标目录已有代码，先检查现状，再在现有实现上扩展

## 默认技术栈

- 前端：Vue 3、Vite、Pinia、Vue Router、Element Plus、Axios、Sass、ECharts
- 后端：Java 17、Spring Boot 3、Spring Security、JWT、Redis、MyBatis-Plus、MySQL 8、Hibernate Validator
- 工程化：Maven、Docker Compose、Nginx、Git
- 如果前端现有代码是 JavaScript，就保持 JavaScript；如果从零开始且没有额外约束，可以使用 TypeScript

## 工作流决策

- 如果工作区还是空的，优先完成项目骨架、鉴权、布局、路由与公共基础设施
- 如果用户只要求某个模块，先检查现有后端包结构、前端路由树、权限模型和相关参考资料，再开始修改
- 如果用户只要求文档、SQL 或部署材料，仍然要和当前代码实现、数据模型保持一致
- 如果用户的要求比较宽泛，就推进到下一个未完成里程碑，不要同时摊开太多半成品模块

## 执行流程

1. 先检查工作区，再阅读 `references/project-brief.md`
2. 只加载当前任务真正需要的参考资料
3. 每次选择一个纵向切片，端到端完成
4. 保持后端接口契约、前端表单和表格、菜单配置、权限码、数据库字段一致
5. 优先复用公共组件、枚举、统一响应结构和校验规则，不要重复造轮子
6. 交付前尽量运行当前可用的最强校验
7. 明确说明假设、验证结果和剩余缺口

## 架构约束

- 不要把项目拆成微服务
- 后端统一使用 `/api` 作为前缀
- 所有接口统一返回 `code`、`message`、`data`
- 权限模型固定采用用户、角色、菜单、权限的 RBAC 设计
- MVP 至少实现菜单可见权限和按钮权限
- 房源建模采用统一的 `house` 聚合，房间粒度场景使用 `house_room`
- 保留 `org_id`、`created_by`、`updated_by`、`deleted` 等扩展字段
- 除非用户明确要求，否则不要主动扩展财务、合同审批、消息中心或复杂 BI 分析

## 纵向切片完成标准

一个模块只有在满足以下大部分条件时，才算真正完成：

- 后端实体、DTO 或 VO、Mapper、Service、Controller 已补齐，或者在现有代码结构中有等价实现
- 前端路由、页面、API 层，以及核心表单或表格流程已经接通
- 菜单项和权限码与页面上的可见操作保持一致
- 关键枚举、字典、状态标签在前后端保持统一
- 在条件允许时，通过构建、编译或现有自动化测试等真实校验

## 参考资料加载指引

先从 `references/project-brief.md` 了解范围、目标与技术栈。

涉及以下内容时，加载 `references/domain-model.md`：

- 实体设计
- 表结构
- 字段命名
- 状态枚举
- 业务关系

涉及以下内容时，加载 `references/api-and-ui.md`：

- REST 接口
- 鉴权流程
- 路由设计
- 页面结构
- Store 设计
- 后端包结构

涉及以下内容时，加载 `references/delivery-roadmap.md`：

- 开发顺序
- 里程碑范围
- 验收检查项
- 部署交付物

如果用户请求比较宽泛，或者需要一个干净的起始提示词、按阶段拆分任务时，加载 `references/invocation-patterns.md`。

## 交付说明

- 优先交付一个完整可运行的增量，不要同时留下多个未完成模块
- 代码和文档都要适合拿去做作品展示和面试讲解
- 如果需求不完整，优先选择最简单且符合实施方案的实现方式，并在完成后说明假设
