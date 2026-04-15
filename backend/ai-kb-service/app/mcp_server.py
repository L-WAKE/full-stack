import json
import os
from contextlib import contextmanager
from typing import Any

from mcp.server.fastmcp import FastMCP
from mcp.server.fastmcp.prompts.base import AssistantMessage, UserMessage
from sqlalchemy import select
from sqlalchemy.orm import Session

from app.db.init_db import create_schema
from app.db.session import SessionLocal
from app.models.entities import User
from app.services.document.service import document_service
from app.services.llm import LLMConfigurationError, llm_service
from app.services.retrieval.service import retrieval_service
from app.services.settings_service import settings_service
from app.services.space_service import space_service
from app.services.statistics_service import statistics_service
from app.schemas.retrieval import RetrievalRequest


@contextmanager
def db_session() -> Session:
    db = SessionLocal()
    try:
        yield db
    finally:
        db.close()


def _json(data: Any) -> str:
    if hasattr(data, "model_dump"):
        data = data.model_dump()
    return json.dumps(data, ensure_ascii=False, indent=2, default=str)


def _get_user(db: Session, username: str) -> User:
    user = db.scalar(select(User).where(User.username == username))
    if not user:
        raise ValueError(f"User '{username}' does not exist in the knowledge base.")
    return user


def _build_grounded_answer(space_id: int, question: str, username: str, top_k: int | None = None) -> dict[str, Any]:
    with db_session() as db:
        user = _get_user(db, username)
        strategy = settings_service.get_retrieval_strategy()
        retrieval = retrieval_service.search(
            db,
            RetrievalRequest(
                space_id=space_id,
                query=question,
                top_k=top_k or strategy.top_k,
            ),
            user.id,
        )
        citations = [chunk.model_dump() for chunk in retrieval.chunks]
        llm_status = "not_configured"
        model = None
        if citations:
            try:
                llm_result = llm_service.synthesize_grounded_answer(
                    question=question,
                    retrieval_context=retrieval.context,
                    citations=citations,
                    answer_language=strategy.answer_language,
                    citation_required=strategy.citation_required,
                    guardrail_enabled=strategy.guardrail_enabled,
                )
                answer = llm_result["answer"]
                model = llm_result["model"]
                llm_status = "generated"
            except LLMConfigurationError as exc:
                primary = citations[0]
                section = f" / {primary['section_path']}" if primary.get("section_path") else ""
                answer = (
                    f"Question matched {len(citations)} knowledge chunks in space #{space_id}. "
                    f"Primary source is '{primary['document_name']}'"
                    f"{section}. "
                    f"LLM generation is not configured yet: {exc}"
                )
            except Exception as exc:
                primary = citations[0]
                section = f" / {primary['section_path']}" if primary.get("section_path") else ""
                answer = (
                    f"Question matched {len(citations)} knowledge chunks in space #{space_id}. "
                    f"Primary source is '{primary['document_name']}'"
                    f"{section}. "
                    f"LLM generation failed, so this response is a grounded retrieval fallback. Details: {exc}"
                )
                llm_status = "failed"
        else:
            answer = (
                "No relevant chunks were found in the current knowledge space. "
                "Upload and parse more documents, or broaden the retrieval scope."
            )
        return {
            "question": question,
            "space_id": space_id,
            "username": username,
            "answer": answer,
            "llm_status": llm_status,
            "model": model,
            "retrieval_mode": retrieval.retrieval_mode,
            "context": retrieval.context,
            "citations": citations,
        }


server = FastMCP(
    name="ai-kb-mcp",
    instructions=(
        "Expose enterprise knowledge-base capabilities as MCP tools and resources. "
        "Use the provided tools to inspect spaces, documents, chunks, retrieval results, and grounded answers."
    ),
    host=os.getenv("MCP_HOST", "127.0.0.1"),
    port=int(os.getenv("MCP_PORT", "8765")),
    log_level=os.getenv("MCP_LOG_LEVEL", "INFO"),
)


@server.tool(
    name="list_spaces",
    description="List knowledge spaces that the specified user can access.",
)
def list_spaces(username: str = "admin") -> list[dict[str, Any]]:
    with db_session() as db:
        user = _get_user(db, username)
        spaces = space_service.list_spaces_for_user(db, user.id)
        return [space.model_dump() for space in spaces]


@server.tool(
    name="get_space_detail",
    description="Get one knowledge space with member, document, and description details.",
)
def get_space_detail(space_id: int, username: str = "admin") -> dict[str, Any]:
    with db_session() as db:
        user = _get_user(db, username)
        detail = space_service.get_space(db, space_id, user.id)
        if not detail:
            raise ValueError(f"Space #{space_id} is not accessible for user '{username}'.")
        return detail.model_dump()


