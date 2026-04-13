from datetime import datetime

from pydantic import BaseModel, Field


class ConversationCreate(BaseModel):
    space_id: int
    title: str = Field(min_length=2, max_length=128)


class ConversationDetail(BaseModel):
    id: int
    space_id: int
    user_id: int
    title: str
    created_at: datetime
    updated_at: datetime


class MessageCreate(BaseModel):
    content: str = Field(min_length=1)


class MessageRecord(BaseModel):
    id: int
    conversation_id: int
    role: str
    content: str
    created_at: datetime

