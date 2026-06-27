'use client';

import Link from 'next/link';
import { useAuth } from '@/lib/auth-context';
import { Sparkles, Menu, X, GraduationCap } from 'lucide-react';
import { useState } from 'react';

export default function Navbar() {
  const { user, logout } = useAuth();
  const [open, setOpen] = useState(false);

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
          <Link href="/courses" className="flex items-center gap-1 text-sm font-medium text-charcoal/70 hover:text-rose-600">
            <GraduationCap className="h-4 w-4" /> Academy
          </Link>
          {user?.role === 'MUA' && (
            <>
              <Link href="/dashboard" className="text-sm font-medium text-charcoal/70 hover:text-rose-600">Dashboard</Link>
              <Link href="/subscription" className="text-sm font-medium text-gold-600 hover:text-gold-700">Pro</Link>
            </>
          )}
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
            <Link href="/courses" onClick={() => setOpen(false)} className="py-2 text-sm font-medium">Academy</Link>
            {user?.role === 'MUA' && (
              <>
                <Link href="/dashboard" onClick={() => setOpen(false)} className="py-2 text-sm font-medium">Dashboard</Link>
                <Link href="/subscription" onClick={() => setOpen(false)} className="py-2 text-sm font-medium">Upgrade Pro</Link>
              </>
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
