from pydantic import BaseModel, Field


class ModelInfo(BaseModel):
    name: str
    type: str
    provider: str
    scenario: str = ""
    status: str = "available"
    cost_level: str = "medium"
    is_default: bool = False


class ModelSettingUpdate(BaseModel):
    name: str


class RetrievalStrategy(BaseModel):
    profile_name: str = "enterprise-balanced"
    top_k: int = Field(default=5, ge=1, le=20)
    chunk_max_chars: int = Field(default=700, ge=200, le=2000)
    chunk_overlap: int = Field(default=120, ge=0, le=500)
    rerank_enabled: bool = False
    citation_required: bool = True
    guardrail_enabled: bool = True
    answer_language: str = "zh-CN"
    description: str = "Balanced retrieval profile for enterprise knowledge search."
