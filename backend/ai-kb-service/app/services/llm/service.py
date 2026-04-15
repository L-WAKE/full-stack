from __future__ import annotations

from typing import Any

import httpx

from app.core.config import get_settings

settings = get_settings()


class LLMConfigurationError(Exception):
    pass


class LLMService:
    def is_configured(self) -> bool:
        return bool(settings.llm_api_key and settings.llm_base_url and settings.llm_model)

    def synthesize_grounded_answer(
        self,
        *,
        question: str,
        retrieval_context: str,
        citations: list[dict[str, Any]],
        answer_language: str = "zh-CN",
        citation_required: bool = True,
        guardrail_enabled: bool = True,
    ) -> dict[str, Any]:
        if not self.is_configured():
            raise LLMConfigurationError(
                "LLM_API_KEY / LLM_BASE_URL / LLM_MODEL are not fully configured."
            )

        system_prompt = self._build_system_prompt(
            answer_language=answer_language,
            citation_required=citation_required,
            guardrail_enabled=guardrail_enabled,
        )
        user_prompt = self._build_user_prompt(question=question, retrieval_context=retrieval_context, citations=citations)

        response = self._chat_completion(
            messages=[
                {"role": "system", "content": system_prompt},
                {"role": "user", "content": user_prompt},
            ]
        )
        answer = self._extract_text(response)
        return {
            "answer": answer,
            "model": settings.llm_model,
            "provider_base_url": settings.llm_base_url,
            "raw": response,
        }

    def _chat_completion(self, messages: list[dict[str, str]]) -> dict[str, Any]:
        base_url = settings.llm_base_url.rstrip("/")
        url = f"{base_url}/chat/completions"
        payload = {
            "model": settings.llm_model,
            "temperature": settings.llm_temperature,
            "messages": messages,
        }
        headers = {
            "Authorization": f"Bearer {settings.llm_api_key}",
            "Content-Type": "application/json",
        }
        with httpx.Client(timeout=settings.llm_timeout_seconds) as client:
            response = client.post(url, headers=headers, json=payload)
            response.raise_for_status()
            return response.json()

    def _extract_text(self, payload: dict[str, Any]) -> str:
        choices = payload.get("choices") or []
        if not choices:
            return ""
        message = choices[0].get("message") or {}
        content = message.get("content") or ""
        if isinstance(content, str):
            return content.strip()
        if isinstance(content, list):
            parts: list[str] = []
            for item in content:
                if isinstance(item, dict) and item.get("type") == "text":
                    parts.append(item.get("text", ""))
            return "\n".join(part.strip() for part in parts if part).strip()
        return str(content).strip()

    def _build_system_prompt(
        self,
        *,
        answer_language: str,
        citation_required: bool,
        guardrail_enabled: bool,
    ) -> str:
        rules = [
            "You are an enterprise knowledge-base assistant.",
            f"Answer in {answer_language}.",
            "Only use the provided retrieval context and citations.",
            "If the context is insufficient, explicitly say that the knowledge base does not provide enough evidence.",
            "Do not invent policies, figures, or process details.",
        ]
        if citation_required:
            rules.append(
                "Every final answer must include a short 'Sources' section that references the provided citation IDs."
            )
        if guardrail_enabled:
            rules.append(
                "When evidence is weak or ambiguous, present uncertainty and recommend manual review."
            )
        return "\n".join(f"- {rule}" for rule in rules)

    def _build_user_prompt(
        self,
        *,
        question: str,
        retrieval_context: str,
        citations: list[dict[str, Any]],
    ) -> str:
        citation_lines = []
        for index, item in enumerate(citations, start=1):
            citation_lines.append(
                f"[C{index}] doc={item.get('document_name')} "
                f"section={item.get('section_path') or '-'} "
                f"page={item.get('page_no') or '-'} "
                f"score={item.get('score')} "
                f"snippet={item.get('content_preview')}"
            )
        return (
            f"Question:\n{question}\n\n"
            f"Retrieval context:\n{retrieval_context or '(empty)'}\n\n"
            "Citations:\n"
            + ("\n".join(citation_lines) if citation_lines else "(none)")
        )


llm_service = LLMService()
