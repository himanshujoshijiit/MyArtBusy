from pydantic_settings import BaseSettings


class Settings(BaseSettings):
    database_url: str = "postgresql://makeupseven:makeupseven_dev@localhost:5432/makeupseven"
    java_api_url: str = "http://localhost:8080"
    frontend_url: str = "http://localhost:3000"
    whatsapp_api_key: str = ""
    whatsapp_phone_id: str = ""
    whatsapp_verify_token: str = "makeupseven_webhook_verify"
    smtp_host: str = ""
    smtp_port: int = 587
    smtp_user: str = ""
    smtp_password: str = ""
    smtp_from: str = "noreply@makeupseven.com"

    class Config:
        env_file = ".env"


settings = Settings()
