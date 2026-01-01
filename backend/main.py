"""
CineMax Backend API - Main Application Entry Point
"""

from fastapi import FastAPI, HTTPException, Query, Response, Request
from fastapi.middleware.cors import CORSMiddleware
from fastapi.responses import StreamingResponse, JSONResponse
from contextlib import asynccontextmanager
import logging
from typing import Optional, List
from datetime import datetime
import asyncio
import os

from config import HOST, PORT, CACHE_TTL
from database import db
from telegram_client import telegram_client
from tmdb_service import tmdb_service
from models.movie import (
    MovieResponse, MovieListResponse, BannerResponse,
    SearchResponse, StreamInfo, CategoryResponse
)

# Configure logging
logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s - %(name)s - %(levelname)s - %(message)s"
)
logger = logging.getLogger(__name__)


@asynccontextmanager
async def lifespan(app: FastAPI):
    """Application lifespan - startup and shutdown"""
    # Startup
    logger.info("🚀 Starting CineMax Backend...")
    
    # Create sessions directory
    os.makedirs("sessions", exist_ok=True)
    
    # Connect to services
    await db.connect()
    await telegram_client.start()
    await tmdb_service.start()
    
    # Sync movies on startup (background task)
    asyncio.create_task(sync_movies_from_telegram())
    
    logger.info("✅ CineMax Backend started successfully!")
    
    yield
    
    # Shutdown
    logger.info("Shutting down CineMax Backend...")
    await telegram_client.stop()
    await tmdb_service.stop()
    await db.disconnect()
    logger.info("CineMax Backend stopped")


app = FastAPI(
    title="CineMax API",
    description="Professional Movie Streaming Backend",
    version="1.0.0",
    lifespan=lifespan
)

# CORS Middleware for Android app
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)


# ============== Helper Functions ==============

async def sync_movies_from_telegram(limit: int = 200):
    """Sync movies from Telegram channel to database"""
    try:
        logger.info("Syncing movies from Telegram channel...")
        messages = await telegram_client.get_channel_messages(limit=limit)
        
        for msg in messages:
            # Check if movie already exists
            existing = await db.movies.find_one({
                "telegram_file_id": msg["telegram_file_id"]
            })
            
            if not existing:
                # Try to enrich with TMDB data
                tmdb_data = await tmdb_service.search_movie(
                    msg["title"],
                    year=msg.get("year")
                )
                
                movie_doc = {
                    "title": msg["title"],
                    "original_title": tmdb_data.get("original_title") if tmdb_data else None,
                    "year": msg.get("year") or (tmdb_data.get("year") if tmdb_data else None),
                    "quality": msg.get("quality", "HD"),
                    "size": msg.get("size"),
                    "size_bytes": msg.get("size_bytes", 0),
                    "duration_minutes": msg.get("duration_seconds", 0) // 60 if msg.get("duration_seconds") else None,
                    "genres": tmdb_data.get("genres", []) if tmdb_data else [],
                    "language": msg.get("language", "Hindi"),
                    "poster_url": tmdb_data.get("poster_url") if tmdb_data else None,
                    "backdrop_url": tmdb_data.get("backdrop_url") if tmdb_data else None,
                    "rating": tmdb_data.get("rating", 0) if tmdb_data else 0,
                    "plot": tmdb_data.get("plot") if tmdb_data else None,
                    "release_date": tmdb_data.get("release_date") if tmdb_data else None,
                    "tagline": tmdb_data.get("tagline") if tmdb_data else None,
                    "cast": tmdb_data.get("cast", []) if tmdb_data else [],
                    "trailer_url": tmdb_data.get("trailer_url") if tmdb_data else None,
                    "tmdb_id": tmdb_data.get("tmdb_id") if tmdb_data else None,
                    "telegram_file_id": msg["telegram_file_id"],
                    "telegram_message_id": msg["telegram_message_id"],
                    "mime_type": msg.get("mime_type", "video/mp4"),
                    "is_series": False,
                    "category": detect_category(msg["title"], msg.get("language")),
                    "views": 0,
                    "downloads": 0,
                    "created_at": datetime.utcnow(),
                    "message_date": msg.get("message_date")
                }
                
                await db.movies.insert_one(movie_doc)
                logger.info(f"Added movie: {msg['title']}")
                
                # Rate limiting to avoid TMDB limits
                await asyncio.sleep(0.5)
                
        logger.info(f"Sync complete! Processed {len(messages)} messages")
        
    except Exception as e:
        logger.error(f"Error syncing movies: {e}")


