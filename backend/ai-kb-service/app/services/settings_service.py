from app.schemas.settings import ModelInfo, ModelSettingUpdate

model_settings = {"chat_model": "gpt-4.1-mini"}


class SettingsService:
    def list_models(self) -> list[ModelInfo]:
        return [
            ModelInfo(
                name="gpt-4.1-mini",
                type="chat",
                provider="OpenAI",
                is_default=model_settings["chat_model"] == "gpt-4.1-mini",
            ),
            ModelInfo(name="text-embedding-3-large", type="embedding", provider="OpenAI"),
            ModelInfo(name="bge-reranker-v2", type="rerank", provider="Local"),
        ]

    def update_model(self, payload: ModelSettingUpdate) -> ModelInfo:
        model_settings["chat_model"] = payload.name
        return ModelInfo(name=payload.name, type="chat", provider="Custom", is_default=True)


settings_service = SettingsService()
