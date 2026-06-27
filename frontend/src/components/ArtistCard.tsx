import { SearchResult } from '@/lib/api';
import { formatOccasion, formatPrice } from '@/lib/api';
import { Star, MapPin, Clock, BadgeCheck, Crown } from 'lucide-react';
import Link from 'next/link';
import Image from 'next/image';

interface Props {
  artist: SearchResult;
}

export default function ArtistCard({ artist }: Props) {
  const preview = artist.portfolio_preview[0] || 'https://images.unsplash.com/photo-1487412940907-5a55ae5e4c6b?w=600';

  return (
    <Link href={`/artist/${artist.id}`} className="card group overflow-hidden">
      <div className="relative aspect-[4/3] overflow-hidden">
        <Image
          src={preview}
          alt={artist.display_name}
          fill
          className="object-cover transition duration-500 group-hover:scale-105"
          sizes="(max-width:768px) 100vw, 33vw"
        />
        <div className="absolute inset-0 bg-gradient-to-t from-black/60 via-transparent to-transparent" />
        <div className="absolute bottom-3 left-3 right-3 flex flex-wrap gap-1.5">
          {artist.featured && (
            <span className="badge bg-rose-600 text-white">Sponsored</span>
          )}
          {artist.top_artist && (
            <span className="badge-top flex items-center gap-1">
              <Crown className="h-3 w-3" /> Top Artist
            </span>
          )}
          {artist.verified && (
            <span className="badge-verified flex items-center gap-1">
              <BadgeCheck className="h-3 w-3" /> Verified
            </span>
          )}
        </div>
      </div>

      <div className="p-5">
        <div className="mb-2 flex items-start justify-between gap-2">
          <h3 className="font-display text-lg font-semibold leading-tight group-hover:text-rose-600">
            {artist.display_name}
          </h3>
          <div className="flex shrink-0 items-center gap-1 rounded-full bg-rose-50 px-2 py-0.5">
            <Star className="h-3.5 w-3.5 fill-gold-400 text-gold-400" />
            <span className="text-sm font-semibold">{artist.rating.toFixed(1)}</span>
            <span className="text-xs text-charcoal/40">({artist.review_count})</span>
          </div>
        </div>

        <div className="mb-3 flex items-center gap-1 text-sm text-charcoal/60">
          <MapPin className="h-3.5 w-3.5" />
          {artist.locality ? `${artist.locality}, ` : ''}{artist.city}
        </div>

        <div className="mb-3 flex flex-wrap gap-1">
          {artist.occasions.slice(0, 3).map(o => (
            <span key={o} className="rounded-full bg-charcoal/5 px-2 py-0.5 text-xs text-charcoal/70">
              {formatOccasion(o)}
            </span>
          ))}
        </div>

        <div className="flex items-center justify-between border-t border-charcoal/5 pt-3">
          <span className="text-sm font-semibold text-rose-600">
            {formatPrice(artist.min_price)}
            {artist.max_price && artist.max_price !== artist.min_price && ` – ${formatPrice(artist.max_price)}`}
          </span>
          <span className="flex items-center gap-1 text-xs text-charcoal/50">
            <Clock className="h-3 w-3" />
            {artist.response_time_label?.replace('Usually replies ', '') || '2 hrs'}
          </span>
        </div>
      </div>
    </Link>
  );
}