def detect_category(title: str, language: Optional[str]) -> str:
    """Detect movie category based on title and language"""
    title_lower = title.lower()
    
    if language:
        lang_lower = language.lower()
        if "hindi" in lang_lower or "bollywood" in title_lower:
            return "bollywood"
        if "tamil" in lang_lower or "kollywood" in title_lower:
            return "tamil"
        if "telugu" in lang_lower or "tollywood" in title_lower:
            return "telugu"
            
    # Check for series indicators
    series_patterns = ["season", "s01", "s02", "episode", "ep01", "web series"]
    if any(p in title_lower for p in series_patterns):
        return "series"
        
    return "hollywood"


# ============== API Endpoints ==============

@app.get("/")
async def root():
    """Health check endpoint"""
    return {
        "status": "online",
        "app": "CineMax API",
        "version": "1.0.0"
    }


@app.get("/api/movies", response_model=MovieListResponse)
async def get_movies(
    page: int = Query(1, ge=1),
    per_page: int = Query(20, ge=1, le=50),
    category: Optional[str] = None,
    sort_by: str = Query("created_at", regex="^(created_at|rating|views|title)$"),
    order: str = Query("desc", regex="^(asc|desc)$")
):
    """Get paginated list of movies"""
    skip = (page - 1) * per_page
    
    # Build query
    query = {}
    if category:
        query["category"] = category
        
    # Sort direction
    sort_dir = -1 if order == "desc" else 1
    
    # Execute query
    cursor = db.movies.find(query).sort(sort_by, sort_dir).skip(skip).limit(per_page)
    movies = await cursor.to_list(length=per_page)
    
    # Get total count
    total = await db.movies.count_documents(query)
    total_pages = (total + per_page - 1) // per_page
    
    # Convert ObjectId to string
    for movie in movies:
        movie["_id"] = str(movie["_id"])
    
    return MovieListResponse(
        movies=movies,
        total=total,
        page=page,
        per_page=per_page,
        total_pages=total_pages
    )


@app.get("/api/movies/trending")
async def get_trending_movies(limit: int = Query(10, ge=1, le=30)):
    """Get trending movies (by views)"""
    cursor = db.movies.find().sort("views", -1).limit(limit)
    movies = await cursor.to_list(length=limit)
    
    for movie in movies:
        movie["_id"] = str(movie["_id"])
    
    return {"movies": movies, "total": len(movies)}


@app.get("/api/movies/latest")
async def get_latest_movies(limit: int = Query(20, ge=1, le=50)):
    """Get latest added movies"""
    cursor = db.movies.find().sort("created_at", -1).limit(limit)
    movies = await cursor.to_list(length=limit)
    
    for movie in movies:
        movie["_id"] = str(movie["_id"])
    
    return {"movies": movies, "total": len(movies)}


@app.get("/api/movies/{movie_id}")
async def get_movie_by_id(movie_id: str):
    """Get movie details by ID"""
    from bson import ObjectId
    
    try:
        movie = await db.movies.find_one({"_id": ObjectId(movie_id)})
    except:
        movie = await db.movies.find_one({"telegram_file_id": movie_id})
    
    if not movie:
        raise HTTPException(status_code=404, detail="Movie not found")
    
    # Increment views
    await db.movies.update_one(
        {"_id": movie["_id"]},
        {"$inc": {"views": 1}}
    )
    
    movie["_id"] = str(movie["_id"])
    return movie


@app.get("/api/search")
async def search_movies(
    q: str = Query(..., min_length=1),
    page: int = Query(1, ge=1),
    per_page: int = Query(20, ge=1, le=50)
):
    """Search movies by title"""
    skip = (page - 1) * per_page
    
    # Text search
    query = {"$text": {"$search": q}}
    
    cursor = db.movies.find(query).skip(skip).limit(per_page)
    results = await cursor.to_list(length=per_page)
    
    total = await db.movies.count_documents(query)
    
    for movie in results:
        movie["_id"] = str(movie["_id"])
    
    return SearchResponse(
        query=q,
        results=results,
        total=total
    )


