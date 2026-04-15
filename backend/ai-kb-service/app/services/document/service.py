from datetime import datetime
from pathlib import Path

from fastapi import UploadFile
from sqlalchemy import delete, select
from sqlalchemy.orm import Session

from app.core.config import get_settings
from app.db.session import SessionLocal
from app.models.entities import (
    ConversationCitation,
    Document,
    DocumentChunk,
    DocumentChunkEmbedding,
    KnowledgeSpaceMember,
    User,
)
from app.schemas.auth import UserProfile
from app.schemas.document import DocumentDetail, DocumentSummary
from app.services.chunk.service import chunk_service
from app.services.embedding.service import embedding_service
from app.services.parser.service import parser_service
from app.services.space_service import space_service

settings = get_settings()


class DocumentService:
    def can_manage_document(self, db: Session, document: Document, user: UserProfile) -> bool:
        return (
            document.created_by == user.id
            or space_service.can_manage_space(db, document.space_id, user.id, user.role)
        )

    def list_documents(self, db: Session, user_id: int, space_id: int | None) -> list[DocumentSummary]:
        stmt = (
            select(Document, User)
            .join(KnowledgeSpaceMember, KnowledgeSpaceMember.space_id == Document.space_id)
            .join(User, User.id == Document.created_by)
            .where(KnowledgeSpaceMember.user_id == user_id)
            .order_by(Document.created_at.desc())
        )
        if space_id:
            stmt = stmt.where(Document.space_id == space_id)

        result = []
        for item, uploader in db.execute(stmt).all():
            if item.visibility_scope == "PRIVATE" and item.created_by != user_id:
                continue
            result.append(self._to_summary(item, uploader))
        return result

    async def upload_document(
        self,
        db: Session,
        space_id: int,
        file: UploadFile,
        visibility_scope: str,
        current_user: UserProfile,
    ) -> DocumentDetail | None:
        if file.filename is None:
            return None

        safe_name = Path(file.filename).name
        extension = safe_name.split(".")[-1].lower()
        if extension not in {"pdf", "docx"}:
            return None
        if visibility_scope not in {"SPACE", "PRIVATE"}:
            return None
        if not self._has_access(db, space_id, current_user.id):
            return None

        upload_root = Path(settings.upload_dir)
        upload_root.mkdir(parents=True, exist_ok=True)
        content = await file.read()

        document = Document(
            space_id=space_id,
            file_name=safe_name,
            file_type=extension,
            file_size=len(content),
            storage_path="",
            visibility_scope=visibility_scope,
            parse_status="PENDING",
            chunk_count=0,
            parse_requested_at=None,
            parse_started_at=None,
            parse_completed_at=None,
            created_by=current_user.id,
        )
        db.add(document)
        db.flush()

        file_path = upload_root / f"{document.id}-{safe_name}"
        file_path.write_bytes(content)
        document.storage_path = str(file_path).replace("\\", "/")
        db.commit()
        db.refresh(document)
        return self._to_detail(document, self._get_uploader(db, document.created_by))

    def get_document(self, db: Session, document_id: int, user_id: int) -> DocumentDetail | None:
        document = self._get_viewable_model(db, document_id, user_id)
        if not document:
            return None
        return self._to_detail(document, self._get_uploader(db, document.created_by))

    def delete_document(self, db: Session, document_id: int, user_id: int) -> bool:
        document = self._get_membership_model(db, document_id, user_id)
        if not document:
            return False
        chunk_ids = select(DocumentChunk.id).where(DocumentChunk.document_id == document_id)
        try:
            embedding_service.delete_document_points(document_id)
        except Exception:
            pass
        db.execute(delete(ConversationCitation).where(ConversationCitation.chunk_id.in_(chunk_ids)))
        db.execute(delete(ConversationCitation).where(ConversationCitation.document_id == document_id))
        db.execute(delete(DocumentChunkEmbedding).where(DocumentChunkEmbedding.chunk_id.in_(chunk_ids)))
        db.execute(delete(DocumentChunk).where(DocumentChunk.document_id == document_id))
        db.delete(document)
        db.commit()
        return True

    def trigger_parse(self, db: Session, document_id: int, user_id: int) -> DocumentDetail | None:
        document = self._get_membership_model(db, document_id, user_id)
        if not document:
            return None
        if document.parse_status == "PROCESSING":
            return self._to_detail(document, self._get_uploader(db, document.created_by))

        document.parse_status = "PENDING"
        document.failure_reason = None
        document.parse_requested_at = datetime.utcnow()
        document.parse_started_at = None
        document.parse_completed_at = None
        db.commit()
        db.refresh(document)
        return self._to_detail(document, self._get_uploader(db, document.created_by))

    def update_visibility(
        self,
        db: Session,
        document_id: int,
        visibility_scope: str,
        current_user: UserProfile,
    ) -> DocumentDetail | None:
        if visibility_scope not in {"SPACE", "PRIVATE"}:
            return None

        document = self._get_membership_model(db, document_id, current_user.id)
        if not document or not self.can_manage_document(db, document, current_user):
            return None

        document.visibility_scope = visibility_scope
        db.commit()
        db.refresh(document)
        return self._to_detail(document, self._get_uploader(db, document.created_by))

    def process_document(self, document_id: int) -> None:
        db = SessionLocal()
        try:
            document = db.get(Document, document_id)
            if not document:
                return
            uploader = self._get_uploader(db, document.created_by)

            document.parse_status = "PROCESSING"
            document.failure_reason = None
            document.parse_started_at = datetime.utcnow()
            document.parse_completed_at = None
            db.commit()

            sections = parser_service.parse(document.storage_path, document.file_type)
            parsed_chunks = chunk_service.chunk(sections)
            if not parsed_chunks:
                raise ValueError("No parsable text chunks were extracted from the document.")

            db.execute(
                delete(DocumentChunkEmbedding).where(
                    DocumentChunkEmbedding.chunk_id.in_(
                        select(DocumentChunk.id).where(DocumentChunk.document_id == document_id)
                    )
                )
            )
            db.execute(delete(DocumentChunk).where(DocumentChunk.document_id == document_id))
            db.flush()
            try:
                embedding_service.delete_document_points(document_id)
            except Exception:
                pass

            qdrant_points = []
            for item in parsed_chunks:
                chunk = DocumentChunk(
                    document_id=document.id,
                    space_id=document.space_id,
                    chunk_index=item["chunk_index"],
                    content=item["content"],
                    content_preview=item["content_preview"],
                    page_no=item["page_no"],
                    section_path=item["section_path"],
                    token_estimate=item["token_estimate"],
                )
                db.add(chunk)
                db.flush()

                point_id = embedding_service.new_point_id()
                vector = embedding_service.embed_text(chunk.content)
                db.add(
                    DocumentChunkEmbedding(
                        chunk_id=chunk.id,
                        vector_point_id=point_id,
                        provider="local-hash",
                        model_name="hash-embedding-128",
                    )
                )
                qdrant_points.append(
                    {
                        "point_id": point_id,
                        "vector": vector,
                        "payload": {
                            "chunk_id": chunk.id,
                            "document_id": document.id,
                            "space_id": document.space_id,
                            "document_name": document.file_name,
                            "visibility_scope": document.visibility_scope,
                            "created_by": document.created_by,
                            "created_by_name": uploader.display_name if uploader else None,
                            "created_by_username": uploader.username if uploader else None,
                            "page_no": chunk.page_no,
                            "section_path": chunk.section_path,
                            "content_preview": chunk.content_preview,
                        },
                    }
                )

            document.chunk_count = len(parsed_chunks)
            document.parse_status = "READY"
            document.failure_reason = None
            document.parse_completed_at = datetime.utcnow()
            try:
                embedding_service.upsert_chunks(qdrant_points)
            except Exception as vector_exc:
                document.failure_reason = (
                    "Vector index is temporarily unavailable. The document was saved and can still be searched "
                    f"through database fallback mode. Details: {vector_exc}"
                )
            db.commit()
        except Exception as exc:
            db.rollback()
            document = db.get(Document, document_id)
            if document:
                document.parse_status = "FAILED"
                document.failure_reason = str(exc)
                document.parse_completed_at = datetime.utcnow()
                db.commit()
        finally:
            db.close()

    def list_chunks(self, db: Session, document_id: int, user_id: int) -> list[dict] | None:
        document = self._get_viewable_model(db, document_id, user_id)
        if not document:
            return None

        items = db.scalars(
            select(DocumentChunk).where(DocumentChunk.document_id == document_id).order_by(DocumentChunk.chunk_index)
        ).all()
        return [
            {
                "id": item.id,
                "document_id": item.document_id,
                "space_id": item.space_id,
                "chunk_index": item.chunk_index,
                "content": item.content,
                "content_preview": item.content_preview,
                "page_no": item.page_no,
                "section_path": item.section_path,
                "token_estimate": item.token_estimate,
            }
            for item in items
        ]

    def _get_membership_model(self, db: Session, document_id: int, user_id: int) -> Document | None:
        stmt = (
            select(Document)
            .join(KnowledgeSpaceMember, KnowledgeSpaceMember.space_id == Document.space_id)
            .where(Document.id == document_id, KnowledgeSpaceMember.user_id == user_id)
        )
        return db.scalar(stmt)

    def _get_viewable_model(self, db: Session, document_id: int, user_id: int) -> Document | None:
        document = self._get_membership_model(db, document_id, user_id)
        if not document:
            return None
        if document.visibility_scope == "PRIVATE" and document.created_by != user_id:
            return None
        return document

    def _has_access(self, db: Session, space_id: int, user_id: int) -> bool:
        return db.scalar(
            select(KnowledgeSpaceMember.id).where(
                KnowledgeSpaceMember.space_id == space_id,
                KnowledgeSpaceMember.user_id == user_id,
            )
        ) is not None

    def _get_uploader(self, db: Session, user_id: int) -> User | None:
        return db.get(User, user_id)

    def _to_summary(self, item: Document, uploader: User | None = None) -> DocumentSummary:
        return DocumentSummary(
            id=item.id,
            space_id=item.space_id,
            file_name=item.file_name,
            file_type=item.file_type,
            file_size=item.file_size,
            created_by=item.created_by,
            created_by_name=uploader.display_name if uploader else None,
            created_by_username=uploader.username if uploader else None,
            visibility_scope=item.visibility_scope,
            parse_status=item.parse_status,
            chunk_count=item.chunk_count,
            created_at=item.created_at,
        )

    def _to_detail(self, item: Document, uploader: User | None = None) -> DocumentDetail:
        return DocumentDetail(
            id=item.id,
            space_id=item.space_id,
            file_name=item.file_name,
            file_type=item.file_type,
            file_size=item.file_size,
            visibility_scope=item.visibility_scope,
            parse_status=item.parse_status,
            chunk_count=item.chunk_count,
            created_at=item.created_at,
            storage_path=item.storage_path,
            created_by=item.created_by,
            created_by_name=uploader.display_name if uploader else None,
            created_by_username=uploader.username if uploader else None,
            failure_reason=item.failure_reason,
            parse_requested_at=item.parse_requested_at,
            parse_started_at=item.parse_started_at,
            parse_completed_at=item.parse_completed_at,
        )


document_service = DocumentService()
