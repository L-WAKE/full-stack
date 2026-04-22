# 项目工作区说明

本仓库是一个面向业务系统开发与 AI 应用实践的综合工作区，当前主要包含两套可独立运行、也可分别演示的项目：

1. AI 知识库系统：用于知识空间管理、文档上传解析、向量检索、对话问答与模型配置。
2. 房屋管理系统：用于房源、业主、租客、工单、员工、角色与菜单等后台管理场景。

除了前后端业务代码外，仓库中还保留了交付文档、技能脚本、环境启动脚本和冒烟测试脚本，适合继续开发、功能演示、课程学习或做项目作品集整理。

## 在线演示

为了便于快速体验，两个子项目均已提供可访问的演示环境：

- AI 知识库系统演示地址：[https://kb.liulaoban.online](https://kb.liulaoban.online)（备用 IP：[http://82.156.253.71:8089](http://82.156.253.71:8089)）
- 房屋管理系统演示地址：[https://liulaoban.online/#/login](https://liulaoban.online/#/login)（备用 IP：[http://82.156.253.71:8088](http://82.156.253.71:8088)）

如果你希望先看效果、再阅读源码或本地启动项目，建议优先从以上演示地址进入体验。

## 快速查看

如果你是第一次打开这个仓库，可以按下面的顺序快速了解整个项目：

1. 先访问在线演示，了解两个系统的页面风格与核心功能。
2. 再阅读下方“子项目说明”，快速掌握 AI 知识库系统与房屋管理系统的业务定位。
3. 如需本地运行，可直接查看“环境与运行方式”章节，按对应步骤启动前后端及依赖服务。

## 1. 工作区总览

### 1.1 目录结构

```text
all-project
├─ backend
│  ├─ ai-kb-service          # AI 知识库后端（FastAPI）
│  └─ house-service          # 房屋管理后端（Spring Boot）
├─ frontend
│  ├─ ai-kb-web              # AI 知识库前端（Vue 3 + Vite）
│  └─ house-admin            # 房屋管理前端（Vue 3 + Vite）
├─ career-docs               # 项目方案、实施文档、技能说明、学习资料
├─ tools                     # 环境启动/停止/冒烟测试脚本
├─ docker-compose.yml        # AI 知识库依赖基础设施编排
└─ README.md
```

### 1.2 工作区定位

这个仓库不是单一应用仓库，而是一个多项目集合工作区，特点如下：

- 同时覆盖 AI 应用和传统管理后台两类典型项目形态。
- 同时保留前端、后端、数据库、缓存、向量检索、文档处理和脚本工具链。
- 既可以作为独立项目开发使用，也可以作为学习全栈工程实践的案例仓库。
- `career-docs` 中沉淀了项目实施方案、技能说明、阶段总结和专题材料，便于后续继续扩展。

## 2. 子项目说明

### 2.1 AI 知识库系统

#### 2.1.1 项目目标

AI 知识库系统用于构建“空间化管理 + 文档知识沉淀 + 检索增强问答”的基础能力。它面向的核心场景包括：

- 按知识空间组织团队资料。
- 上传 PDF、DOCX 等文档并进行解析。
- 将文档内容切片后写入向量检索链路。
- 在指定空间内进行知识检索和多轮问答。
- 支持模型配置、统计看板和聊天引用结果展示。

#### 2.1.2 技术栈

前端 `frontend/ai-kb-web`

- Vue 3：用于构建单页应用界面。
- Vite：负责前端开发调试与构建。
- Vue Router：负责登录页、看板页、空间页、文档页、聊天页、设置页等路由组织。
- Pinia：用于登录态、工作区状态等前端状态管理。
- Axios：统一封装 HTTP 请求，与后端 `/api` 接口对接。
- Element Plus：承担后台风格 UI 组件，例如表单、表格、按钮、对话框等。

后端 `backend/ai-kb-service`

- FastAPI：提供 REST API 与流式接口，适合快速构建 AI 服务型后端。
- Uvicorn：FastAPI 本地开发和部署运行入口。
- Pydantic Settings：统一读取 `.env` 配置。
- SQLAlchemy 2：负责业务实体建模与数据库访问。
- Alembic：负责数据库版本迁移与表结构演进。
- Psycopg：连接 PostgreSQL。
- Redis：作为缓存/会话等基础能力预留。
- Qdrant：作为向量数据库，承接文档切片后的向量检索。
- PyMuPDF：用于 PDF 解析。
- python-docx：用于 DOCX 文档读取。
- python-multipart：支持文件上传接口。

基础设施

- PostgreSQL 16：存储用户、空间、文档、对话、引用等关系型数据。
- Redis 7：提供缓存与扩展支持。
- Qdrant 1.13：提供语义检索与向量数据管理。
- Docker Compose：统一拉起 AI 知识库相关基础服务。

#### 2.1.3 核心功能

认证与用户相关

- 登录接口：提供基础账号登录能力。
- 用户资料：支持获取当前用户信息。
- 我的空间：支持根据用户身份返回其可访问的知识空间列表。

知识空间管理

- 空间列表查询。
- 新建知识空间。
- 查询单个空间详情。
- 编辑空间名称、描述等基础信息。
- 为空间添加成员。
- 调整成员角色。
- 从空间中移除成员。
- 统计空间下文档数量。

文档管理与知识入库

- 按空间查询文档列表。
- 上传文档并登记文件类型、大小、路径等元数据。
- 删除文档。
- 手动触发文档解析。
- 更新文档可见范围，例如空间内可见等。
- 查询文档切片列表。

文档解析与检索链路

- 支持 PDF 与 DOCX 的原始内容读取。
- 将解析结果进行切片，形成 `document_chunk` 数据。
- 为切片建立向量索引映射信息。
- 将切片与向量点位关联到 Qdrant 检索集合。
- 通过检索接口完成知识召回，为问答能力提供上下文。

对话与问答

- 创建会话。
- 查询会话列表。
- 查询会话消息记录。
- 向指定会话发送消息。
- 通过 SSE 流式返回回答内容。
- 为消息记录保存引用片段、分数、页码、片段摘要等信息，方便前端展示答案依据。

系统配置与统计

- 查询可用模型列表。
- 更新当前使用模型配置。
- 获取统计概览数据，用于看板展示。

#### 2.1.4 数据模型特点

当前 AI 知识库后端已经具备较清晰的核心实体设计，主要包括：

- `user`：用户信息。
- `knowledge_space`：知识空间。
- `knowledge_space_member`：空间成员与角色关系。
- `document`：文档元数据、解析状态、可见范围。
- `document_chunk`：文档切片内容、页码、章节路径、预估 token 数。
- `document_chunk_embedding`：切片与向量点、模型、提供方之间的映射关系。
- `conversation`：会话主表。
- `conversation_message`：消息记录。
- `conversation_citation`：消息引用片段与召回分数。

这套设计说明该项目已经不只是“界面原型”，而是具备继续向真实 RAG 系统扩展的基础结构。

#### 2.1.5 前端页面能力

`frontend/ai-kb-web` 当前已具备以下页面与交互入口：

- 登录页。
- 仪表盘页。
- 知识空间管理页。
- 文档中心页。
- 聊天问答页。
- 模型设置页。

前端已经完成基础路由守卫，会根据本地 token 控制是否允许进入业务页面。

### 2.2 房屋管理系统

#### 2.2.1 项目目标

房屋管理系统是一个典型的后台管理项目，覆盖租赁管理中的常见运营场景，适合作为“管理后台 + 业务中台”练习或作品集项目。其核心用途包括：

- 管理整租、合租、集中式等不同类型房源。
- 维护业主与租客档案。
- 跟踪保洁、维修等工单处理流程。
- 提供员工、角色、菜单等系统管理能力。
- 通过看板接口展示业务概览和趋势数据。

#### 2.2.2 技术栈

前端 `frontend/house-admin`

- Vue 3：构建管理后台界面。
- Vite：开发与构建工具。
- Vue Router：负责后台路由组织。
- Pinia：负责登录状态、用户资料和菜单初始化。
- Axios：负责接口请求封装。
- Element Plus：管理后台主 UI 组件库。
- `@element-plus/icons-vue`：图标支持。
- ECharts：仪表盘趋势图和概览图表展示。
- Sass：后台样式组织与主题扩展。

后端 `backend/house-service`

- Spring Boot 3.3.4：房屋管理后端基础框架。
- Spring Web：提供 REST API。
- Spring Security：负责登录鉴权、接口访问控制。
- Spring Validation：负责请求参数校验。
- Spring Data Redis：负责会话存储扩展。
- MyBatis-Plus：简化实体、Mapper、分页与 CRUD 开发。
- MyBatis-Plus JSQLParser：辅助分页与 SQL 解析能力。
- MySQL Connector/J：连接 MySQL 数据库。
- H2：提供轻量内存数据库运行支持。
- Maven Wrapper：保证后端项目可用 `mvnw.cmd` 构建。
- JDK 17：后端运行版本。

#### 2.2.3 核心功能

登录与权限

- 用户登录。
- 用户退出。
- 获取当前登录人资料。
- 获取当前用户菜单。
- 支持基于 token 的认证过滤。
- 支持内存会话存储和 Redis 会话存储切换。

首页看板

- 获取业务概览数据。
- 获取趋势数据。
- 前端结合 ECharts 展示核心统计结果。

房源管理

- 房源列表查询。
- 房源详情查询。
- 新增房源。
- 编辑房源。
- 修改房源状态。
- 删除房源。
- 前端按整租、合租、集中式三类入口组织页面。

客户管理

- 业主列表查询。
- 业主下拉选项查询。
- 新增、编辑、删除业主。
- 租客列表查询。
- 新增、编辑、删除租客。

工单管理

- 维修工单列表、新增、编辑、删除。
- 保洁工单列表、新增、编辑、删除。
- 支持优先级、状态、指派人、预约时间等字段管理。

系统管理

- 员工列表、新增、编辑、状态变更。
- 角色列表、新增、编辑。
- 菜单树查询。
- 菜单新增、编辑。

#### 2.2.4 数据结构特点

`backend/house-service/src/main/resources/schema.sql` 中已经定义了典型后台管理系统的数据表，包括：

- `sys_user`：系统用户。
- `sys_role`：角色。
- `sys_user_role`：用户与角色关联。
- `sys_menu`：菜单定义。
- `sys_role_menu`：角色菜单关联。
- `landlord`：业主档案。
- `tenant`：租客档案。
- `house`：房源主体信息。
- `maintenance_order`：维修工单。
- `cleaning_order`：保洁工单。

从表结构可以看出，项目已经覆盖“人员、权限、房源、客户、工单”这几条后台业务主线，结构较完整，适合继续细化成正式业务系统。

#### 2.2.5 前端页面能力

`frontend/house-admin` 当前已具备以下主要页面：

- 登录页。
- 首页看板。
- 整租房源管理。
- 合租房源管理。
- 集中式房源管理。
- 租客管理。
- 业主管理。
- 维修工单管理。
- 保洁工单管理。
- 员工管理。
- 角色管理。
- 菜单管理。

## 3. 文档与辅助资源

`career-docs` 目录下保留了较多交付与学习资料，主要价值包括：

- AI 知识库系统实施方案。
- 房屋管理系统实施方案。
- 各项目阶段性总结。
- 技能说明文件 `SKILL.md`。
- 参考资料与开发路线说明。
- 学习型文档与 PDF 材料。

如果后续要继续做简历项目包装、方案汇报或功能迭代，`career-docs` 目录可以直接作为现成资料库使用。

## 4. 环境与运行方式

### 4.1 AI 知识库系统启动

#### 4.1.1 启动基础设施

在仓库根目录执行：

```powershell
cd E:\project\study\all-project
docker compose up -d
```

将启动以下服务：

- PostgreSQL：`127.0.0.1:5432`
- Redis：`127.0.0.1:6379`
- Qdrant：`127.0.0.1:6333`

#### 4.1.2 启动后端

```powershell
cd E:\project\study\all-project\backend\ai-kb-service
python -m venv .venv
.venv\Scripts\Activate.ps1
pip install -r requirements.txt
copy .env.postgres .env
alembic upgrade head
uvicorn main:app --reload
```

如果本机暂时不想依赖 PostgreSQL / Qdrant，也可以切换到本地开发配置：

```powershell
cd E:\project\study\all-project\backend\ai-kb-service
copy .env.sqlite .env
uvicorn main:app --reload
```

#### 4.1.3 启动前端

```powershell
cd E:\project\study\all-project\frontend\ai-kb-web
npm install
copy .env.example .env
npm run dev
```

### 4.2 房屋管理系统启动

#### 4.2.1 启动依赖环境

仓库中提供了 PowerShell 脚本帮助启动本地 MySQL 和 Redis：

```powershell
cd E:\project\study\all-project
.\tools\start-house-env.ps1
```

该脚本会检查：

- MySQL 是否监听 `3306`
- Redis 服务是否正常运行

如需停止相关环境，可使用：

```powershell
cd E:\project\study\all-project
.\tools\stop-house-env.ps1
```

#### 4.2.2 启动后端

```powershell
cd E:\project\study\all-project\backend\house-service
.\mvnw.cmd spring-boot:run
```

默认配置说明：

- 端口：`8080`
- 数据库：MySQL
- Redis：本地 Redis
- 会话存储：默认 `memory`，也可切换为 Redis

#### 4.2.3 启动前端

```powershell
cd E:\project\study\all-project\frontend\house-admin
npm install
npm run dev
```

#### 4.2.4 冒烟测试

仓库中已经提供房屋管理后端的冒烟测试脚本：

```powershell
cd E:\project\study\all-project
.\tools\smoke-test-house-service.ps1
```

该脚本会验证以下流程：

- 登录接口是否可用。
- 看板接口是否可用。
- 维修工单的新增、修改、删除。
- 保洁工单的新增、修改、删除。
