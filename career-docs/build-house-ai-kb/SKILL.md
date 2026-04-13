---
name: build-house-ai-kb
description: 实现、搭建、扩展或排查房源管理场景下的 AI 知识库与 RAG 项目时使用本技能。适用于在当前工作区开发基于 Vue 3、FastAPI、PostgreSQL、Redis、Qdrant 的知识库系统，包括登录、知识库空间、PDF/DOCX 上传、文档解析、切片、向量化、检索问答、引用来源、会话管理、SSE 流式输出、权限隔离、Docker Compose 部署，以及后续从 RAG 升级到 Agent 能力的场景。
---

# 房源 AI 知识库开发技能

## 任务目标

构建一个面向房源管理业务的 AI 知识库系统。
首期目标是把 RAG 链路做完整、做稳定，而不是一开始就做复杂自治 Agent。
优先保证下面这条主链路可用：

`文档 -> 解析 -> 切片 -> 向量化 -> 检索 -> 引用 -> 回答 -> 权限校验`

只有当这条链路已经稳定后，才继续扩展 Agent 类能力。

## 开始之前

1. 先检查当前工作区已有的前端、后端、部署文件和文档，不要盲目重建。
2. 如果房源管理系统里已经有可复用的用户、角色、组织、接口风格或 UI 模式，优先复用成熟做法，但不要过早形成强耦合。
3. 开发前先阅读 [references/project-spec.md](references/project-spec.md)，了解项目范围、架构和模块边界。
4. 涉及数据表、接口、服务职责时，阅读 [references/api-and-data.md](references/api-and-data.md)。
5. 涉及阶段推进、验收标准、开发顺序时，阅读 [references/delivery-checklist.md](references/delivery-checklist.md)。
6. 如果仍有细节不清楚，再回看原始方案文档 [../02-AI-Agent知识库系统-实施技术方案.md](../02-AI-Agent知识库系统-实施技术方案.md)。

## 不可偏离的约束

- 前端优先使用 `Vue 3 + JavaScript + Vite + Pinia + Vue Router + Element Plus + Axios`，除非仓库已经存在更强的本地约束。
- 后端优先使用 `Python 3.11 + FastAPI + Pydantic + SQLAlchemy + Alembic`。
- 结构化数据存 `PostgreSQL`，缓存/队列/会话加速使用 `Redis`，向量存储使用 `Qdrant`。
- 文件存储首期先用本地磁盘目录，不要一开始就把 `MinIO` 作为硬依赖。
- 后端保持“模块化单体”结构，不要过早拆成微服务。
- 首期只做好 `PDF` 和 `DOCX`，不要急着扩展到 OCR、Excel、PPT。
- 流式输出优先使用 `SSE`，首版不要默认 WebSocket。
- 即使向量在 Qdrant 中，也必须把文档和切片元数据保存在 PostgreSQL。
- 会话必须和知识库空间绑定，避免跨空间上下文污染。
- 检索时只能搜索当前用户有权限访问的空间和文档。
- 模型接入必须有适配层，至少区分 `Chat`、`Embedding`、`Rerank` 三类能力。

## 默认目录目标

如果工作区里还没有更合适的现成结构，优先使用下面这套目录：

```text
all-project/
  frontend/
    house-admin/
    ai-kb-web/
  backend/
    house-service/
    ai-kb-service/
  career-docs/
```

前端建议目录：

```text
frontend/ai-kb-web/
  src/
    api/
    assets/
    components/
      chat/
      kb/
      upload/
      citation/
    router/
    stores/
    utils/
    views/
      login/
      dashboard/
      kb-space/
      document/
      chat/
      setting/
```

后端建议目录：

```text
backend/ai-kb-service/
  app/
    api/
    core/
    db/
    models/
    schemas/
    services/
      auth/
      document/
      parser/
      chunk/
      embedding/
      retrieval/
      chat/
    workers/
```

## 执行流程

### 1. 先建立项目基线

- 先确认 `frontend/ai-kb-web` 和 `backend/ai-kb-service` 是否已经存在。
- 如果某一侧缺失，就按推荐技术栈补齐，不要混入无关框架。
- 尽早补齐环境配置、接口基础地址、数据库配置、Docker Compose 基础服务。
- 优先保证项目可以跑起来，再考虑复杂抽象。

### 2. 先完成第一阶段基础设施

- 先实现登录、个人信息获取、可访问空间列表。
- 先打通 PostgreSQL 模型、Alembic 迁移、Redis 连接、Qdrant 连接。
- 先建立模型适配层接口，为聊天模型、向量模型、重排模型预留独立封装。
- 鉴权实现保持相对独立，便于后续替换成和房源管理系统统一登录。

