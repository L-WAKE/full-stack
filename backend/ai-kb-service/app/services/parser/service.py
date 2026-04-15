from pathlib import Path

import fitz
from docx import Document as DocxDocument
from docx.document import Document as DocxDocumentType
from docx.table import _Cell, Table
from docx.text.paragraph import Paragraph


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
                text = self._normalize_text(page.get_text("text"))
                if not text:
                    continue
                pages.append(
                    {
                        "page_no": index,
                        "section_path": f"PDF Page {index}",
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
            content = self._normalize_text("\n".join(buffer))
            if content:
                blocks.append(
                    {
                        "page_no": None,
                        "section_path": current_heading,
                        "content": content,
                    }
                )
            buffer.clear()

        for block in self._iter_block_items(document):
            if isinstance(block, Paragraph):
                text = self._normalize_text(block.text)
                if not text:
                    continue
                style_name = ""
                if block.style and block.style.name:
                    style_name = block.style.name.strip().lower()

                if self._is_heading_style(style_name):
                    flush_buffer()
                    current_heading = text
                else:
                    buffer.append(text)
                continue

            if isinstance(block, Table):
                table_text = self._extract_table_text(block)
                if table_text:
                    buffer.append(table_text)

        flush_buffer()
        return blocks

    def _iter_block_items(self, parent: DocxDocumentType | _Cell):
        if isinstance(parent, DocxDocumentType):
            parent_element = parent.element.body
        else:
            parent_element = parent._tc

        for child in parent_element.iterchildren():
            if child.tag.endswith("}p"):
                yield Paragraph(child, parent)
            elif child.tag.endswith("}tbl"):
                yield Table(child, parent)

    def _extract_table_text(self, table: Table) -> str:
        rows: list[str] = []
        for row in table.rows:
            cells: list[str] = []
            for cell in row.cells:
                cell_parts = []
                for paragraph in cell.paragraphs:
                    text = self._normalize_text(paragraph.text)
                    if text:
                        cell_parts.append(text)
                if cell_parts:
                    cells.append(" / ".join(cell_parts))
            if cells:
                rows.append(" | ".join(cells))
        return "\n".join(rows).strip()

    def _is_heading_style(self, style_name: str) -> bool:
        if not style_name:
            return False
        heading_tokens = ("heading", "title", "\u6807\u9898")
        return any(token in style_name for token in heading_tokens)

    def _normalize_text(self, text: str | None) -> str:
        if not text:
            return ""
        value = text.replace("\r\n", "\n").replace("\r", "\n").strip()
        lines = [line.strip() for line in value.split("\n")]
        return "\n".join(line for line in lines if line)


parser_service = ParserService()
