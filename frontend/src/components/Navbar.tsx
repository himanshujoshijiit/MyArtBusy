'use client';

import Link from 'next/link';
import { useAuth } from '@/lib/auth-context';
import { Sparkles, Menu, X, GraduationCap } from 'lucide-react';
import { useState } from 'react';
import { getLocale, setLocale } from '@/lib/i18n';

export default function Navbar() {
  const { user, logout } = useAuth();
  const [open, setOpen] = useState(false);

  const toggleLocale = () => {
    const next = getLocale() === 'en' ? 'kn' : 'en';
    setLocale(next);
    window.location.reload();
  };

  return (
    <nav className="sticky top-0 z-50 border-b border-charcoal/5 bg-white/90 backdrop-blur-md">
      <div className="mx-auto flex max-w-7xl items-center justify-between px-4 py-4 sm:px-6">
        <Link href="/" className="flex items-center gap-2">
          <Sparkles className="h-6 w-6 text-rose-500" />
          <span className="font-display text-xl font-bold tracking-tight">
            Makeup<span className="text-rose-600">Seven</span>
          </span>
        </Link>

        <div className="hidden items-center gap-5 md:flex">
          <Link href="/search" className="text-sm font-medium text-charcoal/70 hover:text-rose-600">Find Artists</Link>
          <div className="group relative">
            <button type="button" className="text-sm font-medium text-charcoal/70 hover:text-rose-600">Areas ▾</button>
            <div className="invisible absolute left-0 top-full z-50 mt-1 min-w-[160px] rounded-xl border border-charcoal/10 bg-white py-2 opacity-0 shadow-lg transition group-hover:visible group-hover:opacity-100">
              {['bengaluru', 'indiranagar', 'koramangala', 'jayanagar', 'whitefield'].map(slug => (
                <Link key={slug} href={`/city/${slug}`} className="block px-4 py-2 text-sm capitalize hover:bg-rose-50 hover:text-rose-600">
                  {slug.replace('-', ' ')}
                </Link>
              ))}
            </div>
          </div>
          <Link href="/courses" className="flex items-center gap-1 text-sm font-medium text-charcoal/70 hover:text-rose-600">
            <GraduationCap className="h-4 w-4" /> Academy
          </Link>
          <Link href="/platform" className="text-sm font-medium text-charcoal/70 hover:text-rose-600">Features</Link>
          {user?.role === 'MUA' && (
            <>
              <Link href="/dashboard" className="text-sm font-medium text-charcoal/70 hover:text-rose-600">Dashboard</Link>
              <Link href="/onboarding" className="text-sm font-medium text-charcoal/70 hover:text-rose-600">Setup Profile</Link>
              <Link href="/subscription" className="text-sm font-medium text-gold-600 hover:text-gold-700">Pro</Link>
            </>
          )}
          {user?.role === 'ADMIN' && (
            <Link href="/admin" className="text-sm font-medium text-charcoal/70 hover:text-rose-600">Admin</Link>
          )}
          <button type="button" onClick={toggleLocale} className="text-xs font-medium text-charcoal/50 hover:text-rose-600">
            {getLocale() === 'en' ? 'ಕನ್ನಡ' : 'EN'}
          </button>
          {user ? (
            <div className="flex items-center gap-4">
              <Link href="/bookings" className="text-sm font-medium text-charcoal/70 hover:text-rose-600">Bookings</Link>
              <span className="text-sm text-charcoal/50">{user.fullName}</span>
              <button onClick={logout} className="btn-secondary !py-2 !px-4 text-xs">Logout</button>
            </div>
          ) : (
            <div className="flex items-center gap-3">
              <Link href="/login" className="text-sm font-medium text-charcoal/70 hover:text-rose-600">Login</Link>
              <Link href="/register" className="btn-primary !py-2 !px-5 text-xs">Get Started</Link>
            </div>
          )}
        </div>

        <button className="md:hidden" onClick={() => setOpen(!open)} aria-label="Menu">
          {open ? <X className="h-6 w-6" /> : <Menu className="h-6 w-6" />}
        </button>
      </div>

      {open && (
        <div className="border-t border-charcoal/5 bg-white px-4 py-4 md:hidden">
          <div className="flex flex-col gap-3">
            <Link href="/search" onClick={() => setOpen(false)} className="py-2 text-sm font-medium">Find Artists</Link>
            <p className="pt-2 text-xs font-semibold uppercase text-charcoal/40">Areas</p>
            {['bengaluru', 'indiranagar', 'koramangala', 'jayanagar', 'whitefield'].map(slug => (
              <Link key={slug} href={`/city/${slug}`} onClick={() => setOpen(false)} className="py-1 pl-2 text-sm capitalize text-charcoal/70">
                {slug}
              </Link>
            ))}
            <Link href="/courses" onClick={() => setOpen(false)} className="py-2 text-sm font-medium">Academy</Link>
            <Link href="/platform" onClick={() => setOpen(false)} className="py-2 text-sm font-medium">Features</Link>
            {user?.role === 'MUA' && (
              <>
                <Link href="/dashboard" onClick={() => setOpen(false)} className="py-2 text-sm font-medium">Dashboard</Link>
                <Link href="/onboarding" onClick={() => setOpen(false)} className="py-2 text-sm font-medium">Setup Profile</Link>
                <Link href="/subscription" onClick={() => setOpen(false)} className="py-2 text-sm font-medium">Upgrade Pro</Link>
              </>
            )}
            {user?.role === 'ADMIN' && (
              <Link href="/admin" onClick={() => setOpen(false)} className="py-2 text-sm font-medium">Admin</Link>
            )}
            {user ? (
              <>
                <Link href="/bookings" onClick={() => setOpen(false)} className="py-2 text-sm font-medium">Bookings</Link>
                <button onClick={() => { logout(); setOpen(false); }} className="py-2 text-left text-sm text-rose-600">Logout</button>
              </>
            ) : (
              <>
                <Link href="/login" onClick={() => setOpen(false)} className="py-2 text-sm font-medium">Login</Link>
                <Link href="/register" onClick={() => setOpen(false)} className="btn-primary text-center">Get Started</Link>
              </>
            )}
          </div>
        </div>
      )}
    </nav>
  );
}
