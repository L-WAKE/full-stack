from functools import lru_cache

from pydantic_settings import BaseSettings, SettingsConfigDict


class Settings(BaseSettings):
    app_name: str = "AI Knowledge Base Service"
    app_env: str = "development"
    api_prefix: str = "/api"
    secret_key: str = "replace-me"
    access_token_expire_minutes: int = 720
    postgres_dsn: str = "postgresql+psycopg://postgres:postgres@localhost:5432/ai_kb"
    redis_dsn: str = "redis://localhost:6379/0"
    qdrant_url: str = "http://localhost:6333"
    upload_dir: str = "./storage/uploads"
    default_chat_model: str = "gpt-4.1-mini"
    default_embedding_model: str = "text-embedding-3-large"
    llm_api_key: str = ""
    llm_base_url: str = "https://api.openai.com/v1"
    llm_model: str = "gpt-4.1-mini"
    llm_temperature: float = 0.2
    llm_timeout_seconds: float = 30.0
    qdrant_collection: str = "ai_kb_chunks"
    embedding_dimension: int = 128
    qdrant_local_path: str = "./storage/qdrant-local"

    model_config = SettingsConfigDict(env_file=".env", case_sensitive=False)


@lru_cache
def get_settings() -> Settings:
    return Settings()
