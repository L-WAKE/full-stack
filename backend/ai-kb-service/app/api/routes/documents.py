from fastapi import APIRouter, BackgroundTasks, Depends, File, Form, HTTPException, UploadFile, status
from sqlalchemy.orm import Session

from app.api.deps import get_current_user
from app.db.session import get_db
from app.schemas.auth import UserProfile
from app.schemas.document import DocumentDetail, DocumentSummary, DocumentVisibilityUpdate
from app.services.document.service import document_service

router = APIRouter()


@router.get("", response_model=list[DocumentSummary])
def list_documents(
    space_id: int | None = None,
    current_user: UserProfile = Depends(get_current_user),
    db: Session = Depends(get_db),
) -> list[DocumentSummary]:
    return document_service.list_documents(db, current_user.id, space_id)


@router.post("/upload", response_model=DocumentDetail, status_code=status.HTTP_201_CREATED)
async def upload_document(
    space_id: int = Form(...),
    visibility_scope: str = Form("SPACE"),
    file: UploadFile = File(...),
    current_user: UserProfile = Depends(get_current_user),
    db: Session = Depends(get_db),
) -> DocumentDetail:
    document = await document_service.upload_document(db, space_id, file, visibility_scope, current_user)
    if not document:
        raise HTTPException(status_code=status.HTTP_400_BAD_REQUEST, detail="Upload failed")
    return document


@router.get("/{document_id}", response_model=DocumentDetail)
def get_document(
    document_id: int,
    current_user: UserProfile = Depends(get_current_user),
    db: Session = Depends(get_db),
) -> DocumentDetail:
    document = document_service.get_document(db, document_id, current_user.id)
    if not document:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="Document not found")
    return document


@router.delete("/{document_id}", status_code=status.HTTP_204_NO_CONTENT)
def delete_document(
    document_id: int,
    current_user: UserProfile = Depends(get_current_user),
    db: Session = Depends(get_db),
) -> None:
    document = document_service._get_membership_model(db, document_id, current_user.id)
    if not document or not document_service.can_manage_document(db, document, current_user):
        raise HTTPException(status_code=status.HTTP_403_FORBIDDEN, detail="No permission to delete document")
    deleted = document_service.delete_document(db, document_id, current_user.id)
    if not deleted:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="Document not found")


@router.post("/{document_id}/parse", response_model=DocumentDetail)
def trigger_parse(
    document_id: int,
    background_tasks: BackgroundTasks,
    current_user: UserProfile = Depends(get_current_user),
    db: Session = Depends(get_db),
) -> DocumentDetail:
    raw_document = document_service._get_membership_model(db, document_id, current_user.id)
    if not raw_document or not document_service.can_manage_document(db, raw_document, current_user):
        raise HTTPException(status_code=status.HTTP_403_FORBIDDEN, detail="No permission to parse document")
    document = document_service.trigger_parse(db, document_id, current_user.id)
    if not document:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="Document not found")
    if document.parse_status == "PENDING":
        background_tasks.add_task(document_service.process_document, document_id)
    return document


@router.put("/{document_id}/visibility", response_model=DocumentDetail)
def update_visibility(
    document_id: int,
    payload: DocumentVisibilityUpdate,
    current_user: UserProfile = Depends(get_current_user),
    db: Session = Depends(get_db),
) -> DocumentDetail:
    document = document_service.update_visibility(db, document_id, payload.visibility_scope, current_user)
    if not document:
        raise HTTPException(status_code=status.HTTP_403_FORBIDDEN, detail="No permission to update visibility")
    return document


@router.get("/{document_id}/chunks")
def list_chunks(
    document_id: int,
    current_user: UserProfile = Depends(get_current_user),
    db: Session = Depends(get_db),
) -> list[dict]:
    chunks = document_service.list_chunks(db, document_id, current_user.id)
    if chunks is None:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="Document not found")
    return chunks
