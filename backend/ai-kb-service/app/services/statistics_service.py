from sqlalchemy import func, select
from sqlalchemy.orm import Session

from app.models.entities import Conversation, Document, KnowledgeSpaceMember


class StatisticsService:
    def overview(self, db: Session, user_id: int) -> dict[str, int]:
        allowed_spaces = (
            select(KnowledgeSpaceMember.space_id).where(KnowledgeSpaceMember.user_id == user_id).subquery()
        )
        return {
            "space_count": db.scalar(select(func.count()).select_from(allowed_spaces)) or 0,
            "document_count": db.scalar(
                select(func.count(Document.id)).where(Document.space_id.in_(select(allowed_spaces.c.space_id)))
            )
            or 0,
            "conversation_count": db.scalar(
                select(func.count(Conversation.id)).where(Conversation.user_id == user_id)
            )
            or 0,
        }


statistics_service = StatisticsService()
