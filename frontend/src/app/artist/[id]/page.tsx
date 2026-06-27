import { notFound } from 'next/navigation';
import { addDays, format } from 'date-fns';
import ArtistPageContent from './ArtistPageContent';
import type { MuaProfile, Review, AvailabilitySlot } from '@/lib/api';

const API_URL = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080';

async function fetchJson<T>(url: string): Promise<T | null> {
  try {
    const res = await fetch(url, { next: { revalidate: 300 } });
    if (!res.ok) return null;
    return res.json();
  } catch {
    return null;
  }
}

export default async function ArtistPage({ params }: { params: { id: string } }) {
  const { id } = params;
  const start = format(new Date(), 'yyyy-MM-dd');
  const end = format(addDays(new Date(), 14), 'yyyy-MM-dd');

  const [artist, reviews, slots] = await Promise.all([
    fetchJson<MuaProfile>(`${API_URL}/api/muas/${id}`),
    fetchJson<Review[]>(`${API_URL}/api/reviews/mua/${id}`),
    fetchJson<AvailabilitySlot[]>(`${API_URL}/api/bookings/availability/${id}?start=${start}&end=${end}`),
  ]);

  if (!artist) notFound();

  const openSlots = (slots || []).filter(s => s.available);

  return (
    <ArtistPageContent
      artistId={id}
      artist={artist}
      reviews={reviews || []}
      slots={openSlots}
    />
  );
}
