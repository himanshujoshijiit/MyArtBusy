from fastapi import FastAPI, Request
from fastapi.middleware.cors import CORSMiddleware
from fastapi.responses import JSONResponse
from collections import defaultdict
import time

from app.routers import search, notifications, analytics, whatsapp_webhook

app = FastAPI(
    title="MakeupSeven Search & Intelligence API",
    description="Discovery engine, notifications, and analytics for MakeupSeven",
    version="1.1.0",
)

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# Simple in-memory rate limiter: 120 req/min per IP
_rate_buckets: dict[str, list[float]] = defaultdict(list)
RATE_LIMIT = 120
WINDOW = 60


@app.middleware("http")
async def rate_limit_middleware(request: Request, call_next):
    if request.url.path in ("/health", "/docs", "/openapi.json"):
        return await call_next(request)
    ip = request.client.host if request.client else "unknown"
    now = time.time()
    bucket = _rate_buckets[ip]
    _rate_buckets[ip] = [t for t in bucket if now - t < WINDOW]
    if len(_rate_buckets[ip]) >= RATE_LIMIT:
        return JSONResponse(status_code=429, content={"error": "Rate limit exceeded"})
    _rate_buckets[ip].append(now)
    return await call_next(request)


app.include_router(search.router, prefix="/api/search", tags=["Search"])
app.include_router(notifications.router, prefix="/api/notifications", tags=["Notifications"])
app.include_router(analytics.router, prefix="/api/analytics", tags=["Analytics"])
app.include_router(whatsapp_webhook.router, prefix="/api/whatsapp", tags=["WhatsApp"])


@app.get("/health")
def health():
    return {"status": "healthy", "service": "makeupseven-python", "version": "1.1.0"}
