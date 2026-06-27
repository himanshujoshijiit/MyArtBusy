import logging
import smtplib
from email.mime.text import MIMEText

from app.config import settings

logger = logging.getLogger(__name__)


class EmailService:
    def send(self, to_email: str, subject: str, body: str) -> bool:
        if not settings.smtp_host:
            logger.info("[EMAIL MOCK] To: %s | %s | %s", to_email, subject, body[:80])
            return True
        try:
            msg = MIMEText(body)
            msg["Subject"] = subject
            msg["From"] = settings.smtp_from
            msg["To"] = to_email
            with smtplib.SMTP(settings.smtp_host, settings.smtp_port) as server:
                if settings.smtp_user:
                    server.starttls()
                    server.login(settings.smtp_user, settings.smtp_password)
                server.send_message(msg)
            return True
        except Exception as e:
            logger.error("Email send failed: %s", e)
            return False
