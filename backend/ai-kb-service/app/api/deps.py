from fastapi import Depends, Header, HTTPException, Query, status
from sqlalchemy.orm import Session

from app.db.session import get_db
from app.schemas.auth import UserProfile
from app.services.auth.service import auth_service


def get_current_user(
    authorization: str | None = Header(default=None),
    access_token: str | None = Query(default=None),
    db: Session = Depends(get_db),
) -> UserProfile:
    token = access_token
    if authorization and authorization.startswith("Bearer "):
        token = authorization.replace("Bearer ", "", 1).strip()

    if not token:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Missing bearer token",
        )
    user = auth_service.get_user_by_token(db, token)
    if not user:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Invalid token",
        )

    return user
