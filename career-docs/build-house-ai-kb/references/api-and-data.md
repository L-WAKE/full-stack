# 数据与接口

## 核心数据表

用户与权限相关：

- `user`
- `role`
- `user_role`
- `knowledge_space`
- `knowledge_space_member`

文档相关：

- `document`
- `document_parse_result`
- `document_chunk`
- `document_chunk_embedding`

会话相关：

- `conversation`
- `conversation_message`
- `conversation_citation`

## 推荐核心字段

`knowledge_space`

- `id`
- `name`
- `description`
- `org_id`
- `created_by`
- `created_at`

`document`

- `id`
- `space_id`
- `file_name`
- `file_type`
- `file_size`
- `storage_path`
- `parse_status`
- `chunk_count`
- `created_by`
- `created_at`

`document_chunk`

- `id`
- `document_id`
- `space_id`
- `chunk_index`
- `content`
- `content_preview`
- `page_no`
- `section_path`
- `token_estimate`

`conversation`

- `id`
- `space_id`
- `user_id`
- `title`
- `created_at`
- `updated_at`

`conversation_message`

- `id`
- `conversation_id`
- `role`
- `content`
- `model_name`
- `prompt_tokens`
- `completion_tokens`
- `created_at`

向量映射说明：

- 如果向量存储在 Qdrant 中，`document_chunk_embedding` 可以只维护 `chunk_id` 和 `vector_point_id` 的映射关系。

## 接口前缀

接口统一使用 `/api` 作为前缀，除非现有项目已经有明确统一规范。

## 鉴权接口

- `POST /api/auth/login`
- `GET /api/auth/profile`
- `GET /api/auth/spaces`

## 知识库空间接口

- `GET /api/spaces`
- `POST /api/spaces`
- `PUT /api/spaces/{id}`
- `GET /api/spaces/{id}`
- `POST /api/spaces/{id}/members`

## 文档接口

- `GET /api/documents`
- `POST /api/documents/upload`
- `GET /api/documents/{id}`
- `DELETE /api/documents/{id}`
- `POST /api/documents/{id}/parse`
- `GET /api/documents/{id}/chunks`

## 检索与聊天接口

- `POST /api/retrieval/search`
- `POST /api/chat/conversations`
- `GET /api/chat/conversations`
- `GET /api/chat/conversations/{id}/messages`
- `POST /api/chat/conversations/{id}/messages`
- `GET /api/chat/conversations/{id}/stream`

## 管理接口

- `GET /api/models`
- `PUT /api/settings/model`
- `GET /api/statistics/overview`

## 后端服务拆分建议

后端保持模块化单体即可，可以按职责拆为：

- `auth_service`
- `document_service`
- `parser_service`
- `chunk_service`
- `embedding_service`
- `retrieval_service`
- `chat_service`

## 文档处理约定

建议采用异步流程：

1. 上传文件
2. 创建 `document` 记录，状态为 `PENDING`
3. 投递解析任务
4. 执行解析、清洗、切片、向量化
5. 元数据写入 PostgreSQL，向量写入 Qdrant
6. 成功则标记 `READY`，失败则标记 `FAILED`

## 切片约定

默认采用语义切片：

1. 先按标题切
2. 再按段落切
3. 对超长内容再按长度二次切分
4. 保留重叠内容

推荐默认值：

- 切片长度：中文 `500-800` 字
- 重叠长度：`100-150` 字

每个切片至少保存：

- 文档 ID
- 空间 ID
- 顺序索引
- 原始内容
- 预览内容
- 页码
- 标题路径
- token 长度估算

## 检索约定

推荐流程：

1. 用户发起提问
2. 生成查询向量
3. 执行向量 TopK 召回
4. 可选执行关键词召回
5. 融合候选结果
6. 进行重排
7. 拼装上下文
8. 生成带引用的回答

`retrieval_service` 建议返回：

- 命中切片列表
- 相似度分数
- 重排分数
- 最终拼装上下文

## 会话约定

会话必须绑定知识库空间。
默认不要把全部历史消息无脑传给模型。

推荐上下文策略：

- 保留最近 `6-10` 轮消息
- 超限时对历史内容做摘要压缩
- 在系统提示词中强制要求给出引用来源

## 流式输出约定

优先使用 `SSE`，必要时区分以下事件：

- token 增量
- 引用来源
- 完成事件
- 错误事件

## 权限约定

至少覆盖以下隔离维度：

- 用户隔离
- 角色隔离
- 知识库空间隔离
- 文档可见范围隔离

最重要的运行时规则：

- 检索时只能在当前用户有权限访问的空间内搜索。
