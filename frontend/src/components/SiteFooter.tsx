'use client';

import Link from 'next/link';

const LINKS = [
  { href: '/city/bengaluru', label: 'City pages' },
  { href: '/search', label: 'Near Me search' },
  { href: '/register?role=MUA', label: 'MUA onboarding' },
  { href: '/onboarding', label: 'Artist setup wizard' },
  { href: '/courses', label: 'Academy' },
  { href: '/login', label: 'Login (admin / client / MUA)' },
];

export default function SiteFooter() {
  return (
    <footer className="border-t border-charcoal/10 bg-cream px-4 py-10">
      <div className="mx-auto max-w-7xl sm:px-6">
        <div className="grid gap-8 sm:grid-cols-2 lg:grid-cols-3">
          <div>
            <p className="font-display text-lg font-bold">MakeupSeven</p>
            <p className="mt-2 text-sm text-charcoal/60">Book verified makeup artists in Bengaluru.</p>
          </div>
          <div>
            <p className="text-sm font-semibold">Explore</p>
            <ul className="mt-3 space-y-2 text-sm text-charcoal/70">
              {LINKS.map(l => (
                <li key={l.href}><Link href={l.href} className="hover:text-rose-600">{l.label}</Link></li>
              ))}
            </ul>
          </div>
          <div>
            <p className="text-sm font-semibold">Demo logins</p>
            <ul className="mt-3 space-y-1 text-xs text-charcoal/60 font-mono">
              <li>Client: demo@makeupseven.com / demo123</li>
              <li>MUA: priya@makeupseven.com / artist123</li>
              <li>Admin: admin@makeupseven.com / admin123</li>
            </ul>
            <p className="mt-3 text-xs text-charcoal/50">
              On any artist profile: Book Trial, Request Quote, Block dates in Dashboard → Calendar.
            </p>
          </div>
        </div>
      </div>
    </footer>
  );
}
