from pydantic import BaseModel
from typing import List, Optional
from enum import Enum
from decimal import Decimal
from datetime import datetime


class WasteType(str, Enum):
    GENERAL_MEDICAL = "GENERAL_MEDICAL"
    SHARPS = "SHARPS"
    PATHOLOGICAL = "PATHOLOGICAL"


class CollectionServiceResponse(BaseModel):
    id: int
    name: str
    description: Optional[str] = None
    wasteType: WasteType
    price: Decimal
    carrierId: int
    requestCount: int
    status: str
    createdAt: Optional[datetime] = None


class CollectionHistoryResponse(BaseModel):
    generatorId: int
    confirmedCollectionServiceIds: List[int]


class RecommendResponse(BaseModel):
    generatorId: int
    recommendedCollectionServices: List[CollectionServiceResponse]
    basedOnWasteType: Optional[WasteType] = None
    message: str


class ApiResponse(BaseModel):
    success: bool
    message: str
    data: Optional[dict] = None
