"""add document visibility scope"""

from alembic import op
import sqlalchemy as sa


revision = "20260413_0003"
down_revision = "20260413_0002"
branch_labels = None
depends_on = None


def upgrade() -> None:
    op.add_column(
        "document",
        sa.Column("visibility_scope", sa.String(length=32), nullable=False, server_default="SPACE"),
    )
    op.execute("UPDATE document SET visibility_scope = 'SPACE' WHERE visibility_scope IS NULL")
    op.alter_column("document", "visibility_scope", server_default=None)


def downgrade() -> None:
    op.drop_column("document", "visibility_scope")
