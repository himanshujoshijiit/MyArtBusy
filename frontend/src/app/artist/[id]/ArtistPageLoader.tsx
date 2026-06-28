'use client';

import { useEffect, useState } from 'react';
import Link from 'next/link';
import { addDays, format } from 'date-fns';
import ArtistPageContent from './ArtistPageContent';
import { api, MuaProfile, Review, AvailabilitySlot } from '@/lib/api';

interface Props {
  artistId: string;
}

export default function ArtistPageLoader({ artistId }: Props) {
  const [artist, setArtist] = useState<MuaProfile | null>(null);
  const [reviews, setReviews] = useState<Review[]>([]);
  const [slots, setSlots] = useState<AvailabilitySlot[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(false);

  useEffect(() => {
    let cancelled = false;
    const start = format(new Date(), 'yyyy-MM-dd');
    const end = format(addDays(new Date(), 14), 'yyyy-MM-dd');

    Promise.all([
      api.muas.getById(artistId),
      api.reviews.getMua(artistId).catch(() => [] as Review[]),
      api.bookings.availability(artistId, start, end),
    ])
      .then(([a, r, s]) => {
        if (cancelled) return;
        setArtist(a);
        setReviews(r || []);
        setSlots((s || []).filter(slot => slot.available));
      })
      .catch(() => {
        if (!cancelled) setError(true);
      })
      .finally(() => {
        if (!cancelled) setLoading(false);
      });

    return () => { cancelled = true; };
  }, [artistId]);

  if (loading) {
    return <div className="py-20 text-center text-charcoal/50">Loading artist profile…</div>;
  }

  if (error || !artist) {
    return (
      <div className="mx-auto max-w-lg px-4 py-20 text-center">
        <h1 className="font-display mb-2 text-2xl font-bold">Artist not found</h1>
        <p className="mb-6 text-charcoal/60">This profile may be unavailable. Try searching again.</p>
        <Link href="/search" className="btn-primary">Browse Artists</Link>
      </div>
    );
  }

  return (
    <ArtistPageContent
      artistId={artistId}
      artist={artist}
      reviews={reviews}
      slots={slots}
    />
  );
}
