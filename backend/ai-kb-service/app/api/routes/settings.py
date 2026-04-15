from fastapi import APIRouter, Depends

from app.api.deps import get_current_user
from app.schemas.auth import UserProfile
from app.schemas.settings import ModelInfo, ModelSettingUpdate, RetrievalStrategy
from app.services.settings_service import settings_service

router = APIRouter()


@router.get("/models", response_model=list[ModelInfo])
def list_models(current_user: UserProfile = Depends(get_current_user)) -> list[ModelInfo]:
    return settings_service.list_models()


@router.put("/settings/model", response_model=ModelInfo)
def update_model(
    payload: ModelSettingUpdate,
    current_user: UserProfile = Depends(get_current_user),
) -> ModelInfo:
    return settings_service.update_model(payload)


@router.get("/settings/retrieval-strategy", response_model=RetrievalStrategy)
def get_retrieval_strategy(
    current_user: UserProfile = Depends(get_current_user),
) -> RetrievalStrategy:
    return settings_service.get_retrieval_strategy()


@router.put("/settings/retrieval-strategy", response_model=RetrievalStrategy)
def update_retrieval_strategy(
    payload: RetrievalStrategy,
    current_user: UserProfile = Depends(get_current_user),
) -> RetrievalStrategy:
    return settings_service.update_retrieval_strategy(payload)
