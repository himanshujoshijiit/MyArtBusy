import logging
import httpx

from app.config import settings

logger = logging.getLogger(__name__)


class WhatsAppService:
    async def send_message(self, phone: str, message: str) -> bool:
        if not settings.whatsapp_api_key or not settings.whatsapp_phone_id:
            logger.info("[MOCK WhatsApp] To: %s | %s", phone, message[:80])
            return True

        phone = phone.lstrip("+").replace(" ", "")
        if not phone.startswith("91") and len(phone) == 10:
            phone = "91" + phone

        url = f"https://graph.facebook.com/v18.0/{settings.whatsapp_phone_id}/messages"
        headers = {
            "Authorization": f"Bearer {settings.whatsapp_api_key}",
            "Content-Type": "application/json",
        }
        payload = {
            "messaging_product": "whatsapp",
            "to": phone,
            "type": "text",
            "text": {"body": message},
        }
        try:
            async with httpx.AsyncClient() as client:
                resp = await client.post(url, json=payload, headers=headers, timeout=10)
                resp.raise_for_status()
                return True
        except Exception as e:
            logger.error("WhatsApp send failed: %s", e)
            return False
