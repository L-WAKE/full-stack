from pydantic import BaseModel


class RetrievalRequest(BaseModel):
    space_id: int
    query: str
    top_k: int = 5


class RetrievalChunk(BaseModel):
    chunk_id: int
    document_id: int
    document_name: str
    visibility_scope: str = "SPACE"
    created_by: int | None = None
    created_by_name: str | None = None
    created_by_username: str | None = None
    section_path: str | None = None
    score: float
    rerank_score: float
    content_preview: str
    page_no: int | None = None


class RetrievalResponse(BaseModel):
    query: str
    context: str
    chunks: list[RetrievalChunk]
    retrieval_mode: str = "vector"
