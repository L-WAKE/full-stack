from sqlalchemy import select
from sqlalchemy.orm import Session

from app.models.entities import Document, DocumentChunk, KnowledgeSpaceMember, User
from app.schemas.retrieval import RetrievalChunk, RetrievalRequest, RetrievalResponse
from app.services.embedding.service import embedding_service


class RetrievalService:
    def search(self, db: Session, payload: RetrievalRequest, user_id: int) -> RetrievalResponse:
        allowed = db.scalar(
            select(KnowledgeSpaceMember.id).where(
                KnowledgeSpaceMember.space_id == payload.space_id,
                KnowledgeSpaceMember.user_id == user_id,
            )
        )
        if not allowed:
            return RetrievalResponse(query=payload.query, context="", chunks=[], retrieval_mode="forbidden")

        chunks: list[RetrievalChunk] = []
        retrieval_mode = "vector"
        try:
            vector = embedding_service.embed_text(payload.query)
            search_result = embedding_service.search(vector, payload.top_k * 2)
            for item in search_result:
                point_payload = item.payload or {}
                if point_payload.get("space_id") != payload.space_id:
                    continue
                if point_payload.get("visibility_scope") == "PRIVATE" and point_payload.get("created_by") != user_id:
                    continue
                chunks.append(
                    RetrievalChunk(
                        chunk_id=point_payload.get("chunk_id"),
                        document_id=point_payload.get("document_id"),
                        document_name=point_payload.get("document_name", ""),
                        visibility_scope=point_payload.get("visibility_scope", "SPACE"),
                        created_by=point_payload.get("created_by"),
                        created_by_name=point_payload.get("created_by_name"),
                        created_by_username=point_payload.get("created_by_username"),
                        section_path=point_payload.get("section_path"),
                        score=float(item.score),
                        rerank_score=float(item.score),
                        content_preview=point_payload.get("content_preview", ""),
                        page_no=point_payload.get("page_no"),
                    )
                )
                if len(chunks) >= payload.top_k:
                    break
        except Exception:
            retrieval_mode = "database-fallback"
            stmt = (
                select(DocumentChunk, Document, User)
                .join(Document, Document.id == DocumentChunk.document_id)
                .join(User, User.id == Document.created_by)
                .where(DocumentChunk.space_id == payload.space_id)
                .order_by(DocumentChunk.chunk_index.asc())
            )
            rows = db.execute(stmt).all()[: payload.top_k]
            chunks = [
                RetrievalChunk(
                    chunk_id=chunk.id,
                    document_id=document.id,
                    document_name=document.file_name,
                    visibility_scope=document.visibility_scope,
                    created_by=document.created_by,
                    created_by_name=user.display_name,
                    created_by_username=user.username,
                    section_path=chunk.section_path,
                    score=0.82 if payload.query else 0.0,
                    rerank_score=0.79 if payload.query else 0.0,
                    content_preview=chunk.content_preview,
                    page_no=chunk.page_no,
                )
                for chunk, document, user in rows
                if document.visibility_scope != "PRIVATE" or document.created_by == user_id
            ]

        return RetrievalResponse(
            query=payload.query,
            context="\n".join(item.content_preview for item in chunks),
            chunks=chunks,
            retrieval_mode=retrieval_mode,
        )


retrieval_service = RetrievalService()
