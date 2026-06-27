'use client';

import { useEffect, useState } from 'react';
import { useParams, useRouter } from 'next/navigation';
import Image from 'next/image';
import Link from 'next/link';
import { api, MuaProfile, Review, AvailabilitySlot, formatPrice, formatOccasion } from '@/lib/api';
import { useAuth } from '@/lib/auth-context';
import { Star, MapPin, Clock, BadgeCheck, Crown, Calendar } from 'lucide-react';
import { format, addDays } from 'date-fns';

export default function ArtistPage() {
  const { id } = useParams<{ id: string }>();
  const router = useRouter();
  const { user } = useAuth();
  const [artist, setArtist] = useState<MuaProfile | null>(null);
  const [reviews, setReviews] = useState<Review[]>([]);
  const [slots, setSlots] = useState<AvailabilitySlot[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const start = format(new Date(), 'yyyy-MM-dd');
    const end = format(addDays(new Date(), 14), 'yyyy-MM-dd');
    Promise.all([
      api.muas.getById(id),
      api.reviews.getMua(id).catch(() => []),
      api.bookings.availability(id, start, end).catch(() => []),
    ]).then(([a, r, s]) => {
      setArtist(a);
      setReviews(r);
      setSlots(s.filter(slot => slot.available));
    }).finally(() => setLoading(false));
  }, [id]);

  if (loading) return <div className="mx-auto max-w-5xl px-4 py-20 text-center">Loading...</div>;
  if (!artist) return <div className="mx-auto max-w-5xl px-4 py-20 text-center">Artist not found</div>;

  const handleBook = (serviceId?: string) => {
    if (!user) {
      router.push(`/login?redirect=/artist/${id}/book${serviceId ? `?service=${serviceId}` : ''}`);
      return;
    }
    router.push(`/artist/${id}/book${serviceId ? `?service=${serviceId}` : ''}`);
  };

  return (
    <div className="mx-auto max-w-5xl px-4 py-10 sm:px-6">
      {/* Header */}
      <div className="mb-8 flex flex-col gap-6 sm:flex-row sm:items-start">
        <div className="flex-1">
          <div className="mb-3 flex flex-wrap gap-2">
            {artist.topArtist && <span className="badge-top flex items-center gap-1"><Crown className="h-3 w-3" /> Top Artist</span>}
            {artist.verified && <span className="badge-verified flex items-center gap-1"><BadgeCheck className="h-3 w-3" /> Verified</span>}
          </div>
          <h1 className="font-display mb-2 text-3xl font-bold sm:text-4xl">{artist.displayName}</h1>
          <div className="mb-4 flex flex-wrap items-center gap-4 text-sm text-charcoal/60">
            <span className="flex items-center gap-1"><MapPin className="h-4 w-4" />{artist.locality ? `${artist.locality}, ` : ''}{artist.city}</span>
            <span className="flex items-center gap-1"><Star className="h-4 w-4 fill-gold-400 text-gold-400" />{artist.rating} ({artist.reviewCount} reviews)</span>
            <span className="flex items-center gap-1"><Clock className="h-4 w-4" />{artist.responseTimeLabel}</span>
          </div>
          <p className="text-charcoal/70 leading-relaxed">{artist.bio}</p>
          <div className="mt-4 flex flex-wrap gap-2">
            {artist.occasions.map(o => (
              <span key={o} className="rounded-full bg-rose-50 px-3 py-1 text-xs font-medium text-rose-700">{formatOccasion(o)}</span>
            ))}
          </div>
          {artist.skinToneExpertise?.length > 0 && (
            <div className="mt-3">
              <p className="mb-1 text-xs font-medium text-charcoal/50">Skin tone expertise</p>
              <div className="flex flex-wrap gap-1">
                {artist.skinToneExpertise.map(s => (
                  <span key={s} className="rounded-full bg-charcoal/5 px-2 py-0.5 text-xs">{s}</span>
                ))}
              </div>
            </div>
          )}
        </div>
        <div className="card shrink-0 p-6 sm:w-72">
          <p className="mb-1 text-sm text-charcoal/50">Starting from</p>
          <p className="mb-4 text-2xl font-bold text-rose-600">{formatPrice(artist.minPrice)}</p>
          <button onClick={() => handleBook()} className="btn-primary w-full min-h-[44px]">
            <Calendar className="h-4 w-4" /> Book Now
          </button>
          <button onClick={() => router.push(`/artist/${id}/book?trial=1`)} className="btn-secondary mt-2 w-full min-h-[44px]">
            Book Trial Session (50% off)
          </button>
          <Link href={`/artist/${id}/quote`} className="mt-2 block text-center text-sm font-medium text-rose-600 hover:underline">
            Request a Custom Quote →
          </Link>
        </div>
      </div>

      {/* Availability preview */}
      {slots.length > 0 && (
        <section className="mb-10">
          <h2 className="font-display mb-4 text-2xl font-bold">Available Slots</h2>
          <div className="flex flex-wrap gap-2">
            {Array.from(new Set(slots.map(s => s.slotDate))).slice(0, 7).map(d => (
              <span key={d} className="rounded-xl bg-emerald-50 px-3 py-2 text-sm font-medium text-emerald-700">
                {format(new Date(d), 'EEE, MMM d')}
              </span>
            ))}
          </div>
          <p className="mt-2 text-xs text-charcoal/40">{slots.length} open slots in next 2 weeks</p>
        </section>
      )}

      {/* Portfolio */}
      <section className="mb-10">
        <h2 className="font-display mb-4 text-2xl font-bold">Portfolio</h2>
        <div className="grid grid-cols-2 gap-3 sm:grid-cols-3">
          {artist.portfolio.map(item => (
            <div key={item.id} className="group relative aspect-square overflow-hidden rounded-xl">
              <Image src={item.imageUrl} alt={item.caption || 'Portfolio'} fill className="object-cover transition group-hover:scale-105" sizes="33vw" />
              {item.occasion && (
                <span className="absolute bottom-2 left-2 rounded-full bg-black/50 px-2 py-0.5 text-xs text-white">
                  {formatOccasion(item.occasion)}
                </span>
              )}
            </div>
          ))}
        </div>
      </section>

      {/* Services */}
      <section className="mb-10">
        <h2 className="font-display mb-4 text-2xl font-bold">Services & Pricing</h2>
        <div className="space-y-3">
          {artist.services.map(s => (
            <div key={s.id} className="card flex items-center justify-between p-5">
              <div>
                <h3 className="font-semibold">{s.name}</h3>
                {s.description && <p className="mt-1 text-sm text-charcoal/50">{s.description}</p>}
                {s.durationMinutes && <p className="mt-1 text-xs text-charcoal/40">{s.durationMinutes} min</p>}
              </div>
              <div className="text-right">
                <p className="text-lg font-bold text-rose-600">{formatPrice(s.price)}</p>
                <button onClick={() => handleBook(s.id)} className="mt-2 text-sm font-medium text-rose-600 hover:underline">Book →</button>
              </div>
            </div>
          ))}
        </div>
      </section>

      {/* Reviews */}
      <section>
        <h2 className="font-display mb-4 text-2xl font-bold">Verified Reviews</h2>
        {reviews.length === 0 ? (
          <p className="text-charcoal/50">No reviews yet — be the first to book and review!</p>
        ) : (
          <div className="space-y-4">
            {reviews.map(r => (
              <div key={r.id} className="card p-5">
                <div className="mb-2 flex items-center justify-between">
                  <span className="font-medium">{r.clientName}</span>
                  <div className="flex items-center gap-1">
                    {Array.from({ length: 5 }).map((_, i) => (
                      <Star key={i} className={`h-4 w-4 ${i < r.rating ? 'fill-gold-400 text-gold-400' : 'text-charcoal/20'}`} />
                    ))}
                  </div>
                </div>
                {r.comment && <p className="text-sm text-charcoal/70">{r.comment}</p>}
                {r.verified && <span className="mt-2 inline-block text-xs text-emerald-600">✓ Verified booking</span>}
              </div>
            ))}
          </div>
        )}
      </section>
    </div>
  );
}