### 3. 再做知识库空间和文档中心

- 实现知识库空间的新增、编辑、成员分配。
- 实现文档上传、文件类型校验、大小校验。
- 文件上传成功后立即落库，文档状态先写为 `PENDING`。
- 前端需要清楚展示解析状态、切片数量和失败原因。

### 4. 再做文档处理链路

- `PDF` 解析使用 `PyMuPDF`。
- `DOCX` 解析使用 `python-docx`。
- 文本清洗要尽量保留标题层级和段落结构，不要简单粗暴拼成纯文本。
- 切片优先按标题分块，再按段落切片，超长内容再按长度二次切分。
- 默认切片参数可从中文 `500-800` 字、重叠 `100-150` 字起步。
- 每个切片至少保存 `document_id`、`space_id`、`chunk_index`、`content`、`content_preview`、`page_no`、`section_path`、`token_estimate` 和向量映射关系。
- 上传 -> 解析 -> 切片 -> 向量化 应使用异步流程。
- 成功后文档状态更新为 `READY`，失败后更新为 `FAILED`。

### 5. 再做检索和问答

- 检索优先采用“向量召回 + 可选关键词召回 + 重排 + 上下文拼装”的方式。
- `retrieval_service` 至少返回命中切片、相似度分数、重排分数和拼装后的上下文。
- 系统提示词要明确要求模型只能基于检索结果回答，不知道就直说，并且必须给引用来源。
- 回答生成采用 `SSE` 流式返回。
- 如有必要，按事件类型分别返回 token 增量、引用来源、完成事件、错误事件。

### 6. 做引用来源和会话管理

- 会话、消息、引用都要和知识库空间关联。
- 支持会话创建、重命名、删除、历史消息查看、按空间过滤。
- 每次回答都尽量返回引用卡片，至少包含文档名、页码、片段摘要、命中分数。
- 聊天页面建议采用：左侧会话列表，中间对话区，右侧引用来源区。

### 7. 最后做权限隔离和优化

- 权限至少覆盖用户、角色、知识库空间、文档可见范围。
- 每次检索必须限制在当前用户可访问范围内。
- 只有在基础链路稳定后，再增加重排、查询改写、按空间定制提示词等优化。
- 核心功能可用后，再补齐 Docker Compose 本地部署。

## 决策规则

- 优先选择“最简单但可扩展”的实现方案。
- 优先增量交付，不做大而空的重构。
- 用户说“继续开发这个项目”时，默认从最近未完成的阶段继续，而不是从头重做。
- 已经稳定运行的脚手架不要随意推翻，除非它已经阻塞核心功能实现。
- 如果模型密钥或第三方服务尚未准备好，先把适配层、配置项、接口边界搭好，不要把代码绑死在某一家供应商上。
- 如果异步方案还没最终确定，可以先留清晰的 Worker 边界，再在 `Celery` 或 `FastAPI BackgroundTasks` 之间统一选一种。
- 如果要和第一个房源系统联动，优先复用用户、角色、组织这些概念，但没有明确要求前不要强耦合。

## 首期最小可交付版本

只有同时满足下面这些能力，才能认为首期版本基本完成：

- 登录和当前用户信息
- 知识库空间管理
- PDF / DOCX 上传
- 异步解析和切片
- 向量入库到 Qdrant，元数据保存在 PostgreSQL
- 检索与问答
- 引用来源展示
- 会话管理
- SSE 流式回答
- 权限隔离
- Docker Compose 本地部署

如果这些能力缺任何一项，都应视为“RAG 基础设施尚未完整”。

## 二期升级方向

下面这些属于第二阶段能力：

- 工具调用
- 业务 API 联动
- 智能表单填充
- 流程引导
- 工单助手
- SQL 查询助手

不要让二期想法打断首期 RAG 主链路交付。

## 验证重点

- 至少使用一份真实可访问文档，验证从上传到“带引用回答”的全链路。
- 验证无权限用户无法检索到不属于自己的空间内容。
- 验证会话始终绑定在当前知识库空间，不会串空间。
- 验证解析失败时，前后端都能看到清晰的失败状态。
- 验证模型异常、网络异常时，流式输出能优雅失败，而不是卡死。

## 参考资料导航

- 项目范围和架构：读取 [references/project-spec.md](references/project-spec.md)
- 数据模型和接口设计：读取 [references/api-and-data.md](references/api-and-data.md)
- 开发顺序和验收标准：读取 [references/delivery-checklist.md](references/delivery-checklist.md)
