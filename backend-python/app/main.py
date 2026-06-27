from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware

from app.routers import search, notifications, analytics

app = FastAPI(
    title="MakeupSeven Search & Intelligence API",
    description="Discovery engine, notifications, and analytics for MakeupSeven",
    version="1.0.0",
)

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

app.include_router(search.router, prefix="/api/search", tags=["Search"])
app.include_router(notifications.router, prefix="/api/notifications", tags=["Notifications"])
app.include_router(analytics.router, prefix="/api/analytics", tags=["Analytics"])


@app.get("/health")
def health():
    return {"status": "healthy", "service": "makeupseven-python"}
