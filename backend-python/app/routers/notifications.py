import logging
from fastapi import APIRouter
from pydantic import BaseModel

from app.config import settings
from app.services.whatsapp import WhatsAppService

router = APIRouter()
logger = logging.getLogger(__name__)
whatsapp = WhatsAppService()


class BookingNotification(BaseModel):
    booking_id: str
    client_name: str
    client_phone: str
    mua_name: str
    mua_phone: str
    date: str
    time: str
    amount: str


class ReviewNotification(BaseModel):
    booking_id: str
    client_name: str
    client_phone: str
    mua_name: str


@router.post("/booking-confirmation")
async def booking_confirmation(data: BookingNotification):
    client_msg = (
        f"✨ MakeupSeven Booking Confirmed!\n\n"
        f"Artist: {data.mua_name}\n"
        f"Date: {data.date}\n"
        f"Time: {data.time}\n"
        f"Amount: ₹{data.amount}\n\n"
        f"Your contract is ready. Reply HELP for support."
    )
    mua_msg = (
        f"📅 New Booking on MakeupSeven!\n\n"
        f"Client: {data.client_name}\n"
        f"Date: {data.date}\n"
        f"Time: {data.time}\n"
        f"Amount: ₹{data.amount}\n\n"
        f"Check your dashboard for details."
    )

    client_sent = await whatsapp.send_message(data.client_phone, client_msg)
    mua_sent = await whatsapp.send_message(data.mua_phone, mua_msg)

    logger.info(
        "Booking notification booking_id=%s client_sent=%s mua_sent=%s",
        data.booking_id, client_sent, mua_sent,
    )
    return {
        "status": "sent",
        "booking_id": data.booking_id,
        "client_whatsapp": client_sent,
        "mua_whatsapp": mua_sent,
        "mock": not bool(settings.whatsapp_api_key),
    }


@router.post("/review-request")
async def review_request(data: ReviewNotification):
    msg = (
        f"Hi {data.client_name}! 💄\n\n"
        f"How was your experience with {data.mua_name}?\n"
        f"Leave a verified review on MakeupSeven:\n\n"
        f"http://localhost:3000/review/{data.booking_id}"
    )
    sent = await whatsapp.send_message(data.client_phone, msg)
    return {"status": "sent", "booking_id": data.booking_id, "whatsapp": sent}
