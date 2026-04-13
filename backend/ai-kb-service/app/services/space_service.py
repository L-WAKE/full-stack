from sqlalchemy import func, select
from sqlalchemy.orm import Session

from app.models.entities import Document, KnowledgeSpace, KnowledgeSpaceMember, User
from app.schemas.auth import UserProfile
from app.schemas.space import (
    SpaceCreate,
    SpaceDetail,
    SpaceMemberAssign,
    SpaceMemberItem,
    SpaceSummary,
    SpaceUpdate,
)


class SpaceService:
    def can_manage_space(self, db: Session, space_id: int, user_id: int, system_role: str | None = None) -> bool:
        if system_role == "admin":
            return True
        membership = db.scalar(
            select(KnowledgeSpaceMember).where(
                KnowledgeSpaceMember.space_id == space_id,
                KnowledgeSpaceMember.user_id == user_id,
            )
        )
        return membership is not None and membership.role == "owner"

    def list_spaces_for_user(self, db: Session, user_id: int) -> list[SpaceSummary]:
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
        return [
            SpaceSummary(
                id=space.id,
                name=space.name,
                description=space.description,
                document_count=document_count,
                member_count=member_count,
                created_at=space.created_at,
            )
            for space, document_count, member_count in db.execute(stmt).all()
        ]

    def create_space(self, db: Session, payload: SpaceCreate, current_user: UserProfile) -> SpaceDetail:
        space = KnowledgeSpace(
            name=payload.name,
            description=payload.description,
            created_by=current_user.id,
        )
        db.add(space)
        db.flush()
        db.add(KnowledgeSpaceMember(space_id=space.id, user_id=current_user.id, role="owner"))
        db.commit()
        db.refresh(space)
        return self.get_space(db, space.id, current_user.id)

    def get_space(self, db: Session, space_id: int, user_id: int) -> SpaceDetail | None:
        member = db.scalar(
            select(KnowledgeSpaceMember).where(
                KnowledgeSpaceMember.space_id == space_id,
                KnowledgeSpaceMember.user_id == user_id,
            )
        )
        if not member:
            return None
        space = db.get(KnowledgeSpace, space_id)
        document_count = db.scalar(select(func.count(Document.id)).where(Document.space_id == space_id)) or 0
        member_rows = db.execute(
            select(KnowledgeSpaceMember, User)
            .join(User, User.id == KnowledgeSpaceMember.user_id)
            .where(KnowledgeSpaceMember.space_id == space_id)
            .order_by(KnowledgeSpaceMember.created_at.asc())
        ).all()
        members = [
            SpaceMemberItem(
                user_id=member.user_id,
                display_name=user.display_name,
                username=user.username,
                role=member.role,
            )
            for member, user in member_rows
        ]
        return SpaceDetail(
            id=space.id,
            name=space.name,
            description=space.description,
            document_count=document_count,
            member_count=len(members),
            created_at=space.created_at,
            members=members,
        )

    def update_space(self, db: Session, space_id: int, payload: SpaceUpdate, user_id: int) -> SpaceDetail | None:
        detail = self.get_space(db, space_id, user_id)
        if not detail:
            return None
        space = db.get(KnowledgeSpace, space_id)
        space.name = payload.name
        space.description = payload.description
        db.commit()
        return self.get_space(db, space_id, user_id)

    def assign_members(
        self,
        db: Session,
        space_id: int,
        payload: SpaceMemberAssign,
        user_id: int,
        system_role: str | None = None,
    ) -> SpaceDetail | None:
        detail = self.get_space(db, space_id, user_id)
        if not detail or not self.can_manage_space(db, space_id, user_id, system_role):
            return None
        existing_members = {
            item.user_id: item
            for item in db.scalars(
                select(KnowledgeSpaceMember).where(KnowledgeSpaceMember.space_id == space_id)
            ).all()
        }
        for member_id in payload.user_ids:
            member = existing_members.get(member_id)
            if member:
                if member.role != "owner":
                    member.role = payload.role
                continue
            db.add(KnowledgeSpaceMember(space_id=space_id, user_id=member_id, role=payload.role))
        db.commit()
        return self.get_space(db, space_id, user_id)

    def update_member_role(
        self,
        db: Session,
        space_id: int,
        target_user_id: int,
        role: str,
        operator_user_id: int,
        operator_system_role: str | None = None,
    ) -> SpaceDetail | None:
        if role not in {"owner", "member"}:
            return None
        if not self.can_manage_space(db, space_id, operator_user_id, operator_system_role):
            return None
        member = db.scalar(
            select(KnowledgeSpaceMember).where(
                KnowledgeSpaceMember.space_id == space_id,
                KnowledgeSpaceMember.user_id == target_user_id,
            )
        )
        if not member:
            return None
        if member.role == "owner" and role != "owner" and self._owner_count(db, space_id) <= 1:
            return None
        member.role = role
        db.commit()
        return self.get_space(db, space_id, operator_user_id)

    def remove_member(
        self,
        db: Session,
        space_id: int,
        target_user_id: int,
        operator_user_id: int,
        operator_system_role: str | None = None,
    ) -> SpaceDetail | None:
        if target_user_id == operator_user_id:
            return None
        if not self.can_manage_space(db, space_id, operator_user_id, operator_system_role):
            return None
        member = db.scalar(
            select(KnowledgeSpaceMember).where(
                KnowledgeSpaceMember.space_id == space_id,
                KnowledgeSpaceMember.user_id == target_user_id,
            )
        )
        if not member:
            return None
        if member.role == "owner" and self._owner_count(db, space_id) <= 1:
            return None
        db.delete(member)
        db.commit()
        return self.get_space(db, space_id, operator_user_id)

    def _owner_count(self, db: Session, space_id: int) -> int:
        return (
            db.scalar(
                select(func.count(KnowledgeSpaceMember.id)).where(
                    KnowledgeSpaceMember.space_id == space_id,
                    KnowledgeSpaceMember.role == "owner",
                )
            )
            or 0
        )


space_service = SpaceService()
