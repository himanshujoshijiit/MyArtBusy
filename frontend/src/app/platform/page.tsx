import type { Metadata } from 'next';
import Link from 'next/link';
import SiteFooter from '@/components/SiteFooter';
import { PLATFORM_FEATURES, ARCHITECTURE_LAYERS, OWNER } from '@/lib/platform-config';
import { Layers, Sparkles, ArrowRight } from 'lucide-react';

export const metadata: Metadata = {
  title: 'Platform Features & Architecture',
  description: 'MakeupSeven platform overview — booking, payments, academy, dashboard, and system architecture.',
};

export default function PlatformPage() {
  return (
    <>
      <section className="bg-gradient-to-br from-rose-50 via-cream to-gold-50 px-4 py-16">
        <div className="mx-auto max-w-4xl text-center">
          <p className="mb-3 inline-flex items-center gap-2 rounded-full bg-white/80 px-4 py-1.5 text-sm font-medium text-rose-600 shadow-sm">
            <Sparkles className="h-4 w-4" /> MakeupSeven Platform
          </p>
          <h1 className="font-display text-3xl font-bold sm:text-4xl">Features & Architecture</h1>
          <p className="mx-auto mt-4 max-w-2xl text-charcoal/60">
            Full-stack makeup booking platform for Bengaluru — currently running in single-artist mode
            with <span className="font-semibold">Priya Prachi</span> as the studio owner.
          </p>
        </div>
      </section>

      <section className="mx-auto max-w-6xl px-4 py-12 sm:px-6">
        <h2 className="font-display mb-6 flex items-center gap-2 text-2xl font-bold">
          <Layers className="h-6 w-6 text-rose-500" /> System Architecture
        </h2>
        <div className="card overflow-x-auto p-6">
          <pre className="text-xs leading-relaxed text-charcoal/80 sm:text-sm">{`┌─────────────┐     ┌──────────────────┐     ┌─────────────────┐
│   Browser   │────▶│  Next.js Website │────▶│  Java Booking   │
│  (Client)   │     │  localhost:3000  │     │  API :8080      │
└─────────────┘     └────────┬─────────┘     └────────┬────────┘
                             │                         │
                             ▼                         ▼
                    ┌──────────────────┐     ┌─────────────────┐
                    │ Python Search &  │     │   PostgreSQL    │
                    │ Notifications    │     │   (all data)    │
                    │ :8000            │     └─────────────────┘
                    └──────────────────┘
                             │
                             ▼
                    WhatsApp / Email / Razorpay`}</pre>
        </div>
        <div className="mt-6 grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
          {ARCHITECTURE_LAYERS.map(layer => (
            <div key={layer.name} className="rounded-xl border border-charcoal/10 p-4">
              <p className="font-semibold">{layer.name}</p>
              <p className="text-xs font-medium text-rose-600">{layer.tech}</p>
              <p className="mt-2 text-sm text-charcoal/60">{layer.role}</p>
            </div>
          ))}
        </div>
      </section>

      <section className="bg-charcoal/5 px-4 py-12">
        <div className="mx-auto max-w-6xl sm:px-6">
          <h2 className="font-display mb-8 text-2xl font-bold">All Features</h2>
          <div className="grid gap-8 lg:grid-cols-2">
            {PLATFORM_FEATURES.map(section => (
              <div key={section.category}>
                <h3 className="mb-4 font-semibold text-rose-600">{section.category}</h3>
                <div className="space-y-3">
                  {section.items.map(item => (
                    <div key={item.title} className="card p-4">
                      <p className="font-medium">{item.title}</p>
                      <p className="mt-1 text-sm text-charcoal/60">{item.desc}</p>
                    </div>
                  ))}
                </div>
              </div>
            ))}
          </div>
        </div>
      </section>

      <section className="mx-auto max-w-4xl px-4 py-12 text-center sm:px-6">
        <h2 className="font-display mb-4 text-xl font-bold">Quick Links</h2>
        <div className="flex flex-wrap justify-center gap-3">
          <Link href="/search" className="btn-primary inline-flex items-center gap-2">Find Artists <ArrowRight className="h-4 w-4" /></Link>
          <Link href="/courses" className="btn-secondary">Academy Courses</Link>
          <Link href="/login" className="btn-secondary">Owner Login</Link>
        </div>
        <p className="mt-6 text-sm text-charcoal/50">
          Studio owner: {OWNER.email} / {OWNER.password}
        </p>
      </section>

      <SiteFooter />
    </>
  );
}
