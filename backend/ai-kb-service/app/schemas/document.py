from datetime import datetime

from pydantic import BaseModel


class DocumentVisibilityUpdate(BaseModel):
    visibility_scope: str


class DocumentSummary(BaseModel):
    id: int
    space_id: int
    file_name: str
    file_type: str
    file_size: int
    created_by: int
    created_by_name: str | None = None
    created_by_username: str | None = None
    visibility_scope: str
    parse_status: str
    chunk_count: int
    created_at: datetime


class DocumentDetail(DocumentSummary):
    storage_path: str
    failure_reason: str | None = None
    parse_requested_at: datetime | None = None
    parse_started_at: datetime | None = None
    parse_completed_at: datetime | None = None
