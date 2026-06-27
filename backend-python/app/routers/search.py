from fastapi import APIRouter, Depends
from sqlalchemy.orm import Session
from sqlalchemy import text

from app.database import get_db
from app.models.schemas import SearchRequest, SearchResponse, MuaSearchResult

router = APIRouter()


def format_response_time(minutes: int | None) -> str:
    if not minutes or minutes <= 60:
        return "Usually replies within an hour"
    if minutes <= 120:
        return "Usually replies in 2 hours"
    if minutes <= 240:
        return "Usually replies in 4 hours"
    return "Usually replies within a day"


def compute_relevance(row: dict, req: SearchRequest) -> float:
    score = row["rating"] * 20
    if req.occasion and req.occasion.upper() in (row.get("occasions") or []):
        score += 30
    if req.skin_tone and req.skin_tone.upper() in (row.get("skin_tones") or []):
        score += 20
    if row.get("top_artist"):
        score += 15
    if row.get("featured"):
        score += 10
    if row.get("verified"):
        score += 5
    score += min(row.get("total_bookings", 0) / 10, 10)
    return round(score, 2)


@router.post("", response_model=SearchResponse)
def search_muas(req: SearchRequest, db: Session = Depends(get_db)):
    conditions = ["m.active = true"]
    params: dict = {}

    if req.city:
        conditions.append("LOWER(m.city) LIKE LOWER(:city)")
        params["city"] = f"%{req.city}%"
    if req.locality:
        conditions.append("LOWER(m.locality) LIKE LOWER(:locality)")
        params["locality"] = f"%{req.locality}%"
    if req.min_budget is not None:
        conditions.append("m.max_price >= :min_budget")
        params["min_budget"] = req.min_budget
    if req.max_budget is not None:
        conditions.append("m.min_price <= :max_budget")
        params["max_budget"] = req.max_budget
    if req.min_rating is not None:
        conditions.append("m.rating >= :min_rating")
        params["min_rating"] = req.min_rating
    if req.top_artist_only:
        conditions.append("m.top_artist = true")

    occasion_filter = ""
    if req.occasion:
        occasion_filter = """
            AND EXISTS (
                SELECT 1 FROM mua_occasions mo
                WHERE mo.mua_id = m.id AND UPPER(mo.occasion) = UPPER(:occasion)
            )
        """
        params["occasion"] = req.occasion

    skin_filter = ""
    if req.skin_tone:
        skin_filter = """
            AND EXISTS (
                SELECT 1 FROM mua_skin_tones ms
                WHERE ms.mua_id = m.id
                AND (UPPER(ms.skin_tone) = UPPER(:skin_tone) OR UPPER(ms.skin_tone) = 'ALL')
            )
        """
        params["skin_tone"] = req.skin_tone

    availability_filter = ""
    if req.available_date:
        availability_filter = """
            AND EXISTS (
                SELECT 1 FROM availability_slots av
                WHERE av.mua_id = m.id AND av.slot_date = :avail_date
                AND av.available = true AND av.booked = false
            )
        """
        params["avail_date"] = req.available_date

    order_map = {
        "rating": "m.rating DESC, m.review_count DESC",
        "price_low": "m.min_price ASC NULLS LAST",
        "price_high": "m.max_price DESC NULLS LAST",
        "bookings": "m.total_bookings DESC",
    }
    order = order_map.get(req.sort_by, order_map["rating"])

    where = " AND ".join(conditions)
    offset = (req.page - 1) * req.page_size

    count_sql = f"""
        SELECT COUNT(*) FROM mua_profiles m
        WHERE {where} {occasion_filter} {skin_filter} {availability_filter}
    """
    total = db.execute(text(count_sql), params).scalar() or 0

    sql = f"""
        SELECT m.id, m.display_name, m.bio, m.city, m.locality, m.country,
               m.min_price, m.max_price, m.rating, m.review_count, m.total_bookings,
               m.top_artist, m.verified, m.featured, m.response_time_minutes
        FROM mua_profiles m
        WHERE {where} {occasion_filter} {skin_filter} {availability_filter}
        ORDER BY {order}
        LIMIT :limit OFFSET :offset
    """
    params["limit"] = req.page_size
    params["offset"] = offset

    rows = db.execute(text(sql), params).mappings().all()
    results = []

    for row in rows:
        mua_id = str(row["id"])
        occasions = [
            r[0] for r in db.execute(
                text("SELECT occasion FROM mua_occasions WHERE mua_id = :id"),
                {"id": mua_id},
            ).fetchall()
        ]
        skin_tones = [
            r[0] for r in db.execute(
                text("SELECT skin_tone FROM mua_skin_tones WHERE mua_id = :id"),
                {"id": mua_id},
            ).fetchall()
        ]
        portfolio = [
            r[0] for r in db.execute(
                text("SELECT image_url FROM portfolio_items WHERE mua_id = :id ORDER BY sort_order LIMIT 4"),
                {"id": mua_id},
            ).fetchall()
        ]
        row_dict = {**dict(row), "occasions": occasions, "skin_tones": skin_tones}
        results.append(MuaSearchResult(
            id=mua_id,
            display_name=row["display_name"],
            bio=row["bio"],
            city=row["city"],
            locality=row["locality"],
            country=row["country"],
            occasions=occasions,
            skin_tone_expertise=skin_tones,
            min_price=float(row["min_price"]) if row["min_price"] else None,
            max_price=float(row["max_price"]) if row["max_price"] else None,
            rating=float(row["rating"] or 0),
            review_count=row["review_count"] or 0,
            total_bookings=row["total_bookings"] or 0,
            top_artist=bool(row["top_artist"]),
            verified=bool(row["verified"]),
            featured=bool(row["featured"]),
            response_time_label=format_response_time(row["response_time_minutes"]),
            portfolio_preview=portfolio,
            relevance_score=compute_relevance(row_dict, req),
        ))

    if req.sort_by == "rating":
        results.sort(key=lambda x: x.relevance_score, reverse=True)

    parts = []
    if req.city:
        parts.append(req.city)
    if req.locality:
        parts.append(req.locality)
    if req.occasion:
        parts.append(req.occasion.replace("_", " ").title())
    summary = " · ".join(parts) if parts else "All artists"

    return SearchResponse(
        results=results,
        total=total,
        page=req.page,
        page_size=req.page_size,
        query_summary=summary,
    )


@router.get("/cities")
def list_cities(db: Session = Depends(get_db)):
    rows = db.execute(
        text("SELECT DISTINCT city, country FROM mua_profiles WHERE active = true ORDER BY city")
    ).fetchall()
    return [{"city": r[0], "country": r[1]} for r in rows]


@router.get("/occasions")
def list_occasions():
    return [
        {"value": "BRIDAL", "label": "Bridal"},
        {"value": "WEDDING", "label": "Wedding"},
        {"value": "PARTY", "label": "Party"},
        {"value": "EDITORIAL", "label": "Editorial"},
        {"value": "FILM", "label": "Film & TV"},
        {"value": "PERSONAL_EVENT", "label": "Personal Event"},
        {"value": "ENGAGEMENT", "label": "Engagement"},
        {"value": "RECEPTION", "label": "Reception"},
    ]
