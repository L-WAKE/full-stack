import hashlib
import math
from pathlib import Path
from uuid import uuid4

from qdrant_client import QdrantClient
from qdrant_client.http import models as qdrant_models

from app.core.config import get_settings

settings = get_settings()


class EmbeddingService:
    def __init__(self) -> None:
        self.dimension = settings.embedding_dimension
        self._client: QdrantClient | None = None
        self._mode = "uninitialized"

    def embed_text(self, text: str) -> list[float]:
        vector = [0.0] * self.dimension
        tokens = [token for token in text.lower().split() if token]
        if not tokens:
            return vector

        for token in tokens:
            digest = hashlib.sha256(token.encode("utf-8")).digest()
            for idx in range(self.dimension):
                vector[idx] += digest[idx % len(digest)] / 255.0

        norm = math.sqrt(sum(item * item for item in vector)) or 1.0
        return [item / norm for item in vector]

    def get_client(self) -> QdrantClient:
        if self._client is not None:
            return self._client

        try:
            client = QdrantClient(url=settings.qdrant_url, timeout=1.5)
            client.get_collections()
            self._client = client
            self._mode = "remote"
            return client
        except Exception:
            storage_path = Path(settings.qdrant_local_path).resolve()
            storage_path.mkdir(parents=True, exist_ok=True)
            self._client = QdrantClient(path=str(storage_path))
            self._mode = "local"
            return self._client

    def current_mode(self) -> str:
        if self._client is None:
            self.get_client()
        return self._mode

    def ensure_collection(self) -> None:
        client = self.get_client()
        collections = {item.name for item in client.get_collections().collections}
        if settings.qdrant_collection not in collections:
            client.create_collection(
                collection_name=settings.qdrant_collection,
                vectors_config=qdrant_models.VectorParams(
                    size=self.dimension,
                    distance=qdrant_models.Distance.COSINE,
                ),
            )

    def upsert_chunks(self, points: list[dict]) -> None:
        if not points:
            return
        self.ensure_collection()
        self.get_client().upsert(
            collection_name=settings.qdrant_collection,
            points=[
                qdrant_models.PointStruct(
                    id=point["point_id"],
                    vector=point["vector"],
                    payload=point["payload"],
                )
                for point in points
            ],
        )

    def delete_document_points(self, document_id: int) -> None:
        self.ensure_collection()
        self.get_client().delete(
            collection_name=settings.qdrant_collection,
            points_selector=qdrant_models.FilterSelector(
                filter=qdrant_models.Filter(
                    must=[
                        qdrant_models.FieldCondition(
                            key="document_id",
                            match=qdrant_models.MatchValue(value=document_id),
                        )
                    ]
                )
            ),
        )

    def search(self, vector: list[float], limit: int) -> list:
        self.ensure_collection()
        return self.get_client().search(
            collection_name=settings.qdrant_collection,
            query_vector=vector,
            limit=limit,
        )

    def new_point_id(self) -> str:
        return str(uuid4())


embedding_service = EmbeddingService()
