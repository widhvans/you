"""
TMDB Service - Movie Metadata Enrichment
"""

import httpx
from config import TMDB_API_KEY, TMDB_BASE_URL, TMDB_IMAGE_BASE
from typing import Optional, Dict, List
import logging
import asyncio
from functools import lru_cache

logger = logging.getLogger(__name__)


class TMDBService:
    """Service for fetching movie metadata from TMDB"""
    
    def __init__(self):
        self.api_key = TMDB_API_KEY
        self.base_url = TMDB_BASE_URL
        self.image_base = TMDB_IMAGE_BASE
        self.client: Optional[httpx.AsyncClient] = None
        
    async def start(self):
        """Initialize HTTP client"""
        self.client = httpx.AsyncClient(
            timeout=30.0,
            headers={"Accept": "application/json"}
        )
        logger.info("TMDB service started")
        
    async def stop(self):
        """Close HTTP client"""
        if self.client:
            await self.client.aclose()
            logger.info("TMDB service stopped")
            
    def _get_image_url(self, path: Optional[str], size: str = "w500") -> Optional[str]:
        """Generate full image URL from path"""
        if path:
            return f"{self.image_base}/{size}{path}"
        return None
        
    async def search_movie(
        self, 
        query: str, 
        year: Optional[int] = None,
        language: str = "en-US"
    ) -> Optional[Dict]:
        """Search for a movie and return best match"""
        if not self.api_key:
            logger.warning("TMDB API key not configured")
            return None
            
        try:
            params = {
                "api_key": self.api_key,
                "query": query,
                "language": language,
                "include_adult": False
            }
            if year:
                params["year"] = year
                
            response = await self.client.get(
                f"{self.base_url}/search/movie",
                params=params
            )
            response.raise_for_status()
            data = response.json()
            
            if data.get("results"):
                # Return best match (first result)
                movie = data["results"][0]
                return await self.get_movie_details(movie["id"])
                
        except Exception as e:
            logger.error(f"TMDB search error: {e}")
            
        return None
        
    async def get_movie_details(self, movie_id: int) -> Optional[Dict]:
        """Get detailed movie information"""
        if not self.api_key:
            return None
            
        try:
            params = {
                "api_key": self.api_key,
                "language": "en-US",
                "append_to_response": "credits,videos,similar"
            }
            
            response = await self.client.get(
                f"{self.base_url}/movie/{movie_id}",
                params=params
            )
            response.raise_for_status()
            movie = response.json()
            
            # Extract cast (top 10)
            cast = []
            if movie.get("credits", {}).get("cast"):
                for actor in movie["credits"]["cast"][:10]:
                    cast.append({
                        "name": actor["name"],
                        "character": actor.get("character", ""),
                        "profile_url": self._get_image_url(actor.get("profile_path"), "w185")
                    })
            
            # Extract similar movies (top 6)
            similar = []
            if movie.get("similar", {}).get("results"):
                for sim in movie["similar"]["results"][:6]:
                    similar.append({
                        "id": sim["id"],
                        "title": sim["title"],
                        "poster_url": self._get_image_url(sim.get("poster_path"), "w342"),
                        "rating": sim.get("vote_average", 0)
                    })
            
            # Extract trailer
            trailer_url = None
            if movie.get("videos", {}).get("results"):
                for video in movie["videos"]["results"]:
                    if video["type"] == "Trailer" and video["site"] == "YouTube":
                        trailer_url = f"https://www.youtube.com/watch?v={video['key']}"
                        break
            
            return {
                "tmdb_id": movie["id"],
                "title": movie["title"],
                "original_title": movie.get("original_title"),
                "tagline": movie.get("tagline"),
                "plot": movie.get("overview"),
                "release_date": movie.get("release_date"),
                "year": int(movie["release_date"][:4]) if movie.get("release_date") else None,
                "rating": round(movie.get("vote_average", 0), 1),
                "vote_count": movie.get("vote_count", 0),
                "popularity": movie.get("popularity", 0),
                "runtime": movie.get("runtime"),
                "genres": [g["name"] for g in movie.get("genres", [])],
                "poster_url": self._get_image_url(movie.get("poster_path"), "w500"),
                "backdrop_url": self._get_image_url(movie.get("backdrop_path"), "original"),
                "poster_url_small": self._get_image_url(movie.get("poster_path"), "w342"),
                "backdrop_url_small": self._get_image_url(movie.get("backdrop_path"), "w780"),
                "cast": cast,
                "similar": similar,
                "trailer_url": trailer_url,
                "imdb_id": movie.get("imdb_id"),
                "budget": movie.get("budget", 0),
                "revenue": movie.get("revenue", 0),
                "status": movie.get("status"),
                "production_countries": [c["name"] for c in movie.get("production_countries", [])]
            }
            
        except Exception as e:
            logger.error(f"TMDB details error: {e}")
            
        return None
        
    async def get_trending_movies(
        self, 
        time_window: str = "week",
        page: int = 1
    ) -> List[Dict]:
        """Get trending movies"""
        if not self.api_key:
            return []
            
        try:
            params = {
                "api_key": self.api_key,
                "page": page
            }
            
            response = await self.client.get(
                f"{self.base_url}/trending/movie/{time_window}",
                params=params
            )
            response.raise_for_status()
            data = response.json()
            
            movies = []
            for movie in data.get("results", []):
                movies.append({
                    "tmdb_id": movie["id"],
                    "title": movie["title"],
                    "poster_url": self._get_image_url(movie.get("poster_path"), "w342"),
                    "backdrop_url": self._get_image_url(movie.get("backdrop_path"), "w780"),
                    "rating": round(movie.get("vote_average", 0), 1),
                    "release_date": movie.get("release_date"),
                    "plot": movie.get("overview")
                })
                
            return movies
            
        except Exception as e:
            logger.error(f"TMDB trending error: {e}")
            
        return []
        
    async def get_genres(self) -> List[Dict]:
        """Get movie genre list"""
        if not self.api_key:
            return []
            
        try:
            params = {
                "api_key": self.api_key,
                "language": "en-US"
            }
            
            response = await self.client.get(
                f"{self.base_url}/genre/movie/list",
                params=params
            )
            response.raise_for_status()
            data = response.json()
            
            return data.get("genres", [])
            
        except Exception as e:
            logger.error(f"TMDB genres error: {e}")
            
        return []


# Singleton instance
tmdb_service = TMDBService()