@server.tool(
    name="list_documents",
    description="List documents available in a knowledge space for the specified user.",
)
def list_documents(space_id: int, username: str = "admin") -> list[dict[str, Any]]:
    with db_session() as db:
        user = _get_user(db, username)
        documents = document_service.list_documents(db, user.id, space_id)
        return [doc.model_dump() for doc in documents]


@server.tool(
    name="get_document_chunks",
    description="Get the chunk breakdown for one document, including section path, page number, and content preview.",
)
def get_document_chunks(document_id: int, username: str = "admin") -> list[dict[str, Any]]:
    with db_session() as db:
        user = _get_user(db, username)
        chunks = document_service.list_chunks(db, document_id, user.id)
        if chunks is None:
            raise ValueError(f"Document #{document_id} is not accessible for user '{username}'.")
        return chunks


@server.tool(
    name="search_knowledge",
    description="Run semantic retrieval in a knowledge space and return matched chunks with metadata.",
)
def search_knowledge(space_id: int, query: str, top_k: int = 5, username: str = "admin") -> dict[str, Any]:
    with db_session() as db:
        user = _get_user(db, username)
        result = retrieval_service.search(
            db,
            RetrievalRequest(space_id=space_id, query=query, top_k=top_k),
            user.id,
        )
        return result.model_dump()


@server.tool(
    name="ask_knowledge_base",
    description="Return a grounded answer draft plus citations for a knowledge-base question.",
)
def ask_knowledge_base(space_id: int, question: str, username: str = "admin", top_k: int | None = None) -> dict[str, Any]:
    return _build_grounded_answer(space_id=space_id, question=question, username=username, top_k=top_k)


@server.tool(
    name="get_kb_overview",
    description="Return operational overview metrics for the current user's accessible knowledge spaces.",
)
def get_kb_overview(username: str = "admin") -> dict[str, Any]:
    with db_session() as db:
        user = _get_user(db, username)
        return statistics_service.overview(db, user.id)


@server.resource(
    "kb://spaces/{username}",
    name="knowledge-spaces",
    description="Readable resource that returns all accessible knowledge spaces for a user.",
    mime_type="application/json",
)
def spaces_resource(username: str) -> str:
    return _json(list_spaces(username=username))


@server.resource(
    "kb://spaces/{username}/{space_id}",
    name="knowledge-space-detail",
    description="Readable resource for one knowledge space and its member/document summary.",
    mime_type="application/json",
)
def space_detail_resource(username: str, space_id: str) -> str:
    return _json(get_space_detail(space_id=int(space_id), username=username))


@server.resource(
    "kb://documents/{username}/{space_id}",
    name="knowledge-space-documents",
    description="Readable resource returning all visible documents in a knowledge space.",
    mime_type="application/json",
)
def documents_resource(username: str, space_id: str) -> str:
    return _json(list_documents(space_id=int(space_id), username=username))


@server.resource(
    "kb://overview/{username}",
    name="knowledge-base-overview",
    description="Readable operational overview for the given user.",
    mime_type="application/json",
)
def overview_resource(username: str) -> str:
    return _json(get_kb_overview(username=username))


@server.resource(
    "kb://strategy/retrieval",
    name="retrieval-strategy",
    description="Readable retrieval strategy configuration currently used by the knowledge base.",
    mime_type="application/json",
)
def retrieval_strategy_resource() -> str:
    return _json(settings_service.get_retrieval_strategy())


@server.prompt(
    name="customer_support_grounded_answer",
    description="Prompt template for answering a customer-support question with grounded knowledge-base citations.",
)
def customer_support_grounded_answer(question: str, space_id: int, username: str = "admin") -> list[Any]:
    grounded = _build_grounded_answer(space_id=space_id, question=question, username=username)
    return [
        AssistantMessage(
            "You are a customer-support knowledge assistant. Answer only with grounded enterprise knowledge and "
            "explicitly cite the returned sources."
        ),
        UserMessage(
            f"Knowledge space: {space_id}\n"
            f"User: {username}\n"
            f"Question: {question}\n\n"
            f"Grounded retrieval package:\n{_json(grounded)}"
        ),
    ]


@server.prompt(
    name="implementation_handoff_review",
    description="Prompt template for reviewing delivery or implementation handoff notes using the knowledge base.",
)
def implementation_handoff_review(project_scope: str, space_id: int, username: str = "admin") -> list[Any]:
    return [
        AssistantMessage(
            "You are an implementation delivery copilot. Use the knowledge base to review risks, missing SOPs, "
            "and operational handoff steps."
        ),
        UserMessage(
            f"Please review the following project handoff scope for knowledge gaps.\n"
            f"Knowledge space: {space_id}\n"
            f"User: {username}\n"
            f"Project scope:\n{project_scope}\n\n"
            "Use MCP tools such as list_documents, search_knowledge, and get_document_chunks before finalizing advice."
        ),
    ]


def main() -> None:
    create_schema()
    transport = os.getenv("MCP_TRANSPORT", "stdio")
    server.run(transport=transport)


if __name__ == "__main__":
    main()
