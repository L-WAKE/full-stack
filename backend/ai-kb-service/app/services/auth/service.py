from sqlalchemy import func, select
from sqlalchemy.orm import Session

from app.models.entities import Document, KnowledgeSpace, KnowledgeSpaceMember, User
from app.schemas.auth import LoginRequest, LoginResponse, UserProfile
from app.schemas.space import SpaceSummary


class AuthError(Exception):
    pass


class AuthService:
    def login(self, db: Session, payload: LoginRequest) -> LoginResponse:
        user = db.scalar(select(User).where(User.username == payload.username))
        if not user:
            raise AuthError("用户名或密码错误")

        # 兼容已有 demo 种子数据，同时继续禁止未知账号直接放行。
        password_ok = payload.password == user.password_hash or (
            user.password_hash == "demo" and payload.password == "123456"
        )
        if not password_ok:
            raise AuthError("用户名或密码错误")

        profile = self._to_profile(user)
        return LoginResponse(access_token=f"demo-token-{profile.id}", user=profile)

    def get_user_by_token(self, db: Session, token: str) -> UserProfile | None:
        if not token.startswith("demo-token-"):
            return None
        try:
            user_id = int(token.split("-")[-1])
        except ValueError:
            return None
        user = db.get(User, user_id)
        return self._to_profile(user) if user else None

    def list_user_spaces(self, db: Session, user_id: int) -> list[SpaceSummary]:
        member_count_sq = (
            select(
                KnowledgeSpaceMember.space_id.label("space_id"),
                func.count(KnowledgeSpaceMember.id).label("member_count"),
            )
            .group_by(KnowledgeSpaceMember.space_id)
            .subquery()
        )

        stmt = (
            select(
                KnowledgeSpace,
                func.count(Document.id).label("document_count"),
                func.coalesce(member_count_sq.c.member_count, 0).label("member_count"),
            )
            .join(KnowledgeSpaceMember, KnowledgeSpaceMember.space_id == KnowledgeSpace.id)
            .outerjoin(Document, Document.space_id == KnowledgeSpace.id)
            .outerjoin(member_count_sq, member_count_sq.c.space_id == KnowledgeSpace.id)
            .where(KnowledgeSpaceMember.user_id == user_id)
            .group_by(KnowledgeSpace.id, member_count_sq.c.member_count)
            .order_by(KnowledgeSpace.created_at.desc())
        )
        rows = db.execute(stmt).all()
        return [
            SpaceSummary(
                id=space.id,
                name=space.name,
                description=space.description,
                document_count=document_count,
                member_count=member_count,
                created_at=space.created_at,
            )
            for space, document_count, member_count in rows
        ]

    def _to_profile(self, user: User) -> UserProfile:
        return UserProfile(
            id=user.id,
            username=user.username,
            display_name=user.display_name,
            role=user.role,
        )


auth_service = AuthService()
