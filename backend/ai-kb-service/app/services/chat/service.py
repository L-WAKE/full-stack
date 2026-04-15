from datetime import datetime

from sqlalchemy import select
from sqlalchemy.orm import Session

from app.models.entities import Conversation, ConversationMessage, KnowledgeSpaceMember
from app.schemas.auth import UserProfile
from app.schemas.chat import ConversationCreate, ConversationDetail, MessageCreate, MessageRecord
from app.schemas.retrieval import RetrievalRequest
from app.services.retrieval.service import retrieval_service
from app.services.settings_service import settings_service


class ChatService:
    def create_conversation(
        self,
        db: Session,
        payload: ConversationCreate,
        current_user: UserProfile,
    ) -> ConversationDetail | None:
        if not self._has_space_access(db, payload.space_id, current_user.id):
            return None
        conversation = Conversation(
            space_id=payload.space_id,
            user_id=current_user.id,
            title=payload.title,
        )
        db.add(conversation)
        db.commit()
        db.refresh(conversation)
        return self._to_conversation(conversation)

    def list_conversations(self, db: Session, user_id: int, space_id: int | None) -> list[ConversationDetail]:
        stmt = select(Conversation).where(Conversation.user_id == user_id).order_by(Conversation.updated_at.desc())
        if space_id:
            stmt = stmt.where(Conversation.space_id == space_id)
        return [self._to_conversation(item) for item in db.scalars(stmt).all()]

    def list_messages(self, db: Session, conversation_id: int, user_id: int) -> list[MessageRecord] | None:
        if not self.can_access(db, conversation_id, user_id):
            return None
        stmt = (
            select(ConversationMessage)
            .where(ConversationMessage.conversation_id == conversation_id)
            .order_by(ConversationMessage.created_at.asc())
        )
        return [self._to_message(item) for item in db.scalars(stmt).all()]

    def add_user_message(
        self,
        db: Session,
        conversation_id: int,
        payload: MessageCreate,
        user_id: int,
    ) -> MessageRecord | None:
        conversation = self._get_conversation(db, conversation_id, user_id)
        if not conversation:
            return None
        message = ConversationMessage(conversation_id=conversation_id, role="user", content=payload.content)
        db.add(message)
        conversation.updated_at = datetime.utcnow()
        db.commit()
        db.refresh(message)
        return self._to_message(message)

    def can_access(self, db: Session, conversation_id: int, user_id: int) -> bool:
        return self._get_conversation(db, conversation_id, user_id) is not None

    def stream_answer(self, db: Session, conversation_id: int) -> list[dict]:
        conversation = db.get(Conversation, conversation_id)
        latest_message = db.scalar(
            select(ConversationMessage)
            .where(ConversationMessage.conversation_id == conversation_id, ConversationMessage.role == "user")
            .order_by(ConversationMessage.created_at.desc())
        )
        strategy = settings_service.get_retrieval_strategy()
        retrieval = retrieval_service.search(
            db,
            RetrievalRequest(
                space_id=conversation.space_id,
                query=latest_message.content if latest_message else "",
                top_k=strategy.top_k,
            ),
            conversation.user_id,
        )

        citation_items = [
            {
                "document_name": item.document_name,
                "page_no": item.page_no,
                "snippet": item.content_preview,
                "visibility_scope": item.visibility_scope,
                "created_by_name": item.created_by_name,
                "created_by_username": item.created_by_username,
                "section_path": item.section_path,
            }
            for item in retrieval.chunks
        ]

        if retrieval.chunks:
            head = retrieval.chunks[0]
            citation_hint = "本次回答已附带引用来源，适合企业内部 SOP、FAQ 与政策问答场景。"
            if not strategy.citation_required:
                citation_hint = "当前策略允许摘要式回答，但系统仍保留了候选片段，便于人工复核。"
            answer = (
                f"已从当前知识空间检索到 {len(retrieval.chunks)} 条高相关片段，"
                f"检索模式为 {retrieval.retrieval_mode}。"
                f"优先参考文档《{head.document_name}》"
                f"{f' 的 {head.section_path}' if head.section_path else ''}。"
                f"该片段上传人是 {head.created_by_name or head.created_by_username or '未知成员'}。"
                f"关键内容摘要：{head.content_preview}。"
                f"{citation_hint}"
            )
        else:
            answer = (
                "当前知识空间下没有命中可用片段。"
                "请先上传并解析 PDF 或 DOCX 文档，"
                "或检查当前空间、文档可见范围与检索策略是否配置正确。"
            )

        return [
            {"type": "token", "delta": answer},
            {"type": "citation", "items": citation_items},
            {"type": "done"},
        ]

    def _get_conversation(self, db: Session, conversation_id: int, user_id: int) -> Conversation | None:
        stmt = (
            select(Conversation)
            .join(KnowledgeSpaceMember, KnowledgeSpaceMember.space_id == Conversation.space_id)
            .where(
                Conversation.id == conversation_id,
                Conversation.user_id == user_id,
                KnowledgeSpaceMember.user_id == user_id,
            )
        )
        return db.scalar(stmt)

    def _has_space_access(self, db: Session, space_id: int, user_id: int) -> bool:
        return db.scalar(
            select(KnowledgeSpaceMember.id).where(
                KnowledgeSpaceMember.space_id == space_id,
                KnowledgeSpaceMember.user_id == user_id,
            )
        ) is not None

    def _to_conversation(self, item: Conversation) -> ConversationDetail:
        return ConversationDetail(
            id=item.id,
            space_id=item.space_id,
            user_id=item.user_id,
            title=item.title,
            created_at=item.created_at,
            updated_at=item.updated_at,
        )

    def _to_message(self, item: ConversationMessage) -> MessageRecord:
        return MessageRecord(
            id=item.id,
            conversation_id=item.conversation_id,
            role=item.role,
            content=item.content,
            created_at=item.created_at,
        )


chat_service = ChatService()
