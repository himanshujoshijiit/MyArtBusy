import logging
from fastapi import APIRouter
from pydantic import BaseModel

from app.config import settings
from app.services.whatsapp import WhatsAppService
from app.services.email import EmailService

router = APIRouter()
logger = logging.getLogger(__name__)
whatsapp = WhatsAppService()
email = EmailService()


class BookingNotification(BaseModel):
    booking_id: str
    client_name: str
    client_phone: str
    client_email: str = ""
    mua_name: str
    mua_phone: str
    date: str
    time: str
    amount: str


class ReviewNotification(BaseModel):
    booking_id: str
    client_name: str
    client_phone: str
    client_email: str = ""
    mua_name: str
    frontend_url: str = ""


class ReminderNotification(BaseModel):
    booking_id: str
    client_name: str
    client_phone: str
    client_email: str = ""
    mua_name: str
    date: str
    time: str


class QuoteNotification(BaseModel):
    quote_id: str
    client_name: str
    mua_phone: str
    mua_name: str
    details: str = ""


class QuoteResponseNotification(BaseModel):
    quote_id: str
    client_phone: str
    client_email: str = ""
    mua_name: str
    quoted_amount: str


def review_url(booking_id: str, frontend_url: str = "") -> str:
    base = frontend_url or settings.frontend_url
    return f"{base}/review/{booking_id}"


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
        f"Amount: ₹{data.amount}"
    )
    client_sent = await whatsapp.send_message(data.client_phone, client_msg)
    mua_sent = await whatsapp.send_message(data.mua_phone, mua_msg)
    if data.client_email:
        email.send(data.client_email, "Booking Confirmed — MakeupSeven", client_msg)
    return {"status": "sent", "booking_id": data.booking_id, "client_whatsapp": client_sent, "mua_whatsapp": mua_sent}


@router.post("/review-request")
async def review_request(data: ReviewNotification):
    url = review_url(data.booking_id, data.frontend_url)
    msg = (
        f"Hi {data.client_name}! 💄\n\n"
        f"How was your experience with {data.mua_name}?\n"
        f"Leave a verified review:\n{url}"
    )
    sent = await whatsapp.send_message(data.client_phone, msg)
    if data.client_email:
        email.send(data.client_email, "Share your review — MakeupSeven", msg)
    return {"status": "sent", "booking_id": data.booking_id, "whatsapp": sent}


@router.post("/appointment-reminder")
async def appointment_reminder(data: ReminderNotification):
    msg = (
        f"Reminder: Your makeup appointment with {data.mua_name} is tomorrow!\n"
        f"📅 {data.date} at {data.time}\n\nSee you soon! — MakeupSeven"
    )
    sent = await whatsapp.send_message(data.client_phone, msg)
    if data.client_email:
        email.send(data.client_email, "Appointment Tomorrow — MakeupSeven", msg)
    return {"status": "sent", "booking_id": data.booking_id, "whatsapp": sent}


@router.post("/quote-request")
async def quote_request(data: QuoteNotification):
    msg = (
        f"💬 New quote request on MakeupSeven!\n\n"
        f"Client: {data.client_name}\n"
        f"Details: {data.details}\n\nCheck your dashboard to respond."
    )
    sent = await whatsapp.send_message(data.mua_phone, msg)
    return {"status": "sent", "quote_id": data.quote_id, "whatsapp": sent}


@router.post("/quote-response")
async def quote_response(data: QuoteResponseNotification):
    msg = (
        f"Hi! {data.mua_name} sent you a quote: ₹{data.quoted_amount}\n"
        f"View details on MakeupSeven dashboard."
    )
    sent = await whatsapp.send_message(data.client_phone, msg)
    if data.client_email:
        email.send(data.client_email, f"Quote from {data.mua_name}", msg)
    return {"status": "sent", "quote_id": data.quote_id, "whatsapp": sent}
