'use client';

import Link from 'next/link';

const LINKS = [
  { href: '/city/bengaluru', label: 'City pages' },
  { href: '/search', label: 'Near Me search' },
  { href: '/register?role=MUA', label: 'MUA onboarding' },
  { href: '/onboarding', label: 'Artist setup wizard' },
  { href: '/courses', label: 'Academy' },
  { href: '/platform', label: 'Features & architecture' },
];

const LEGAL = [
  { href: '/legal/privacy', label: 'Privacy Policy' },
  { href: '/legal/terms', label: 'Terms of Service' },
  { href: '/legal/refund', label: 'Refund & Cancellation' },
];

export default function SiteFooter() {
  return (
    <footer className="border-t border-charcoal/10 bg-cream px-4 py-10">
      <div className="mx-auto max-w-7xl sm:px-6">
        <div className="grid gap-8 sm:grid-cols-2 lg:grid-cols-4">
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
            <p className="text-sm font-semibold">Legal</p>
            <ul className="mt-3 space-y-2 text-sm text-charcoal/70">
              {LEGAL.map(l => (
                <li key={l.href}><Link href={l.href} className="hover:text-rose-600">{l.label}</Link></li>
              ))}
            </ul>
            <p className="mt-3 text-xs text-charcoal/50">
              Required for Razorpay KYC and DPDP Act compliance.
            </p>
          </div>
          <div>
            <p className="text-sm font-semibold">Support</p>
            <p className="mt-3 text-sm text-charcoal/70">
              <a href="mailto:support@makeupseven.com" className="hover:text-rose-600">support@makeupseven.com</a>
            </p>
            <p className="mt-2 text-xs text-charcoal/50">
              Bengaluru, Karnataka, India
            </p>
          </div>
        </div>
        <p className="mt-8 text-center text-xs text-charcoal/40">
          © {new Date().getFullYear()} MakeupSeven. All rights reserved.
        </p>
      </div>
    </footer>
  );
}
