from fastapi import APIRouter, Depends
from sqlalchemy.orm import Session

from app.api.deps import get_current_user
from app.db.session import get_db
from app.schemas.auth import UserProfile
from app.services.statistics_service import statistics_service

router = APIRouter()


@router.get("/overview")
def overview(
    current_user: UserProfile = Depends(get_current_user),
    db: Session = Depends(get_db),
) -> dict[str, int]:
    return statistics_service.overview(db, current_user.id)
