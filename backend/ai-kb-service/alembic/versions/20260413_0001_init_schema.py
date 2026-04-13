"""init schema"""

from alembic import op
import sqlalchemy as sa


revision = "20260413_0001"
down_revision = None
branch_labels = None
depends_on = None


def upgrade() -> None:
    op.create_table(
        "user",
        sa.Column("id", sa.Integer(), primary_key=True),
        sa.Column("username", sa.String(length=64), nullable=False),
        sa.Column("display_name", sa.String(length=128), nullable=False),
        sa.Column("password_hash", sa.String(length=255), nullable=False),
        sa.Column("role", sa.String(length=32), nullable=False),
        sa.Column("created_at", sa.DateTime(), nullable=False),
    )
    op.create_index("ix_user_username", "user", ["username"], unique=True)

    op.create_table(
        "knowledge_space",
        sa.Column("id", sa.Integer(), primary_key=True),
        sa.Column("name", sa.String(length=128), nullable=False),
        sa.Column("description", sa.Text(), nullable=False),
        sa.Column("created_by", sa.Integer(), nullable=False),
        sa.Column("created_at", sa.DateTime(), nullable=False),
    )

    op.create_table(
        "knowledge_space_member",
        sa.Column("id", sa.Integer(), primary_key=True),
        sa.Column("space_id", sa.Integer(), sa.ForeignKey("knowledge_space.id"), nullable=False),
        sa.Column("user_id", sa.Integer(), sa.ForeignKey("user.id"), nullable=False),
        sa.Column("role", sa.String(length=32), nullable=False),
        sa.Column("created_at", sa.DateTime(), nullable=False),
        sa.UniqueConstraint("space_id", "user_id", name="uq_space_member"),
    )

    op.create_table(
        "document",
        sa.Column("id", sa.Integer(), primary_key=True),
        sa.Column("space_id", sa.Integer(), sa.ForeignKey("knowledge_space.id"), nullable=False),
        sa.Column("file_name", sa.String(length=255), nullable=False),
        sa.Column("file_type", sa.String(length=32), nullable=False),
        sa.Column("file_size", sa.Integer(), nullable=False),
        sa.Column("storage_path", sa.String(length=255), nullable=False),
        sa.Column("visibility_scope", sa.String(length=32), nullable=False),
        sa.Column("parse_status", sa.String(length=32), nullable=False),
        sa.Column("chunk_count", sa.Integer(), nullable=False),
        sa.Column("failure_reason", sa.Text(), nullable=True),
        sa.Column("parse_requested_at", sa.DateTime(), nullable=True),
        sa.Column("parse_started_at", sa.DateTime(), nullable=True),
        sa.Column("parse_completed_at", sa.DateTime(), nullable=True),
        sa.Column("created_by", sa.Integer(), nullable=False),
        sa.Column("created_at", sa.DateTime(), nullable=False),
    )

    op.create_table(
        "document_chunk",
        sa.Column("id", sa.Integer(), primary_key=True),
        sa.Column("document_id", sa.Integer(), sa.ForeignKey("document.id"), nullable=False),
        sa.Column("space_id", sa.Integer(), sa.ForeignKey("knowledge_space.id"), nullable=False),
        sa.Column("chunk_index", sa.Integer(), nullable=False),
        sa.Column("content", sa.Text(), nullable=False),
        sa.Column("content_preview", sa.Text(), nullable=False),
        sa.Column("page_no", sa.Integer(), nullable=True),
        sa.Column("section_path", sa.String(length=255), nullable=False),
        sa.Column("token_estimate", sa.Integer(), nullable=False),
        sa.Column("created_at", sa.DateTime(), nullable=False),
    )

    op.create_table(
        "document_chunk_embedding",
        sa.Column("id", sa.Integer(), primary_key=True),
        sa.Column("chunk_id", sa.Integer(), sa.ForeignKey("document_chunk.id"), nullable=False),
        sa.Column("vector_point_id", sa.String(length=128), nullable=False),
        sa.Column("provider", sa.String(length=64), nullable=False),
        sa.Column("model_name", sa.String(length=128), nullable=False),
        sa.Column("created_at", sa.DateTime(), nullable=False),
        sa.UniqueConstraint("vector_point_id"),
    )

    op.create_table(
        "conversation",
        sa.Column("id", sa.Integer(), primary_key=True),
        sa.Column("space_id", sa.Integer(), sa.ForeignKey("knowledge_space.id"), nullable=False),
        sa.Column("user_id", sa.Integer(), nullable=False),
        sa.Column("title", sa.String(length=255), nullable=False),
        sa.Column("created_at", sa.DateTime(), nullable=False),
        sa.Column("updated_at", sa.DateTime(), nullable=False),
    )

    op.create_table(
        "conversation_message",
        sa.Column("id", sa.Integer(), primary_key=True),
        sa.Column("conversation_id", sa.Integer(), sa.ForeignKey("conversation.id"), nullable=False),
        sa.Column("role", sa.String(length=32), nullable=False),
        sa.Column("content", sa.Text(), nullable=False),
        sa.Column("model_name", sa.String(length=128), nullable=True),
        sa.Column("prompt_tokens", sa.Integer(), nullable=False),
        sa.Column("completion_tokens", sa.Integer(), nullable=False),
        sa.Column("created_at", sa.DateTime(), nullable=False),
    )

    op.create_table(
        "conversation_citation",
        sa.Column("id", sa.Integer(), primary_key=True),
        sa.Column("message_id", sa.Integer(), sa.ForeignKey("conversation_message.id"), nullable=False),
        sa.Column("document_id", sa.Integer(), sa.ForeignKey("document.id"), nullable=False),
        sa.Column("chunk_id", sa.Integer(), sa.ForeignKey("document_chunk.id"), nullable=False),
        sa.Column("score", sa.Float(), nullable=False),
        sa.Column("page_no", sa.Integer(), nullable=True),
        sa.Column("snippet", sa.Text(), nullable=False),
        sa.Column("created_at", sa.DateTime(), nullable=False),
    )


def downgrade() -> None:
    op.drop_table("conversation_citation")
    op.drop_table("conversation_message")
    op.drop_table("conversation")
    op.drop_table("document_chunk_embedding")
    op.drop_table("document_chunk")
    op.drop_table("document")
    op.drop_table("knowledge_space_member")
    op.drop_table("knowledge_space")
    op.drop_index("ix_user_username", table_name="user")
    op.drop_table("user")
