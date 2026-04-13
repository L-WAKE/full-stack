from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy.orm import Session

from app.api.deps import get_current_user
from app.db.session import get_db
from app.schemas.auth import LoginRequest, LoginResponse, UserProfile
from app.schemas.space import SpaceSummary
from app.services.auth.service import AuthError, auth_service

router = APIRouter()


@router.post("/login", response_model=LoginResponse)
def login(payload: LoginRequest, db: Session = Depends(get_db)) -> LoginResponse:
    try:
        return auth_service.login(db, payload)
    except AuthError as exc:
        raise HTTPException(status_code=status.HTTP_401_UNAUTHORIZED, detail=str(exc)) from exc


@router.get("/profile", response_model=UserProfile)
def profile(current_user: UserProfile = Depends(get_current_user)) -> UserProfile:
    return current_user


@router.get("/spaces", response_model=list[SpaceSummary])
def my_spaces(
    current_user: UserProfile = Depends(get_current_user),
    db: Session = Depends(get_db),
) -> list[SpaceSummary]:
    return auth_service.list_user_spaces(db, current_user.id)
