from sqlalchemy import case, func, select
from sqlalchemy.orm import Session

from app.models.entities import Conversation, Document, DocumentChunk, KnowledgeSpaceMember
from app.services.embedding.service import embedding_service


class StatisticsService:
    def overview(self, db: Session, user_id: int) -> dict[str, int | str | float]:
        allowed_spaces = (
            select(KnowledgeSpaceMember.space_id).where(KnowledgeSpaceMember.user_id == user_id).subquery()
        )
        document_stmt = select(
            func.count(Document.id).label("document_count"),
            func.coalesce(func.sum(case((Document.parse_status == "READY", 1), else_=0)), 0).label("ready_count"),
            func.coalesce(func.sum(case((Document.parse_status == "PROCESSING", 1), else_=0)), 0).label(
                "processing_count"
            ),
            func.coalesce(func.sum(case((Document.parse_status == "FAILED", 1), else_=0)), 0).label("failed_count"),
            func.coalesce(func.sum(case((Document.visibility_scope == "PRIVATE", 1), else_=0)), 0).label(
                "private_count"
            ),
            func.coalesce(func.sum(Document.chunk_count), 0).label("chunk_count"),
        ).where(Document.space_id.in_(select(allowed_spaces.c.space_id)))
        document_stats = db.execute(document_stmt).one()
        conversation_count = db.scalar(select(func.count(Conversation.id)).where(Conversation.user_id == user_id)) or 0
        avg_chunks = round(
            float(document_stats.chunk_count or 0) / float(document_stats.document_count or 1),
            1,
        ) if document_stats.document_count else 0.0

        try:
            vector_store_mode = embedding_service.current_mode()
        except Exception:
            vector_store_mode = "unavailable"

        return {
            "space_count": db.scalar(select(func.count()).select_from(allowed_spaces)) or 0,
            "document_count": document_stats.document_count or 0,
            "conversation_count": conversation_count,
            "ready_count": document_stats.ready_count or 0,
            "processing_count": document_stats.processing_count or 0,
            "failed_count": document_stats.failed_count or 0,
            "private_count": document_stats.private_count or 0,
            "shared_count": (document_stats.document_count or 0) - (document_stats.private_count or 0),
            "chunk_count": document_stats.chunk_count or 0,
            "avg_chunks_per_document": avg_chunks,
            "vector_store_mode": vector_store_mode,
            "coverage_ratio": round(
                float(document_stats.ready_count or 0) / float(document_stats.document_count or 1),
                2,
            ) if document_stats.document_count else 0.0,
        }

    def recent_activity(self, db: Session, user_id: int) -> dict[str, list[dict]]:
        allowed_spaces = (
            select(KnowledgeSpaceMember.space_id).where(KnowledgeSpaceMember.user_id == user_id).subquery()
        )
        documents = db.execute(
            select(Document)
            .where(Document.space_id.in_(select(allowed_spaces.c.space_id)))
            .order_by(Document.created_at.desc())
            .limit(5)
        ).scalars().all()
        conversations = db.execute(
            select(Conversation).where(Conversation.user_id == user_id).order_by(Conversation.updated_at.desc()).limit(5)
        ).scalars().all()
        chunks = db.execute(
            select(DocumentChunk)
            .where(DocumentChunk.space_id.in_(select(allowed_spaces.c.space_id)))
            .order_by(DocumentChunk.created_at.desc())
            .limit(5)
        ).scalars().all()
        return {
            "documents": [
                {
                    "id": item.id,
                    "file_name": item.file_name,
                    "parse_status": item.parse_status,
                    "visibility_scope": item.visibility_scope,
                    "chunk_count": item.chunk_count,
                    "created_at": item.created_at.isoformat(),
                }
                for item in documents
            ],
            "conversations": [
                {
                    "id": item.id,
                    "title": item.title,
                    "space_id": item.space_id,
                    "updated_at": item.updated_at.isoformat(),
                }
                for item in conversations
            ],
            "chunks": [
                {
                    "id": item.id,
                    "document_id": item.document_id,
                    "section_path": item.section_path,
                    "page_no": item.page_no,
                    "token_estimate": item.token_estimate,
                }
                for item in chunks
            ],
        }


statistics_service = StatisticsService()
