"""
Pydantic Models for Movies and Responses
"""

from pydantic import BaseModel, Field
from typing import Optional, List
from datetime import datetime
from bson import ObjectId


class PyObjectId(str):
    @classmethod
    def __get_validators__(cls):
        yield cls.validate

    @classmethod
    def validate(cls, v, handler):
        if isinstance(v, ObjectId):
            return str(v)
        return str(v)


class MovieBase(BaseModel):
    title: str
    original_title: Optional[str] = None
    year: Optional[int] = None
    quality: Optional[str] = "HD"
    size: Optional[str] = None
    size_bytes: Optional[int] = None
    duration_minutes: Optional[int] = None
    genres: List[str] = []
    language: Optional[str] = "Hindi"
    poster_url: Optional[str] = None
    backdrop_url: Optional[str] = None
    rating: Optional[float] = 0.0
    plot: Optional[str] = None
    release_date: Optional[str] = None
    is_series: bool = False
    category: str = "hollywood"


class MovieCreate(MovieBase):
    telegram_file_id: str
    telegram_message_id: int


class MovieResponse(MovieBase):
    id: str = Field(alias="_id")
    telegram_file_id: Optional[str] = None
    views: int = 0
    downloads: int = 0
    created_at: Optional[datetime] = None

    class Config:
        populate_by_name = True
        json_encoders = {ObjectId: str}


class MovieListResponse(BaseModel):
    movies: List[MovieResponse]
    total: int
    page: int
    per_page: int
    total_pages: int


class BannerResponse(BaseModel):
    id: str = Field(alias="_id")
    movie_id: str
    title: str
    backdrop_url: str
    poster_url: Optional[str] = None
    tagline: Optional[str] = None

    class Config:
        populate_by_name = True


class SearchResponse(BaseModel):
    query: str
    results: List[MovieResponse]
    total: int


class StreamInfo(BaseModel):
    movie_id: str
    title: str
    stream_url: str
    size_bytes: Optional[int] = None
    mime_type: str = "video/mp4"


class CategoryResponse(BaseModel):
    name: str
    slug: str
    count: int
    icon: Optional[str] = None
