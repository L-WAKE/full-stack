# AI Knowledge Base Workspace

这个工作区已经按 `career-docs/build-house-ai-kb` 的 skill 开始搭建前后端项目，并已从演示骨架推进到“真实可持续开发”的第一步：后端开始使用 SQLAlchemy、PostgreSQL 连接和 Alembic 迁移骨架，同时接入真实 PDF/DOCX 解析与向量检索代码路径。

## 目录

```text
frontend/ai-kb-web
backend/ai-kb-service
career-docs/build-house-ai-kb
docker-compose.yml
```

## 当前已完成

- `frontend/ai-kb-web`
  - Vue 3 + Vite + Pinia + Vue Router + Element Plus 基础项目
  - 登录页、总览页、空间管理、文档中心、聊天页、设置页
  - Axios 请求封装与基础状态管理
- `backend/ai-kb-service`
  - FastAPI 入口与 `/api` 路由
  - 登录、用户资料、空间、文档、检索、聊天、SSE、统计接口
  - 已落地 SQLAlchemy 模型、数据库会话、启动初始化和种子数据
  - 已补 `alembic` 目录、`alembic.ini` 和首个初始化迁移
  - 已接入 `PyMuPDF`、`python-docx`、真实切片服务和 Qdrant 检索适配
- 基础基础设施
  - `docker-compose.yml` 已定义 PostgreSQL、Redis、Qdrant

## 启动方式

### 后端

```powershell
cd E:\project\study\all-project
docker compose up -d

cd E:\project\study\all-project\backend\ai-kb-service
python -m venv .venv
.venv\Scripts\Activate.ps1
pip install -r requirements.txt
copy .env.postgres .env
alembic upgrade head
uvicorn main:app --reload
```

如果本机暂时没有 PostgreSQL / Qdrant 服务，可以临时切到 SQLite 本地开发模式：

```powershell
cd E:\project\study\all-project\backend\ai-kb-service
copy .env.sqlite .env
uvicorn main:app --reload
```

### 前端

```powershell
cd E:\project\study\all-project\frontend\ai-kb-web
npm install
copy .env.example .env
npm run dev
```

### 基础服务

```powershell
cd E:\project\study\all-project
docker compose up -d
```

## 建议的下一步

1. 用真实 PostgreSQL、Redis、Qdrant 服务替换当前本机兜底运行环境。
2. 把解析任务和向量入库改造成后台任务队列，而不是同步触发。
3. 把空间成员、文档可见性和检索范围做成更细粒度的真实权限校验。
4. 为模型适配层补上 Chat / Embedding / Rerank 的实际供应商实现与配置中心。
