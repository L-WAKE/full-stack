from pydantic import BaseModel


class ModelInfo(BaseModel):
    name: str
    type: str
    provider: str
    is_default: bool = False


class ModelSettingUpdate(BaseModel):
    name: str
