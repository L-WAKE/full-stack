from fastapi import APIRouter

from app.api.routes.auth import router as auth_router
from app.api.routes.chat import router as chat_router
from app.api.routes.documents import router as documents_router
from app.api.routes.retrieval import router as retrieval_router
from app.api.routes.settings import router as settings_router
from app.api.routes.spaces import router as spaces_router
from app.api.routes.statistics import router as statistics_router

api_router = APIRouter()
api_router.include_router(auth_router, prefix="/auth", tags=["auth"])
api_router.include_router(spaces_router, prefix="/spaces", tags=["spaces"])
api_router.include_router(documents_router, prefix="/documents", tags=["documents"])
api_router.include_router(retrieval_router, prefix="/retrieval", tags=["retrieval"])
api_router.include_router(chat_router, prefix="/chat", tags=["chat"])
api_router.include_router(settings_router, tags=["settings"])
api_router.include_router(statistics_router, prefix="/statistics", tags=["statistics"])

