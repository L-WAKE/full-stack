from fastapi import APIRouter, Depends
from sqlalchemy.orm import Session

from app.api.deps import get_current_user
from app.db.session import get_db
from app.schemas.auth import UserProfile
from app.schemas.retrieval import RetrievalRequest, RetrievalResponse
from app.services.retrieval.service import retrieval_service

router = APIRouter()


@router.post("/search", response_model=RetrievalResponse)
def search(
    payload: RetrievalRequest,
    current_user: UserProfile = Depends(get_current_user),
    db: Session = Depends(get_db),
) -> RetrievalResponse:
    return retrieval_service.search(db, payload, current_user.id)
