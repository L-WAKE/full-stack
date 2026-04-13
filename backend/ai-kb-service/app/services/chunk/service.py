class ChunkService:
    def chunk(self, sections: list[dict], max_chars: int = 700, overlap: int = 120) -> list[dict]:
        chunks: list[dict] = []
        chunk_index = 0

        for section in sections:
            paragraphs = [item.strip() for item in section["content"].splitlines() if item.strip()]
            if not paragraphs:
                continue

            current = ""
            for paragraph in paragraphs:
                candidate = f"{current}\n{paragraph}".strip() if current else paragraph
                if len(candidate) <= max_chars:
                    current = candidate
                    continue

                if current:
                    chunks.append(
                        {
                            "chunk_index": chunk_index,
                            "content": current,
                            "content_preview": current[:220],
                            "page_no": section.get("page_no"),
                            "section_path": section.get("section_path", ""),
                            "token_estimate": max(1, len(current) // 2),
                        }
                    )
                    chunk_index += 1
                    tail = current[-overlap:] if overlap > 0 else ""
                    current = f"{tail}\n{paragraph}".strip()
                else:
                    current = paragraph[:max_chars]

            if current:
                chunks.append(
                    {
                        "chunk_index": chunk_index,
                        "content": current,
                        "content_preview": current[:220],
                        "page_no": section.get("page_no"),
                        "section_path": section.get("section_path", ""),
                        "token_estimate": max(1, len(current) // 2),
                    }
                )
                chunk_index += 1

        return chunks


chunk_service = ChunkService()
