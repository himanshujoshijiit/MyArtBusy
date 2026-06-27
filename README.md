# MakeupSeven

**Portfolio-first makeup artist discovery & booking platform** — built for Bengaluru launch, designed to scale globally.

📖 **[Full HTML Guide](docs/index.html)** — features, setup steps, demo accounts, API reference, deployment, troubleshooting (also at `GUIDE.html` in project root).

## Architecture

| Service | Tech | Port | Role |
|---------|------|------|------|
| **Frontend** | Next.js 14 + Tailwind | 3000 | Client & MUA UI |
| **Core API** | Java Spring Boot 3 | 8080 | Auth, bookings, payments, dashboard |
| **Search & Intelligence** | Python FastAPI | 8000 | Occasion-based search, WhatsApp, analytics |
| **Database** | PostgreSQL 16 | 5432 | Shared data store |

## Features (MVP — Launch Ready)

### Core 1: MUA Discovery
- Search by **city + occasion** (bridal Bengaluru, party Indiranagar)
- Portfolio gallery, services, pricing on each profile
- Filters: skin tone, budget, rating, availability

### Core 2: Booking & Payment
- Calendar-based availability slots
- **Razorpay** deposit (25% upfront) — mock mode works without keys
- Auto **WhatsApp** confirmation (mock logs when no API key)
- Contract URL generation on confirm

### Core 3: Reviews & Trust
- Verified reviews (completed bookings only)
- Top Artist badges (4.5+ rating, 5+ reviews)
- Response time indicator

### Core 4: MUA Business Dashboard
- Booking calendar & earnings tracker
- Client face profiles (skin tone, allergies)
- Kit inventory with expiry/low-stock alerts
- Auto review request after booking completion

## Quick Start (Deploy Today)

### Prerequisites
- [Docker Desktop](https://www.docker.com/products/docker-desktop/) installed and running

### 1. Clone & configure

```bash
cd MaB
cp .env.example .env
# Edit .env with Razorpay keys when ready (optional for demo)
```

### 2. Launch everything

```bash
docker compose up --build
```

Wait ~2-3 minutes for all services to start.

### 3. Open the app

| URL | Description |
|-----|-------------|
| http://localhost:3000 | **Main app** — start here |
| http://localhost:8080/actuator/health | Java API health |
| http://localhost:8000/health | Python search API |
| http://localhost:8000/docs | FastAPI Swagger docs |

## Demo Accounts

| Role | Email | Password |
|------|-------|----------|
| Client | demo@makeupseven.com | demo123 |
| MUA (Priya — Top Artist) | priya@makeupseven.com | artist123 |
| MUA (Ananya) | ananya@makeupseven.com | artist123 |

5 seeded MUAs in Bengaluru with portfolios, services, and availability.

## Production Deployment

### Option A: Single VPS (DigitalOcean / AWS EC2 / Hetzner)

```bash
# On your server
git clone <your-repo>
cd MaB
cp .env.example .env
# Set production values:
# JWT_SECRET=<256-bit random string>
# RAZORPAY_KEY_ID=rzp_live_xxx
# RAZORPAY_KEY_SECRET=xxx
# FRONTEND_URL=https://yourdomain.com
# NEXT_PUBLIC_API_URL=https://api.yourdomain.com
# NEXT_PUBLIC_SEARCH_URL=https://search.yourdomain.com

docker compose up -d --build
```

Put **nginx** or **Caddy** in front for HTTPS:

```nginx
server {
    listen 443 ssl;
    server_name makeupseven.com;
    location / { proxy_pass http://localhost:3000; }
}
server {
    listen 443 ssl;
    server_name api.makeupseven.com;
    location / { proxy_pass http://localhost:8080; }
}
```

### Option B: Cloud (scale globally)

- **Frontend**: Vercel or AWS Amplify (set `NEXT_PUBLIC_API_URL`)
- **Java API**: AWS ECS / Google Cloud Run / Railway
- **Python API**: Same — separate Cloud Run service
- **Database**: AWS RDS PostgreSQL / Supabase / Neon

## Environment Variables

| Variable | Required | Description |
|----------|----------|-------------|
| `JWT_SECRET` | Yes (prod) | Min 256-bit secret for JWT |
| `RAZORPAY_KEY_ID` | For payments | From Razorpay dashboard |
| `RAZORPAY_KEY_SECRET` | For payments | From Razorpay dashboard |
| `WHATSAPP_API_KEY` | For WhatsApp | Meta Cloud API token |
| `WHATSAPP_PHONE_ID` | For WhatsApp | Phone number ID |
| `FRONTEND_URL` | Yes (prod) | For CORS |

Without Razorpay keys, payments run in **mock mode** (auto-confirms deposit).

## Revenue Model (Built In)

- **Free tier**: 3 bookings/month (enforced in API)
- **Pro tier**: ₹999/month — unlimited bookings + dashboard
- **Commission**: 10% calculated on each booking
- **Deposit**: 25% upfront via Razorpay

## Go-To-Market Checklist

- [ ] Replace Priya Sharma seed profile with your wife's real portfolio images
- [ ] Set Razorpay live keys
- [ ] Connect WhatsApp Business API
- [ ] Point domain + SSL
- [ ] Onboard 20-30 Bengaluru MUAs via Instagram
- [ ] Run Google Ads: "bridal makeup artist Bengaluru"

## Phase 2 (Education & Community)

Stubs ready for:
- Online courses (70/30 revenue split)
- Featured brand ads in search results
- Multi-city expansion (city field + country code already in schema)

## Local Development (without Docker)

```bash
# Terminal 1 — PostgreSQL (or use Docker for postgres only)
docker compose up postgres -d

# Terminal 2 — Java
cd backend-java && mvn spring-boot:run

# Terminal 3 — Python
cd backend-python && pip install -r requirements.txt && uvicorn app.main:app --reload

# Terminal 4 — Frontend
cd frontend && npm install && npm run dev
```

## License

Proprietary — MakeupSeven © 2026
