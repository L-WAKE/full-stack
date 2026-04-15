# MCP Client Config Examples

This document shows practical examples for connecting the knowledge-base MCP server to common clients on Windows.

Project root used in the examples:

`E:\project\study\all-project\backend\ai-kb-service`

Python used in the examples:

`E:\project\study\all-project\backend\ai-kb-service\.venv\Scripts\python.exe`

## 1. Claude Desktop

Anthropic documents show that Claude Desktop uses a `claude_desktop_config.json` file with an `mcpServers` object containing `command`, `args`, and `env`.

Example:

```json
{
  "mcpServers": {
    "ai-kb": {
      "command": "E:\\project\\study\\all-project\\backend\\ai-kb-service\\.venv\\Scripts\\python.exe",
      "args": ["-m", "app.mcp_server"],
      "env": {
        "PYTHONPATH": "E:\\project\\study\\all-project\\backend\\ai-kb-service",
        "MCP_TRANSPORT": "stdio",
        "LLM_API_KEY": "YOUR_API_KEY",
        "LLM_BASE_URL": "https://api.openai.com/v1",
        "LLM_MODEL": "gpt-4.1-mini"
      }
    }
  }
}
```

If you want to keep secrets out of the Claude Desktop config file, you can omit the LLM variables there and instead set them in the machine environment before launching Claude Desktop.

Suggested first prompts in Claude Desktop:

- `List all accessible knowledge spaces from the ai-kb server.`
- `Search the support knowledge base for refund rules and cite the sources.`
- `Use ask_knowledge_base to answer based only on cited enterprise documents.`

## 2. Cherry Studio

Cherry Studio documents show the basic fields for manual MCP server setup are:

- Name
- Type: `STDIO`
- Command
- Arguments

Recommended configuration:

- Name: `ai-kb`
- Type: `STDIO`
- Command:

```text
E:\project\study\all-project\backend\ai-kb-service\.venv\Scripts\python.exe
```

- Arguments:

```text
-m
app.mcp_server
```

- Environment Variables:

```text
PYTHONPATH=E:\project\study\all-project\backend\ai-kb-service
MCP_TRANSPORT=stdio
LLM_API_KEY=YOUR_API_KEY
LLM_BASE_URL=https://api.openai.com/v1
LLM_MODEL=gpt-4.1-mini
```

If Cherry Studio asks for a single JSON-style import in a future version, you can adapt this shape:

```json
{
  "name": "ai-kb",
  "type": "stdio",
  "command": "E:\\project\\study\\all-project\\backend\\ai-kb-service\\.venv\\Scripts\\python.exe",
  "args": ["-m", "app.mcp_server"],
  "env": {
    "PYTHONPATH": "E:\\project\\study\\all-project\\backend\\ai-kb-service",
    "MCP_TRANSPORT": "stdio",
    "LLM_API_KEY": "YOUR_API_KEY",
    "LLM_BASE_URL": "https://api.openai.com/v1",
    "LLM_MODEL": "gpt-4.1-mini"
  }
}
```

## 3. Generic MCP Client (STDIO)

Many local MCP clients accept a command, args, and env tuple. Use:

```json
{
  "mcpServers": {
    "ai-kb": {
      "type": "stdio",
      "command": "E:\\project\\study\\all-project\\backend\\ai-kb-service\\.venv\\Scripts\\python.exe",
      "args": ["-m", "app.mcp_server"],
      "env": {
        "PYTHONPATH": "E:\\project\\study\\all-project\\backend\\ai-kb-service",
        "MCP_TRANSPORT": "stdio",
        "LLM_API_KEY": "YOUR_API_KEY",
        "LLM_BASE_URL": "https://api.openai.com/v1",
        "LLM_MODEL": "gpt-4.1-mini"
      }
    }
  }
}
```

## 4. Generic MCP Client (HTTP / Remote)

Start the MCP server in streamable HTTP mode:

```powershell
$env:PYTHONPATH="E:\project\study\all-project\backend\ai-kb-service"
$env:MCP_TRANSPORT="streamable-http"
$env:MCP_PORT="8765"
$env:LLM_API_KEY="YOUR_API_KEY"
$env:LLM_BASE_URL="https://api.openai.com/v1"
$env:LLM_MODEL="gpt-4.1-mini"
.\.venv\Scripts\python.exe -m app.mcp_server
```

Then connect a generic HTTP-capable MCP client to:

```text
http://127.0.0.1:8765/mcp
```

## 5. Suggested Runtime Variables

These variables are useful when you want the MCP server to produce real LLM-generated answers instead of retrieval-only fallbacks:

```text
LLM_API_KEY=YOUR_API_KEY
LLM_BASE_URL=https://api.openai.com/v1
LLM_MODEL=gpt-4.1-mini
LLM_TEMPERATURE=0.2
LLM_TIMEOUT_SECONDS=30
```

## 6. Suggested First Tool Calls

Once connected, try these in your MCP client:

- `list_spaces(username="admin")`
- `list_documents(space_id=1, username="admin")`
- `search_knowledge(space_id=1, query="客户支持 SOP 是什么？", top_k=5, username="admin")`
- `ask_knowledge_base(space_id=1, question="总结一下客户支持流程，并附上来源。", username="admin")`
