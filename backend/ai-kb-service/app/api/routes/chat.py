import asyncio
import json

from fastapi import APIRouter, Depends, HTTPException, status
from fastapi.responses import StreamingResponse
from sqlalchemy.orm import Session

from app.api.deps import get_current_user
from app.db.session import get_db
from app.schemas.auth import UserProfile
from app.schemas.chat import ConversationCreate, ConversationDetail, MessageCreate, MessageRecord
from app.services.chat.service import chat_service

router = APIRouter()


@router.post("/conversations", response_model=ConversationDetail, status_code=status.HTTP_201_CREATED)
def create_conversation(
    payload: ConversationCreate,
    current_user: UserProfile = Depends(get_current_user),
    db: Session = Depends(get_db),
) -> ConversationDetail:
    conversation = chat_service.create_conversation(db, payload, current_user)
    if not conversation:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="Space not found")
    return conversation


@router.get("/conversations", response_model=list[ConversationDetail])
def list_conversations(
    space_id: int | None = None,
    current_user: UserProfile = Depends(get_current_user),
    db: Session = Depends(get_db),
) -> list[ConversationDetail]:
    return chat_service.list_conversations(db, current_user.id, space_id)


@router.get("/conversations/{conversation_id}/messages", response_model=list[MessageRecord])
def list_messages(
    conversation_id: int,
    current_user: UserProfile = Depends(get_current_user),
    db: Session = Depends(get_db),
) -> list[MessageRecord]:
    items = chat_service.list_messages(db, conversation_id, current_user.id)
    if items is None:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="Conversation not found")
    return items


@router.post("/conversations/{conversation_id}/messages", response_model=MessageRecord)
def create_message(
    conversation_id: int,
    payload: MessageCreate,
    current_user: UserProfile = Depends(get_current_user),
    db: Session = Depends(get_db),
) -> MessageRecord:
    message = chat_service.add_user_message(db, conversation_id, payload, current_user.id)
    if not message:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="Conversation not found")
    return message


@router.get("/conversations/{conversation_id}/stream")
def stream_answer(
    conversation_id: int,
    current_user: UserProfile = Depends(get_current_user),
    db: Session = Depends(get_db),
) -> StreamingResponse:
    if not chat_service.can_access(db, conversation_id, current_user.id):
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="Conversation not found")

    async def event_generator():
        for chunk in chat_service.stream_answer(db, conversation_id):
            yield f"data: {json.dumps(chunk, ensure_ascii=False)}\n\n"
            await asyncio.sleep(0.15)

    return StreamingResponse(event_generator(), media_type="text/event-stream")
