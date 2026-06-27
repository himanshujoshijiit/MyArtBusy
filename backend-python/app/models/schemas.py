from typing import Optional
from pydantic import BaseModel, Field


class SearchRequest(BaseModel):
    city: Optional[str] = None
    locality: Optional[str] = None
    pincode: Optional[str] = None
    occasion: Optional[str] = None
    service_category: Optional[str] = None
    salon_service: Optional[str] = None
    skin_tone: Optional[str] = None
    min_budget: Optional[float] = None
    max_budget: Optional[float] = None
    min_rating: Optional[float] = None
    available_date: Optional[str] = None
    top_artist_only: bool = False
    latitude: Optional[float] = None
    longitude: Optional[float] = None
    radius_km: float = Field(default=10.0, ge=1, le=50)
    sort_by: str = "rating"
    page: int = Field(default=1, ge=1)
    page_size: int = Field(default=20, ge=1, le=50)


class MuaSearchResult(BaseModel):
    id: str
    display_name: str
    bio: Optional[str]
    city: str
    locality: Optional[str]
    pincode: Optional[str] = None
    country: Optional[str]
    occasions: list[str]
    skin_tone_expertise: list[str]
    min_price: Optional[float]
    max_price: Optional[float]
    rating: float
    review_count: int
    total_bookings: int
    top_artist: bool
    verified: bool
    featured: bool
    response_time_label: str
    portfolio_preview: list[str]
    relevance_score: float = 0.0
    distance_km: Optional[float] = None


class SearchResponse(BaseModel):
    results: list[MuaSearchResult]
    total: int
    page: int
    page_size: int
    query_summary: str
