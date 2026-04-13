from sqlalchemy import select
from sqlalchemy.orm import Session

from app.db.base import Base
from app.db.session import engine
from app.models.entities import Document, DocumentChunk, KnowledgeSpace, KnowledgeSpaceMember, User


def create_schema() -> None:
    Base.metadata.create_all(bind=engine)


def seed_demo_data(db: Session) -> None:
    if db.scalar(select(User).where(User.username == "admin")):
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
