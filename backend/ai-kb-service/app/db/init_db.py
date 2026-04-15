from sqlalchemy import inspect, select, text
from sqlalchemy.orm import Session

from app.db.base import Base
from app.db.session import engine
from app.models.entities import Document, DocumentChunk, KnowledgeSpace, KnowledgeSpaceMember, User


def create_schema() -> None:
    Base.metadata.create_all(bind=engine)
    ensure_compatible_schema()


def ensure_compatible_schema() -> None:
    """Keep local demo databases compatible when models evolve during development."""
    inspector = inspect(engine)
    if "document" not in inspector.get_table_names():
        return

    existing = {column["name"] for column in inspector.get_columns("document")}
    migrations = {
        "visibility_scope": "ALTER TABLE document ADD COLUMN visibility_scope VARCHAR(32) DEFAULT 'SPACE'",
        "failure_reason": "ALTER TABLE document ADD COLUMN failure_reason TEXT",
        "parse_requested_at": "ALTER TABLE document ADD COLUMN parse_requested_at DATETIME",
        "parse_started_at": "ALTER TABLE document ADD COLUMN parse_started_at DATETIME",
        "parse_completed_at": "ALTER TABLE document ADD COLUMN parse_completed_at DATETIME",
    }

    with engine.begin() as connection:
        for column_name, ddl in migrations.items():
            if column_name not in existing:
                connection.execute(text(ddl))


def seed_demo_data(db: Session) -> None:
    existing_admin = db.scalar(select(User).where(User.username == "admin"))
    if existing_admin:
        repair_demo_text(db)
        return

    admin = User(username="admin", display_name="系统管理员", password_hash="demo", role="admin")
    operator = User(username="operator", display_name="业务运营", password_hash="demo", role="editor")
    db.add_all([admin, operator])
    db.flush()

    space = KnowledgeSpace(
        name="租赁业务知识库",
        description="租赁流程、FAQ 和 SOP",
        created_by=admin.id,
    )
    db.add(space)
    db.flush()
    db.add_all(
        [
            KnowledgeSpaceMember(space_id=space.id, user_id=admin.id, role="owner"),
            KnowledgeSpaceMember(space_id=space.id, user_id=operator.id, role="member"),
        ]
    )

    document = Document(
        space_id=space.id,
        file_name="房源录入规范.pdf",
        file_type="pdf",
        file_size=248120,
        storage_path="storage/uploads/demo-house-rule.pdf",
        parse_status="READY",
        chunk_count=2,
        created_by=admin.id,
    )
    db.add(document)
    db.flush()
    db.add_all(
        [
            DocumentChunk(
                document_id=document.id,
                space_id=space.id,
                chunk_index=0,
                content="房源录入前必须核验业主身份、产权证明和委托凭证。",
                content_preview="房源录入前必须核验业主身份、产权证明和委托凭证。",
                page_no=1,
                section_path="一、录入前准备",
                token_estimate=28,
            ),
            DocumentChunk(
                document_id=document.id,
                space_id=space.id,
                chunk_index=1,
                content="上传图片前应删除水印、模糊图和重复图，确保房源主图清晰。",
                content_preview="上传图片前应删除水印、模糊图和重复图，确保房源主图清晰。",
                page_no=2,
                section_path="二、图片规范",
                token_estimate=30,
            ),
        ]
    )
    db.commit()


def repair_demo_text(db: Session) -> None:
    admin = db.scalar(select(User).where(User.username == "admin"))
    if admin:
        admin.display_name = "系统管理员"

    operator = db.scalar(select(User).where(User.username == "operator"))
    if operator:
        operator.display_name = "业务运营"

    space = db.scalar(select(KnowledgeSpace).order_by(KnowledgeSpace.id.asc()))
    if space:
        space.name = "租赁业务知识库"
        space.description = "租赁流程、FAQ 和 SOP"

    document = db.scalar(select(Document).where(Document.storage_path == "storage/uploads/demo-house-rule.pdf"))
    if document:
        document.file_name = "房源录入规范.pdf"
        chunks = db.scalars(
            select(DocumentChunk).where(DocumentChunk.document_id == document.id).order_by(DocumentChunk.chunk_index)
        ).all()
        if len(chunks) >= 1:
            chunks[0].content = "房源录入前必须核验业主身份、产权证明和委托凭证。"
            chunks[0].content_preview = chunks[0].content
            chunks[0].section_path = "一、录入前准备"
        if len(chunks) >= 2:
            chunks[1].content = "上传图片前应删除水印、模糊图和重复图，确保房源主图清晰。"
            chunks[1].content_preview = chunks[1].content
            chunks[1].section_path = "二、图片规范"

    db.commit()
