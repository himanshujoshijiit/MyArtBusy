'use client';

import Link from 'next/link';
import { MapPin } from 'lucide-react';

const AREAS = [
  { slug: 'bengaluru', label: 'All Bengaluru' },
  { slug: 'indiranagar', label: 'Indiranagar' },
  { slug: 'koramangala', label: 'Koramangala' },
  { slug: 'jayanagar', label: 'Jayanagar' },
  { slug: 'whitefield', label: 'Whitefield' },
];

export default function CityLinks() {
  return (
    <section className="border-b border-charcoal/5 bg-white py-6">
      <div className="mx-auto max-w-7xl px-4 sm:px-6">
        <p className="mb-3 flex items-center gap-2 text-sm font-medium text-charcoal/50">
          <MapPin className="h-4 w-4" /> Browse by area
        </p>
        <div className="flex flex-wrap gap-2">
          {AREAS.map(a => (
            <Link
              key={a.slug}
              href={`/city/${a.slug}`}
              className="rounded-full border border-charcoal/10 bg-cream px-4 py-2 text-sm font-medium text-charcoal/80 transition hover:border-rose-300 hover:bg-rose-50 hover:text-rose-700"
            >
              {a.label}
            </Link>
          ))}
        </div>
      </div>
    </section>
  );
}
