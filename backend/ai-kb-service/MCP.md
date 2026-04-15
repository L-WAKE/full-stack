# AI Knowledge Base MCP Server

This service exposes the enterprise knowledge-base project as a Model Context Protocol server.

For concrete client setup examples, see [MCP_CLIENTS.md](./MCP_CLIENTS.md).

## What It Exposes

Tools:

- `list_spaces`: list knowledge spaces visible to a user.
- `get_space_detail`: inspect one knowledge space and its members.
- `list_documents`: list documents in a knowledge space.
- `get_document_chunks`: inspect parsed document chunks.
- `search_knowledge`: run semantic retrieval and return matched chunks.
- `ask_knowledge_base`: return a grounded answer plus citations, using a real LLM when configured.
- `get_kb_overview`: return operational metrics for accessible spaces.

Resources:

- `kb://spaces/{username}`
- `kb://spaces/{username}/{space_id}`
- `kb://documents/{username}/{space_id}`
- `kb://overview/{username}`
- `kb://strategy/retrieval`

Prompts:

- `customer_support_grounded_answer`
- `implementation_handoff_review`

## Run Locally

Install the MCP dependency in the Python environment you use for this service:

```powershell
python -m pip install -r requirements-mcp.txt
```

Run with stdio transport, which is the common mode for local MCP clients:

```powershell
python -m app.mcp_server
```

Run with streamable HTTP transport for local testing:

```powershell
$env:MCP_TRANSPORT = "streamable-http"
$env:MCP_PORT = "8765"
python -m app.mcp_server
```

The default demo user is `admin`. Most tools accept a `username` argument so clients can test access boundaries.

## Real LLM Answer Generation

`ask_knowledge_base` can call a real OpenAI-compatible chat-completions endpoint when these variables are configured:

```text
LLM_API_KEY=YOUR_API_KEY
LLM_BASE_URL=https://api.openai.com/v1
LLM_MODEL=gpt-4.1-mini
LLM_TEMPERATURE=0.2
LLM_TIMEOUT_SECONDS=30
```

If these variables are not configured, the server returns a grounded retrieval fallback instead of a model-generated answer.

## Example Tool Payloads

```json
{
  "space_id": 1,
  "query": "客户咨询操作流程时应该优先参考哪份 SOP？",
  "top_k": 5,
  "username": "admin"
}
```

```json
{
  "space_id": 1,
  "question": "实施交付前需要检查哪些事项？",
  "username": "admin"
}
```
