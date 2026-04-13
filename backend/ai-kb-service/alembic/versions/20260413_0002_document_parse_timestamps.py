"""add document parse timestamps"""

from alembic import op
import sqlalchemy as sa


revision = "20260413_0002"
down_revision = "20260413_0001"
branch_labels = None
depends_on = None


def upgrade() -> None:
    op.add_column("document", sa.Column("parse_requested_at", sa.DateTime(), nullable=True))
    op.add_column("document", sa.Column("parse_started_at", sa.DateTime(), nullable=True))
    op.add_column("document", sa.Column("parse_completed_at", sa.DateTime(), nullable=True))


def downgrade() -> None:
    op.drop_column("document", "parse_completed_at")
    op.drop_column("document", "parse_started_at")
    op.drop_column("document", "parse_requested_at")
