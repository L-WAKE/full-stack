from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware

from app.api.router import api_router
from app.core.config import get_settings
from app.db.init_db import create_schema, seed_demo_data
from app.db.session import SessionLocal

settings = get_settings()

app = FastAPI(title=settings.app_name)
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)
app.include_router(api_router, prefix=settings.api_prefix)


@app.on_event("startup")
def startup() -> None:
    create_schema()
    db = SessionLocal()
    try:
        seed_demo_data(db)
    finally:
        db.close()


@app.get("/health")
def healthcheck() -> dict[str, str]:
    return {"status": "ok", "service": settings.app_name}
