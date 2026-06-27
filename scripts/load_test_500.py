#!/usr/bin/env python3
"""
MakeupSeven load test — 500 virtual users, mixed use cases.

Usage:
  python scripts/load_test_500.py
  python scripts/load_test_500.py --users 500 --concurrency 40

Requires: httpx (pip install httpx)
"""

from __future__ import annotations

import argparse
import asyncio
import os
import random
import statistics
import time
import uuid
from dataclasses import dataclass, field
from datetime import date, timedelta
from typing import Any

import httpx

API = os.environ.get("LOAD_TEST_API", "http://localhost:8080")
SEARCH = os.environ.get("LOAD_TEST_SEARCH", "http://localhost:8000")
FRONTEND = os.environ.get("LOAD_TEST_FRONTEND", "http://localhost:3000")

OCCASIONS = ["BRIDAL", "PARTY", "HALDI_MEHENDI", "GLAMOROUS", "WEDDING", "ENGAGEMENT"]
LOCALITIES = ["Koramangala", "Indiranagar", "HSR Layout", "Whitefield", "Jayanagar", "MG Road"]
SALON = ["threading", "waxing", "facial"]


@dataclass
class Result:
    scenario: str
    step: str
    ok: bool
    status: int
    ms: float
    error: str = ""


@dataclass
class Stats:
    results: list[Result] = field(default_factory=list)

    def add(self, r: Result) -> None:
        self.results.append(r)

    def report(self) -> str:
        total = len(self.results)
        ok = sum(1 for r in self.results if r.ok)
        failed = total - ok
        latencies = [r.ms for r in self.results if r.ok]
        by_scenario: dict[str, list[Result]] = {}
        for r in self.results:
            by_scenario.setdefault(r.scenario, []).append(r)

        lines = [
            "",
            "=" * 60,
            "MAKEUPSEVEN LOAD TEST REPORT",
            "=" * 60,
            f"Total requests : {total}",
            f"Success        : {ok} ({100 * ok / total:.1f}%)" if total else "Total requests : 0",
            f"Failed         : {failed}",
        ]
        if latencies:
            latencies.sort()
            p50 = latencies[len(latencies) // 2]
            p95 = latencies[int(len(latencies) * 0.95)]
            p99 = latencies[int(len(latencies) * 0.99)]
            lines += [
                f"Avg latency    : {statistics.mean(latencies):.0f} ms",
                f"p50 latency    : {p50:.0f} ms",
                f"p95 latency    : {p95:.0f} ms",
                f"p99 latency    : {p99:.0f} ms",
            ]

        status_counts: dict[int, int] = {}
        for r in self.results:
            if not r.ok:
                status_counts[r.status] = status_counts.get(r.status, 0) + 1
        if status_counts:
            lines.append("\nFailure status codes:")
            for code, cnt in sorted(status_counts.items()):
                lines.append(f"  HTTP {code}: {cnt}")

        lines.append("\nBy scenario:")
        for name, rs in sorted(by_scenario.items()):
            s_ok = sum(1 for r in rs if r.ok)
            lines.append(f"  {name:28} {s_ok}/{len(rs)} ok")

        errors = [r for r in self.results if not r.ok and r.error][:10]
        if errors:
            lines.append("\nSample errors:")
            for e in errors:
                lines.append(f"  [{e.scenario}/{e.step}] {e.status}: {e.error[:120]}")

        lines.append("=" * 60)
        return "\n".join(lines)


stats = Stats()
mua_ids: list[str] = []
demo_client_token: str | None = None
demo_mua_token: str | None = None
admin_token: str | None = None


async def timed(
    client: httpx.AsyncClient,
    scenario: str,
    step: str,
    method: str,
    url: str,
    **kwargs: Any,
) -> httpx.Response | None:
    t0 = time.perf_counter()
    try:
        resp = await client.request(method, url, timeout=30.0, **kwargs)
        ms = (time.perf_counter() - t0) * 1000
        ok = resp.status_code < 400
        err = "" if ok else resp.text[:200]
        stats.add(Result(scenario, step, ok, resp.status_code, ms, err))
        return resp
    except Exception as exc:
        ms = (time.perf_counter() - t0) * 1000
        stats.add(Result(scenario, step, False, 0, ms, str(exc)[:200]))
        return None


async def setup(client: httpx.AsyncClient) -> None:
    global mua_ids, demo_client_token, demo_mua_token, admin_token

    r = await timed(client, "setup", "list_muas", "GET", f"{API}/api/muas")
    if r and r.status_code == 200:
        mua_ids = [m["id"] for m in r.json()[:10]]

    r = await timed(client, "setup", "demo_client_login", "POST", f"{API}/api/auth/login", json={
        "email": "demo@makeupseven.com", "password": "demo123",
    })
    if r and r.status_code == 200:
        demo_client_token = r.json().get("token")

    r = await timed(client, "setup", "demo_mua_login", "POST", f"{API}/api/auth/login", json={
        "email": "priya@makeupseven.com", "password": "artist123",
    })
    if r and r.status_code == 200:
        demo_mua_token = r.json().get("token")

    r = await timed(client, "setup", "admin_login", "POST", f"{API}/api/auth/login", json={
        "email": "admin@makeupseven.com", "password": "admin123",
    })
    if r and r.status_code == 200:
        admin_token = r.json().get("token")


# --- Scenarios (500 users distributed) ---

async def scenario_anonymous_browser(client: httpx.AsyncClient, uid: int) -> None:
    s = "anonymous_browser"
    await timed(client, s, "frontend_home", "GET", FRONTEND)
    await timed(client, s, "list_muas", "GET", f"{API}/api/muas")
    await timed(client, s, "top_artists", "GET", f"{API}/api/muas/top")
    await timed(client, s, "search_occasions", "GET", f"{SEARCH}/api/search/occasions")
    if mua_ids:
        await timed(client, s, "artist_profile", "GET", f"{API}/api/muas/{random.choice(mua_ids)}")
    await timed(client, s, "search_bridal", "POST", f"{SEARCH}/api/search", json={
        "city": "Bengaluru", "occasion": random.choice(OCCASIONS), "page_size": 20,
    })


async def scenario_new_client_register(client: httpx.AsyncClient, uid: int) -> None:
    s = "new_client_register"
    email = f"loadtest{uid}_{uuid.uuid4().hex[:8]}@test.makeupseven.com"
    r = await timed(client, s, "register", "POST", f"{API}/api/auth/register", json={
        "email": email,
        "password": "testpass123",
        "fullName": f"Load Test Client {uid}",
        "phone": f"98{uid:08d}"[-10:],
        "role": "CLIENT",
    })
    if not r or r.status_code >= 400:
        return
    token = r.json().get("token")
    headers = {"Authorization": f"Bearer {token}"}
    await timed(client, s, "my_bookings", "GET", f"{API}/api/bookings/my", headers=headers)
    if mua_ids:
        mua = random.choice(mua_ids)
        await timed(client, s, "availability", "GET",
                    f"{API}/api/bookings/availability/{mua}",
                    params={"start": str(date.today()), "end": str(date.today() + timedelta(days=14))})
        await timed(client, s, "create_booking", "POST", f"{API}/api/bookings", headers=headers, json={
            "muaId": mua,
            "bookingDate": str(date.today() + timedelta(days=random.randint(3, 20))),
            "startTime": "10:00:00",
            "occasion": random.choice(OCCASIONS),
            "bookingType": "STANDARD",
        })


async def scenario_search_power_user(client: httpx.AsyncClient, uid: int) -> None:
    s = "search_power_user"
    searches = [
        {"city": "Bengaluru", "occasion": "BRIDAL", "min_rating": 4.0},
        {"city": "Bengaluru", "locality": random.choice(LOCALITIES), "occasion": "HALDI_MEHENDI"},
        {"city": "Bengaluru", "latitude": 12.9352, "longitude": 77.6245, "sort_by": "distance", "radius_km": 10},
        {"city": "Bengaluru", "service_category": "SALON", "salon_service": random.choice(SALON)},
        {"city": "Bengaluru", "pincode": "560034", "max_budget": 15000},
    ]
    for i, body in enumerate(searches):
        await timed(client, s, f"search_{i}", "POST", f"{SEARCH}/api/search", json=body)
    await timed(client, s, "cities", "GET", f"{SEARCH}/api/search/cities")
    await timed(client, s, "localities", "GET", f"{SEARCH}/api/search/localities/Bengaluru")


async def scenario_quote_requester(client: httpx.AsyncClient, uid: int) -> None:
    s = "quote_requester"
    if not demo_client_token or not mua_ids:
        return
    headers = {"Authorization": f"Bearer {demo_client_token}"}
    await timed(client, s, "create_quote", "POST", f"{API}/api/quotes", headers=headers, json={
        "muaId": random.choice(mua_ids),
        "occasion": random.choice(OCCASIONS),
        "eventDate": str(date.today() + timedelta(days=30)),
        "details": f"Bridal package for {uid} guests, need HD makeup",
        "budgetMin": 8000,
        "budgetMax": 25000,
    })
    await timed(client, s, "my_quotes", "GET", f"{API}/api/quotes/my", headers=headers)


async def scenario_mua_dashboard(client: httpx.AsyncClient, uid: int) -> None:
    s = "mua_dashboard"
    if not demo_mua_token:
        return
    headers = {"Authorization": f"Bearer {demo_mua_token}"}
    await timed(client, s, "dashboard_stats", "GET", f"{API}/api/dashboard/stats", headers=headers)
    await timed(client, s, "mua_bookings", "GET", f"{API}/api/bookings/mua", headers=headers)
    await timed(client, s, "mua_quotes", "GET", f"{API}/api/quotes/mua", headers=headers)
    await timed(client, s, "kit_items", "GET", f"{API}/api/dashboard/kit", headers=headers)
    await timed(client, s, "block_date", "POST", f"{API}/api/blocked-dates", headers=headers, json={
        "blockDate": str(date.today() + timedelta(days=100 + uid)),
        "reason": "Personal",
    })


async def scenario_otp_login(client: httpx.AsyncClient, uid: int) -> None:
    s = "otp_login"
    phone = f"91{9000000000 + uid}"
    await timed(client, s, "send_otp", "POST", f"{API}/api/auth/otp/send", json={"phone": phone})
    # Demo: OTP logged to console; verify will fail without real code — expected
    await timed(client, s, "verify_otp_fail", "POST", f"{API}/api/auth/otp/verify", json={
        "phone": phone, "code": "000000", "fullName": f"OTP User {uid}",
    })


async def scenario_referral_courses(client: httpx.AsyncClient, uid: int) -> None:
    s = "referral_courses"
    if not demo_client_token:
        return
    headers = {"Authorization": f"Bearer {demo_client_token}"}
    await timed(client, s, "my_referral", "GET", f"{API}/api/referrals/my", headers=headers)
    await timed(client, s, "list_courses", "GET", f"{API}/api/courses")
    r = await timed(client, s, "courses_detail", "GET", f"{API}/api/courses")
    if r and r.status_code == 200 and r.json():
        cid = r.json()[0]["id"]
        await timed(client, s, "enroll_course", "POST", f"{API}/api/courses/{cid}/enroll", headers=headers)


async def scenario_admin(client: httpx.AsyncClient, uid: int) -> None:
    s = "admin_panel"
    if not admin_token:
        return
    headers = {"Authorization": f"Bearer {admin_token}"}
    await timed(client, s, "admin_stats", "GET", f"{API}/api/admin/stats", headers=headers)
    await timed(client, s, "admin_muas", "GET", f"{API}/api/admin/muas", headers=headers)


def build_user_plan(total: int) -> list[tuple[str, int]]:
    """Distribute 500 users across use cases."""
    plan: list[tuple[str, int]] = []
    counts = {
        "anonymous_browser": int(total * 0.20),      # 100 — browse without login
        "new_client_register": int(total * 0.35),    # 175 — register + book
        "search_power_user": int(total * 0.25),      # 125 — heavy search
        "quote_requester": int(total * 0.08),        # 40
        "mua_dashboard": int(total * 0.05),         # 25
        "otp_login": int(total * 0.04),             # 20
        "referral_courses": int(total * 0.02),      # 10
        "admin_panel": max(1, int(total * 0.01)),    # 5
    }
    assigned = sum(counts.values())
    counts["anonymous_browser"] += total - assigned
    for name, cnt in counts.items():
        plan.extend([(name, i) for i in range(cnt)])
    random.shuffle(plan)
    return plan


SCENARIOS = {
    "anonymous_browser": scenario_anonymous_browser,
    "new_client_register": scenario_new_client_register,
    "search_power_user": scenario_search_power_user,
    "quote_requester": scenario_quote_requester,
    "mua_dashboard": scenario_mua_dashboard,
    "otp_login": scenario_otp_login,
    "referral_courses": scenario_referral_courses,
    "admin_panel": scenario_admin,
}


async def run_user(client: httpx.AsyncClient, name: str, uid: int, sem: asyncio.Semaphore) -> None:
    async with sem:
        fn = SCENARIOS.get(name)
        if fn:
            await fn(client, uid)
        await asyncio.sleep(random.uniform(0.1, 0.5))


async def main(users: int, concurrency: int) -> None:
    print(f"Starting load test: {users} users, concurrency={concurrency}")
    print(f"API={API}  SEARCH={SEARCH}  FRONTEND={FRONTEND}\n")

    t0 = time.perf_counter()
    plan = build_user_plan(users)
    sem = asyncio.Semaphore(concurrency)

    async with httpx.AsyncClient() as client:
        await setup(client)
        if not mua_ids:
            print("WARNING: No MUAs found — some scenarios will skip booking/quote steps")

        tasks = [run_user(client, name, i, sem) for i, (name, _) in enumerate(plan)]
        await asyncio.gather(*tasks)

    elapsed = time.perf_counter() - t0
    print(stats.report())
    print(f"\nTotal wall time: {elapsed:.1f}s")
    print(f"Throughput: {len(stats.results) / elapsed:.1f} req/s")

    ok_rate = sum(1 for r in stats.results if r.ok) / max(len(stats.results), 1)
    if ok_rate >= 0.85:
        print("\n✅ PASS — ≥85% success rate for local load test")
    elif ok_rate >= 0.70:
        print("\n⚠️  WARN — 70–85% success (rate limits or demo data limits expected locally)")
    else:
        print("\n❌ FAIL — <70% success; investigate errors before going public")


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description="MakeupSeven 500-user load test")
    parser.add_argument("--users", type=int, default=500)
    parser.add_argument("--concurrency", type=int, default=40,
                        help="Max parallel users (keep ≤50 locally to reduce 429s)")
    args = parser.parse_args()
    asyncio.run(main(args.users, args.concurrency))
