'use client';

import { useEffect, useState } from 'react';
import Link from 'next/link';
import SearchBar from '@/components/SearchBar';
import ArtistCard from '@/components/ArtistCard';
import CityLinks from '@/components/CityLinks';
import SiteFooter from '@/components/SiteFooter';
import { api, SearchResult, MuaProfile } from '@/lib/api';
import { Sparkles, Shield, Calendar, Star, Crown } from 'lucide-react';

export default function HomePage() {
  const [artists, setArtists] = useState<SearchResult[]>([]);
  const [topArtists, setTopArtists] = useState<MuaProfile[]>([]);
  const [loading, setLoading] = useState(true);

  const search = async (params: Record<string, string | number | boolean> = { city: 'Bengaluru' }) => {
    setLoading(true);
    try {
      const res = await api.search.query(params);
      setArtists(res.results);
    } catch {
      setArtists([]);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    search();
    api.muas.getTop().then(setTopArtists).catch(() => {});
  }, []);

  return (
    <>
      <section className="relative overflow-hidden bg-gradient-to-br from-rose-50 via-cream to-gold-50 px-4 py-20 sm:py-28">
        <div className="absolute -right-20 -top-20 h-80 w-80 rounded-full bg-rose-200/30 blur-3xl" />
        <div className="relative mx-auto max-w-4xl text-center">
          <p className="mb-4 inline-flex items-center gap-2 rounded-full bg-white/80 px-4 py-1.5 text-sm font-medium text-rose-600 shadow-sm">
            <Sparkles className="h-4 w-4" /> Now live in Bengaluru
          </p>
          <h1 className="font-display mb-6 text-4xl font-bold leading-tight sm:text-5xl lg:text-6xl">
            Book makeup artists<br />
            <span className="bg-gradient-to-r from-rose-600 to-gold-500 bg-clip-text text-transparent">
              by style, not price
            </span>
          </h1>
          <p className="mx-auto mb-10 max-w-2xl text-lg text-charcoal/60">
            Search by occasion — bridal, party, editorial, film. Browse portfolios, read verified reviews, and book with confidence.
          </p>
          <SearchBar onSearch={search} loading={loading} />
        </div>
      </section>

      <CityLinks />

      <section className="border-b border-charcoal/5 bg-white py-8">
        <div className="mx-auto flex max-w-4xl flex-wrap items-center justify-center gap-8 px-4 text-sm text-charcoal/60">
          <span className="flex items-center gap-2"><Shield className="h-5 w-5 text-emerald-500" /> Verified reviews only</span>
          <span className="flex items-center gap-2"><Calendar className="h-5 w-5 text-rose-500" /> Real-time availability</span>
          <span className="flex items-center gap-2"><Star className="h-5 w-5 text-gold-500" /> Top Artist badges</span>
          <Link href="/search" className="flex items-center gap-2 text-rose-600 hover:underline">Near Me search →</Link>
        </div>
      </section>

      {topArtists.length > 0 && (
        <section className="mx-auto max-w-7xl px-4 py-12 sm:px-6">
          <div className="mb-6 flex items-center gap-2">
            <Crown className="h-5 w-5 text-gold-500" />
            <h2 className="font-display text-xl font-bold">Top Artists in Bengaluru</h2>
          </div>
          <div className="flex gap-4 overflow-x-auto pb-2">
            {topArtists.slice(0, 5).map(a => (
              <Link key={a.id} href={`/artist/${a.id}`} className="card shrink-0 w-48 p-4 transition hover:border-rose-200">
                <p className="font-semibold leading-tight">{a.displayName}</p>
                <p className="mt-1 text-sm text-charcoal/50">{a.locality}</p>
                <p className="mt-2 flex items-center gap-1 text-sm">
                  <Star className="h-3.5 w-3.5 fill-gold-400 text-gold-400" />{a.rating} · {a.reviewCount} reviews
                </p>
              </Link>
            ))}
          </div>
        </section>
      )}

      <section className="mx-auto max-w-7xl px-4 py-12 sm:px-6">
        <div className="mb-8 flex items-end justify-between">
          <div>
            <h2 className="font-display text-2xl font-bold sm:text-3xl">
              {loading ? 'Finding artists...' : `${artists.length} Artists Found`}
            </h2>
            <p className="mt-1 text-charcoal/50">Portfolio-first discovery — book the look you love</p>
          </div>
          <Link href="/search" className="hidden text-sm font-medium text-rose-600 hover:underline sm:block">Advanced search →</Link>
        </div>

        {loading ? (
          <div className="grid gap-6 sm:grid-cols-2 lg:grid-cols-3">
            {[1, 2, 3, 4, 5, 6].map(i => <div key={i} className="card h-96 animate-pulse bg-charcoal/5" />)}
          </div>
        ) : (
          <div className="grid gap-6 sm:grid-cols-2 lg:grid-cols-3">
            {artists.map(a => <ArtistCard key={a.id} artist={a} />)}
          </div>
        )}
      </section>

      <section className="bg-charcoal px-4 py-16 text-white">
        <div className="mx-auto grid max-w-4xl gap-8 sm:grid-cols-2">
          <div className="text-center sm:text-left">
            <h2 className="font-display mb-4 text-2xl font-bold">Are you a makeup artist?</h2>
            <p className="mb-6 text-white/70">3 free bookings/month. Pro at ₹999 for unlimited bookings + dashboard.</p>
            <Link href="/register?role=MUA" className="btn-gold">List Your Profile — Free</Link>
          </div>
          <div className="text-center sm:text-left">
            <h2 className="font-display mb-4 text-2xl font-bold">Learn makeup artistry</h2>
            <p className="mb-6 text-white/70">Online courses from top Bengaluru artists. 70% goes to instructors.</p>
            <Link href="/courses" className="btn-secondary !border-white/30 !text-white hover:!bg-white/10">Browse Academy</Link>
          </div>
        </div>
      </section>

      <SiteFooter />
    </>
  );
}
