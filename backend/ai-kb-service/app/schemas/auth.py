from pydantic import BaseModel


class LoginRequest(BaseModel):
    username: str
    password: str


class UserProfile(BaseModel):
    id: int
    username: str
    display_name: str
    role: str


class LoginResponse(BaseModel):
    access_token: str
    token_type: str = "bearer"
    user: UserProfile