@app.get("/api/banners")
async def get_banners(limit: int = Query(5, ge=1, le=10)):
    """Get featured movie banners for carousel"""
    # Get high-rated movies with backdrop images
    cursor = db.movies.find({
        "backdrop_url": {"$ne": None},
        "rating": {"$gte": 6.0}
    }).sort("rating", -1).limit(limit)
    
    movies = await cursor.to_list(length=limit)
    
    banners = []
    for movie in movies:
        banners.append({
            "_id": str(movie["_id"]),
            "movie_id": str(movie["_id"]),
            "title": movie["title"],
            "backdrop_url": movie.get("backdrop_url"),
            "poster_url": movie.get("poster_url"),
            "tagline": movie.get("tagline") or movie.get("plot", "")[:100] + "..."
        })
    
    return {"banners": banners}


@app.get("/api/categories")
async def get_categories():
    """Get all movie categories with counts"""
    pipeline = [
        {"$group": {"_id": "$category", "count": {"$sum": 1}}},
        {"$sort": {"count": -1}}
    ]
    
    results = await db.movies.aggregate(pipeline).to_list(length=20)
    
    category_icons = {
        "hollywood": "🎬",
        "bollywood": "🎭",
        "tamil": "🎪",
        "telugu": "🎨",
        "series": "📺"
    }
    
    categories = []
    for r in results:
        cat_name = r["_id"] or "other"
        categories.append(CategoryResponse(
            name=cat_name.title(),
            slug=cat_name,
            count=r["count"],
            icon=category_icons.get(cat_name, "🎥")
        ))
    
    return {"categories": categories}


@app.get("/api/stream/{movie_id}")
async def stream_movie(movie_id: str, request: Request):
    """Stream movie with range request support"""
    from bson import ObjectId
    
    try:
        movie = await db.movies.find_one({"_id": ObjectId(movie_id)})
    except:
        movie = await db.movies.find_one({"telegram_file_id": movie_id})
    
    if not movie:
        raise HTTPException(status_code=404, detail="Movie not found")
    
    file_id = movie["telegram_file_id"]
    file_size = movie.get("size_bytes", 0)
    mime_type = movie.get("mime_type", "video/mp4")
    
    # Parse range header
    range_header = request.headers.get("Range")
    
    if range_header:
        # Parse range
        range_match = range_header.replace("bytes=", "").split("-")
        start = int(range_match[0]) if range_match[0] else 0
        end = int(range_match[1]) if len(range_match) > 1 and range_match[1] else file_size - 1
        
        content_length = end - start + 1
        
        async def stream_range():
            async for chunk in telegram_client.stream_file(file_id, offset=start):
                yield chunk
        
        return StreamingResponse(
            stream_range(),
            status_code=206,
            media_type=mime_type,
            headers={
                "Content-Range": f"bytes {start}-{end}/{file_size}",
                "Accept-Ranges": "bytes",
                "Content-Length": str(content_length)
            }
        )
    else:
        # Full file stream
        async def stream_full():
            async for chunk in telegram_client.stream_file(file_id):
                yield chunk
        
        return StreamingResponse(
            stream_full(),
            media_type=mime_type,
            headers={
                "Accept-Ranges": "bytes",
                "Content-Length": str(file_size)
            }
        )


@app.get("/api/download/{movie_id}")
async def download_movie(movie_id: str):
    """Get download information for a movie"""
    from bson import ObjectId
    
    try:
        movie = await db.movies.find_one({"_id": ObjectId(movie_id)})
    except:
        movie = await db.movies.find_one({"telegram_file_id": movie_id})
    
    if not movie:
        raise HTTPException(status_code=404, detail="Movie not found")
    
    # Increment download count
    await db.movies.update_one(
        {"_id": movie["_id"]},
        {"$inc": {"downloads": 1}}
    )
    
    return StreamInfo(
        movie_id=str(movie["_id"]),
        title=movie["title"],
        stream_url=f"/api/stream/{movie_id}",
        size_bytes=movie.get("size_bytes"),
        mime_type=movie.get("mime_type", "video/mp4")
    )


@app.post("/api/sync")
async def trigger_sync(limit: int = Query(100, ge=10, le=500)):
    """Manually trigger movie sync from Telegram"""
    asyncio.create_task(sync_movies_from_telegram(limit=limit))
    return {"message": "Sync started", "limit": limit}


# ============== Run Server ==============

if __name__ == "__main__":
    import uvicorn
    uvicorn.run(
        "main:app",
        host=HOST,
        port=PORT,
        reload=True,
        log_level="info"
    )
