from sqlalchemy import func, select
from sqlalchemy.orm import Session

from app.models.entities import Document


class DocumentRepository:
    def count_by_space(self, db: Session, space_id: int) -> int:
        return db.scalar(select(func.count(Document.id)).where(Document.space_id == space_id)) or 0


document_repository = DocumentRepository()
