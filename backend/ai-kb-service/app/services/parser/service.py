from pathlib import Path

import fitz
from docx import Document as DocxDocument


class ParserService:
    def parse(self, file_path: str, file_type: str) -> list[dict]:
        suffix = file_type.lower()
        if suffix == "pdf":
            return self._parse_pdf(file_path)
        if suffix == "docx":
            return self._parse_docx(file_path)
        raise ValueError(f"Unsupported file type: {file_type}")

    def _parse_pdf(self, file_path: str) -> list[dict]:
        pages: list[dict] = []
        doc = fitz.open(file_path)
        try:
            for index, page in enumerate(doc, start=1):
                text = page.get_text("text").strip()
                if text:
                    pages.append(
                        {
                            "page_no": index,
                            "section_path": f"PDF 第 {index} 页",
                            "content": text,
                        }
                    )
        finally:
            doc.close()
        return pages

    def _parse_docx(self, file_path: str) -> list[dict]:
        document = DocxDocument(file_path)
        blocks: list[dict] = []
        current_heading = Path(file_path).stem
        buffer: list[str] = []

        def flush_buffer() -> None:
            if not buffer:
                return
            content = "\n".join(buffer).strip()
            if content:
                blocks.append(
                    {
                        "page_no": None,
                        "section_path": current_heading,
                        "content": content,
                    }
                )
            buffer.clear()

        for paragraph in document.paragraphs:
            text = paragraph.text.strip()
            if not text:
                continue
            style_name = paragraph.style.name.lower() if paragraph.style and paragraph.style.name else ""
            if "heading" in style_name or style_name.startswith("标题"):
                flush_buffer()
                current_heading = text
            else:
                buffer.append(text)

        flush_buffer()
        return blocks


parser_service = ParserService()
