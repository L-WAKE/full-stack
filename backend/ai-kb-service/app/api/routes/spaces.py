from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy.orm import Session

from app.api.deps import get_current_user
from app.db.session import get_db
from app.schemas.auth import UserProfile
from pydantic import BaseModel

from app.schemas.space import SpaceCreate, SpaceDetail, SpaceMemberAssign, SpaceSummary, SpaceUpdate
from app.services.document.repository import document_repository
from app.services.space_service import space_service

router = APIRouter()


class SpaceMemberRoleUpdate(BaseModel):
    role: str


@router.get("", response_model=list[SpaceSummary])
def list_spaces(
    current_user: UserProfile = Depends(get_current_user),
    db: Session = Depends(get_db),
) -> list[SpaceSummary]:
    return space_service.list_spaces_for_user(db, current_user.id)


@router.post("", response_model=SpaceDetail, status_code=status.HTTP_201_CREATED)
def create_space(
    payload: SpaceCreate,
    current_user: UserProfile = Depends(get_current_user),
    db: Session = Depends(get_db),
) -> SpaceDetail:
    return space_service.create_space(db, payload, current_user)


@router.get("/{space_id}", response_model=SpaceDetail)
def get_space(
    space_id: int,
    current_user: UserProfile = Depends(get_current_user),
    db: Session = Depends(get_db),
) -> SpaceDetail:
    space = space_service.get_space(db, space_id, current_user.id)
    if not space:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="Space not found")
    return space


@router.put("/{space_id}", response_model=SpaceDetail)
def update_space(
    space_id: int,
    payload: SpaceUpdate,
    current_user: UserProfile = Depends(get_current_user),
    db: Session = Depends(get_db),
) -> SpaceDetail:
    space = space_service.update_space(db, space_id, payload, current_user.id)
    if not space:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="Space not found")
    return space


@router.post("/{space_id}/members", response_model=SpaceDetail)
def assign_members(
    space_id: int,
    payload: SpaceMemberAssign,
    current_user: UserProfile = Depends(get_current_user),
    db: Session = Depends(get_db),
) -> SpaceDetail:
    space = space_service.assign_members(db, space_id, payload, current_user.id, current_user.role)
    if not space:
        raise HTTPException(status_code=status.HTTP_403_FORBIDDEN, detail="No permission to manage members")
    return space


@router.put("/{space_id}/members/{user_id}/role", response_model=SpaceDetail)
def update_member_role(
    space_id: int,
    user_id: int,
    payload: SpaceMemberRoleUpdate,
    current_user: UserProfile = Depends(get_current_user),
    db: Session = Depends(get_db),
) -> SpaceDetail:
    space = space_service.update_member_role(
        db,
        space_id,
        user_id,
        payload.role,
        current_user.id,
        current_user.role,
    )
    if not space:
        raise HTTPException(status_code=status.HTTP_403_FORBIDDEN, detail="No permission to update member role")
    return space


@router.delete("/{space_id}/members/{user_id}", response_model=SpaceDetail)
def remove_member(
    space_id: int,
    user_id: int,
    current_user: UserProfile = Depends(get_current_user),
    db: Session = Depends(get_db),
) -> SpaceDetail:
    space = space_service.remove_member(db, space_id, user_id, current_user.id, current_user.role)
    if not space:
        raise HTTPException(status_code=status.HTTP_403_FORBIDDEN, detail="No permission to remove member")
    return space


@router.get("/{space_id}/documents/count")
def document_count(
    space_id: int,
    current_user: UserProfile = Depends(get_current_user),
    db: Session = Depends(get_db),
) -> dict[str, int]:
    if not space_service.get_space(db, space_id, current_user.id):
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="Space not found")
    return {"count": document_repository.count_by_space(db, space_id)}
