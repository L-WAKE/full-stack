from datetime import datetime

from sqlalchemy import DateTime, Float, ForeignKey, Integer, String, Text, UniqueConstraint
from sqlalchemy.orm import Mapped, mapped_column

from app.db.base import Base


class User(Base):
    __tablename__ = "user"

    id: Mapped[int] = mapped_column(Integer, primary_key=True)
    username: Mapped[str] = mapped_column(String(64), unique=True, index=True)
    display_name: Mapped[str] = mapped_column(String(128))
    password_hash: Mapped[str] = mapped_column(String(255), default="")
    role: Mapped[str] = mapped_column(String(32), default="editor")
    created_at: Mapped[datetime] = mapped_column(DateTime, default=datetime.utcnow)


class KnowledgeSpace(Base):
    __tablename__ = "knowledge_space"

    id: Mapped[int] = mapped_column(Integer, primary_key=True)
    name: Mapped[str] = mapped_column(String(128))
    description: Mapped[str] = mapped_column(Text, default="")
    created_by: Mapped[int] = mapped_column(Integer)
    created_at: Mapped[datetime] = mapped_column(DateTime, default=datetime.utcnow)


class KnowledgeSpaceMember(Base):
    __tablename__ = "knowledge_space_member"
    __table_args__ = (UniqueConstraint("space_id", "user_id", name="uq_space_member"),)

    id: Mapped[int] = mapped_column(Integer, primary_key=True)
    space_id: Mapped[int] = mapped_column(ForeignKey("knowledge_space.id"), index=True)
    user_id: Mapped[int] = mapped_column(ForeignKey("user.id"), index=True)
    role: Mapped[str] = mapped_column(String(32), default="member")
    created_at: Mapped[datetime] = mapped_column(DateTime, default=datetime.utcnow)


class Document(Base):
    __tablename__ = "document"

    id: Mapped[int] = mapped_column(Integer, primary_key=True)
    space_id: Mapped[int] = mapped_column(ForeignKey("knowledge_space.id"))
    file_name: Mapped[str] = mapped_column(String(255))
    file_type: Mapped[str] = mapped_column(String(32))
    file_size: Mapped[int] = mapped_column(Integer)
    storage_path: Mapped[str] = mapped_column(String(255))
    visibility_scope: Mapped[str] = mapped_column(String(32), default="SPACE")
    parse_status: Mapped[str] = mapped_column(String(32), default="PENDING")
    chunk_count: Mapped[int] = mapped_column(Integer, default=0)
    failure_reason: Mapped[str | None] = mapped_column(Text, nullable=True)
    parse_requested_at: Mapped[datetime | None] = mapped_column(DateTime, nullable=True)
    parse_started_at: Mapped[datetime | None] = mapped_column(DateTime, nullable=True)
    parse_completed_at: Mapped[datetime | None] = mapped_column(DateTime, nullable=True)
    created_by: Mapped[int] = mapped_column(Integer)
    created_at: Mapped[datetime] = mapped_column(DateTime, default=datetime.utcnow)


class DocumentChunk(Base):
    __tablename__ = "document_chunk"

    id: Mapped[int] = mapped_column(Integer, primary_key=True)
    document_id: Mapped[int] = mapped_column(ForeignKey("document.id"), index=True)
    space_id: Mapped[int] = mapped_column(ForeignKey("knowledge_space.id"), index=True)
    chunk_index: Mapped[int] = mapped_column(Integer)
    content: Mapped[str] = mapped_column(Text)
    content_preview: Mapped[str] = mapped_column(Text)
    page_no: Mapped[int | None] = mapped_column(Integer, nullable=True)
    section_path: Mapped[str] = mapped_column(String(255), default="")
    token_estimate: Mapped[int] = mapped_column(Integer, default=0)
    created_at: Mapped[datetime] = mapped_column(DateTime, default=datetime.utcnow)


class DocumentChunkEmbedding(Base):
    __tablename__ = "document_chunk_embedding"

    id: Mapped[int] = mapped_column(Integer, primary_key=True)
    chunk_id: Mapped[int] = mapped_column(ForeignKey("document_chunk.id"), index=True)
    vector_point_id: Mapped[str] = mapped_column(String(128), unique=True)
    provider: Mapped[str] = mapped_column(String(64), default="openai")
    model_name: Mapped[str] = mapped_column(String(128), default="")
    created_at: Mapped[datetime] = mapped_column(DateTime, default=datetime.utcnow)


class Conversation(Base):
    __tablename__ = "conversation"

    id: Mapped[int] = mapped_column(Integer, primary_key=True)
    space_id: Mapped[int] = mapped_column(ForeignKey("knowledge_space.id"))
    user_id: Mapped[int] = mapped_column(Integer)
    title: Mapped[str] = mapped_column(String(255))
    created_at: Mapped[datetime] = mapped_column(DateTime, default=datetime.utcnow)
    updated_at: Mapped[datetime] = mapped_column(DateTime, default=datetime.utcnow)


class ConversationMessage(Base):
    __tablename__ = "conversation_message"

    id: Mapped[int] = mapped_column(Integer, primary_key=True)
    conversation_id: Mapped[int] = mapped_column(ForeignKey("conversation.id"), index=True)
    role: Mapped[str] = mapped_column(String(32))
    content: Mapped[str] = mapped_column(Text)
    model_name: Mapped[str | None] = mapped_column(String(128), nullable=True)
    prompt_tokens: Mapped[int] = mapped_column(Integer, default=0)
    completion_tokens: Mapped[int] = mapped_column(Integer, default=0)
    created_at: Mapped[datetime] = mapped_column(DateTime, default=datetime.utcnow)


class ConversationCitation(Base):
    __tablename__ = "conversation_citation"

    id: Mapped[int] = mapped_column(Integer, primary_key=True)
    message_id: Mapped[int] = mapped_column(ForeignKey("conversation_message.id"), index=True)
    document_id: Mapped[int] = mapped_column(ForeignKey("document.id"), index=True)
    chunk_id: Mapped[int] = mapped_column(ForeignKey("document_chunk.id"), index=True)
    score: Mapped[float] = mapped_column(Float, default=0)
    page_no: Mapped[int | None] = mapped_column(Integer, nullable=True)
    snippet: Mapped[str] = mapped_column(Text)
    created_at: Mapped[datetime] = mapped_column(DateTime, default=datetime.utcnow)
