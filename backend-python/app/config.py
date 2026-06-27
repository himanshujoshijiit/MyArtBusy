from pydantic_settings import BaseSettings


class Settings(BaseSettings):
    database_url: str = "postgresql://makeupseven:makeupseven_dev@localhost:5432/makeupseven"
    java_api_url: str = "http://localhost:8080"
    whatsapp_api_key: str = ""
    whatsapp_phone_id: str = ""

    class Config:
        env_file = ".env"


settings = Settings()
