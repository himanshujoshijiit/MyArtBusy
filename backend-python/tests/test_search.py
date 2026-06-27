import pytest
from app.routers.search import haversine_km, compute_relevance
from app.models.schemas import SearchRequest


def test_haversine_same_point():
    assert haversine_km(12.97, 77.59, 12.97, 77.59) == 0.0


def test_haversine_bengaluru_distance():
    d = haversine_km(12.9784, 77.6408, 12.9352, 77.6245)
    assert 4 < d < 7


def test_relevance_score():
    row = {"rating": 4.5, "occasions": ["BRIDAL"], "top_artist": True, "featured": False, "verified": True, "total_bookings": 50}
    req = SearchRequest(occasion="BRIDAL")
    score = compute_relevance(row, req)
    assert score > 100
