from fastapi import APIRouter, Depends
from sqlalchemy.orm import Session
from sqlalchemy import text

from app.database import get_db

router = APIRouter()


@router.get("/top-artists/{city}")
def top_artists(city: str, db: Session = Depends(get_db)):
    sql = """
        SELECT m.id, m.display_name, m.rating, m.review_count, m.total_bookings,
               m.top_artist, m.locality
        FROM mua_profiles m
        WHERE LOWER(m.city) LIKE LOWER(:city) AND m.active = true
        ORDER BY m.top_artist DESC, m.rating DESC, m.total_bookings DESC
        LIMIT 10
    """
    rows = db.execute(text(sql), {"city": f"%{city}%"}).mappings().all()
    return [dict(r) for r in rows]


@router.get("/market-stats/{city}")
def market_stats(city: str, db: Session = Depends(get_db)):
    stats = db.execute(text("""
        SELECT COUNT(*) as total_artists,
               AVG(rating) as avg_rating,
               MIN(min_price) as price_floor,
               MAX(max_price) as price_ceiling,
               SUM(total_bookings) as total_bookings
        FROM mua_profiles
        WHERE LOWER(city) LIKE LOWER(:city) AND active = true
    """), {"city": f"%{city}%"}).mappings().first()

    occasion_breakdown = db.execute(text("""
        SELECT mo.occasion, COUNT(DISTINCT mo.mua_id) as count
        FROM mua_occasions mo
        JOIN mua_profiles m ON m.id = mo.mua_id
        WHERE LOWER(m.city) LIKE LOWER(:city) AND m.active = true
        GROUP BY mo.occasion
        ORDER BY count DESC
    """), {"city": f"%{city}%"}).mappings().all()

    return {
        "city": city,
        "stats": dict(stats) if stats else {},
        "occasion_breakdown": [dict(r) for r in occasion_breakdown],
    }
