import logging
from fastapi import APIRouter, Request, Response, HTTPException
from pydantic import BaseModel

from app.config import settings
from app.services.whatsapp import WhatsAppService

router = APIRouter()
logger = logging.getLogger(__name__)
whatsapp = WhatsAppService()

# In-memory conversation threads (production: use Redis/DB)
conversations: dict[str, list[dict]] = {}


class WebhookMessage(BaseModel):
    from_number: str
    body: str
    message_id: str = ""


@router.get("/webhook")
async def verify_webhook(request: Request):
    """Meta Cloud API webhook verification."""
    mode = request.query_params.get("hub.mode")
    token = request.query_params.get("hub.verify_token")
    challenge = request.query_params.get("hub.challenge")
    if mode == "subscribe" and token == settings.whatsapp_verify_token:
        return Response(content=challenge, media_type="text/plain")
    raise HTTPException(status_code=403, detail="Verification failed")


@router.post("/webhook")
async def receive_webhook(request: Request):
    """Handle incoming WhatsApp messages."""
    payload = await request.json()
    try:
        entry = payload.get("entry", [{}])[0]
        changes = entry.get("changes", [{}])[0]
        value = changes.get("value", {})
        messages = value.get("messages", [])
        for msg in messages:
            from_number = msg.get("from", "")
            text = msg.get("text", {}).get("body", "")
            msg_id = msg.get("id", "")
            thread = conversations.setdefault(from_number, [])
            thread.append({"role": "user", "body": text, "id": msg_id})
            logger.info("WhatsApp inbound from %s: %s", from_number, text[:100])

            if text.upper() in ("HELP", "HI", "HELLO"):
                reply = (
                    "Welcome to MakeupSeven! 💄\n"
                    "Book artists at makeupseven.com\n"
                    "Reply with your booking ID for status."
                )
                await whatsapp.send_message(from_number, reply)
                thread.append({"role": "bot", "body": reply})
    except Exception as e:
        logger.error("Webhook parse error: %s", e)
    return {"status": "ok"}


@router.get("/conversations/{phone}")
def get_conversation(phone: str):
    return conversations.get(phone, [])
