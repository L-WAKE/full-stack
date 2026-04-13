from datetime import datetime

from pydantic import BaseModel, Field


class SpaceCreate(BaseModel):
    name: str = Field(min_length=2, max_length=64)
    description: str = ""


class SpaceUpdate(BaseModel):
    name: str = Field(min_length=2, max_length=64)
    description: str = ""


class SpaceMemberAssign(BaseModel):
    user_ids: list[int]
    role: str = "member"


class SpaceMemberItem(BaseModel):
    user_id: int
    display_name: str
    username: str
    role: str


class SpaceSummary(BaseModel):
    id: int
    name: str
    description: str
    document_count: int = 0
    member_count: int = 0
    created_at: datetime


class SpaceDetail(SpaceSummary):
    members: list[SpaceMemberItem] = []
