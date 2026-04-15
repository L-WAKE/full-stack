from app.schemas.settings import ModelInfo, ModelSettingUpdate, RetrievalStrategy

model_settings = {"chat_model": "gpt-4.1-mini"}
retrieval_strategy = {
    "profile_name": "enterprise-balanced",
    "top_k": 5,
    "chunk_max_chars": 700,
    "chunk_overlap": 120,
    "rerank_enabled": False,
    "citation_required": True,
    "guardrail_enabled": True,
    "answer_language": "zh-CN",
    "description": "Balanced retrieval profile for SOP, FAQ, implementation guides, and policy documents.",
}


class SettingsService:
    def list_models(self) -> list[ModelInfo]:
        return [
            ModelInfo(
                name="gpt-4.1-mini",
                type="chat",
                provider="OpenAI",
                scenario="General enterprise Q&A and assistant generation",
                status="active",
                cost_level="medium",
                is_default=model_settings["chat_model"] == "gpt-4.1-mini",
            ),
            ModelInfo(
                name="gpt-4.1",
                type="chat",
                provider="OpenAI",
                scenario="Complex enterprise analysis and long-context reasoning",
                status="standby",
                cost_level="high",
                is_default=model_settings["chat_model"] == "gpt-4.1",
            ),
            ModelInfo(
                name="qwen-max",
                type="chat",
                provider="Qwen",
                scenario="Chinese internal knowledge assistant and workflow copilots",
                status="candidate",
                cost_level="medium",
                is_default=model_settings["chat_model"] == "qwen-max",
            ),
            ModelInfo(
                name="deepseek-chat",
                type="chat",
                provider="DeepSeek",
                scenario="Cost-sensitive customer support and operations knowledge search",
                status="candidate",
                cost_level="low",
                is_default=model_settings["chat_model"] == "deepseek-chat",
            ),
            ModelInfo(
                name="text-embedding-3-large",
                type="embedding",
                provider="OpenAI",
                scenario="Production embedding baseline for multi-domain corpora",
                status="candidate",
                cost_level="high",
            ),
            ModelInfo(
                name="bge-large-zh-v1.5",
                type="embedding",
                provider="BAAI",
                scenario="Chinese enterprise document retrieval",
                status="active",
                cost_level="low",
            ),
            ModelInfo(
                name="bge-reranker-v2",
                type="rerank",
                provider="Local",
                scenario="Second-stage rerank for policy, SOP, and compliance documents",
                status="planned",
                cost_level="low",
            ),
        ]

    def update_model(self, payload: ModelSettingUpdate) -> ModelInfo:
        model_settings["chat_model"] = payload.name
        return ModelInfo(
            name=payload.name,
            type="chat",
            provider="Custom",
            scenario="Custom configured model",
            status="active",
            cost_level="custom",
            is_default=True,
        )

    def get_retrieval_strategy(self) -> RetrievalStrategy:
        return RetrievalStrategy(**retrieval_strategy)

    def update_retrieval_strategy(self, payload: RetrievalStrategy) -> RetrievalStrategy:
        retrieval_strategy.update(payload.model_dump())
        return RetrievalStrategy(**retrieval_strategy)


settings_service = SettingsService()
