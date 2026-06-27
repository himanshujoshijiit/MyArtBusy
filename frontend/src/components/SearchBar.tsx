'use client';

import { useEffect, useState } from 'react';
import { Search, MapPin, Palette, Star, Calendar } from 'lucide-react';

interface Props {
  onSearch: (params: Record<string, string | number | boolean>) => void;
  loading?: boolean;
}

export default function SearchBar({ onSearch, loading }: Props) {
  const [city, setCity] = useState('Bengaluru');
  const [locality, setLocality] = useState('');
  const [occasion, setOccasion] = useState('');
  const [skinTone, setSkinTone] = useState('');
  const [minBudget, setMinBudget] = useState('');
  const [maxBudget, setMaxBudget] = useState('');
  const [minRating, setMinRating] = useState('');
  const [availableDate, setAvailableDate] = useState('');
  const [topArtistOnly, setTopArtistOnly] = useState(false);
  const [showFilters, setShowFilters] = useState(false);
  const [occasions, setOccasions] = useState<{ value: string; label: string }[]>([]);

  useEffect(() => {
    fetch(`${process.env.NEXT_PUBLIC_SEARCH_URL || 'http://localhost:8000'}/api/search/occasions`)
      .then(r => r.json())
      .then(setOccasions)
      .catch(() => setOccasions([
        { value: 'BRIDAL', label: 'Bridal' }, { value: 'PARTY', label: 'Party' },
        { value: 'WEDDING', label: 'Wedding' }, { value: 'EDITORIAL', label: 'Editorial' },
      ]));
  }, []);

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    const params: Record<string, string | number | boolean> = { city };
    if (locality) params.locality = locality;
    if (occasion) params.occasion = occasion;
    if (skinTone) params.skin_tone = skinTone;
    if (minBudget) params.min_budget = Number(minBudget);
    if (maxBudget) params.max_budget = Number(maxBudget);
    if (minRating) params.min_rating = Number(minRating);
    if (availableDate) params.available_date = availableDate;
    if (topArtistOnly) params.top_artist_only = true;
    onSearch(params);
  };

  return (
    <form onSubmit={handleSubmit} className="card mx-auto max-w-4xl p-2 shadow-elevated">
      <div className="flex flex-col gap-2 sm:flex-row sm:items-center">
        <div className="flex flex-1 items-center gap-2 rounded-xl bg-charcoal/5 px-4 py-3">
          <MapPin className="h-5 w-5 shrink-0 text-rose-500" />
          <input type="text" placeholder="City" value={city} onChange={e => setCity(e.target.value)}
            className="w-full bg-transparent text-sm outline-none placeholder:text-charcoal/40" />
        </div>
        <div className="flex flex-1 items-center gap-2 rounded-xl bg-charcoal/5 px-4 py-3">
          <Search className="h-5 w-5 shrink-0 text-rose-500" />
          <select value={occasion} onChange={e => setOccasion(e.target.value)} className="w-full bg-transparent text-sm outline-none">
            <option value="">All Occasions</option>
            {occasions.map(o => <option key={o.value} value={o.value}>{o.label}</option>)}
          </select>
        </div>
        <button type="submit" disabled={loading} className="btn-primary shrink-0">
          {loading ? 'Searching...' : 'Find Artists'}
        </button>
      </div>

      <div className="mt-2 flex flex-wrap items-center gap-4 px-2">
        <button type="button" onClick={() => setShowFilters(!showFilters)} className="text-xs font-medium text-rose-600 hover:underline">
          {showFilters ? 'Hide filters' : '+ Advanced filters'}
        </button>
        <label className="flex items-center gap-2 text-xs text-charcoal/60">
          <input type="checkbox" checked={topArtistOnly} onChange={e => setTopArtistOnly(e.target.checked)} className="rounded" />
          Top Artists only
        </label>
      </div>

      {showFilters && (
        <div className="mt-3 grid gap-3 border-t border-charcoal/5 px-2 pt-3 sm:grid-cols-2 lg:grid-cols-3">
          <input type="text" placeholder="Locality (Indiranagar)" value={locality} onChange={e => setLocality(e.target.value)} className="input-field !py-2" />
          <div className="flex items-center gap-2">
            <Palette className="h-4 w-4 shrink-0 text-charcoal/40" />
            <select value={skinTone} onChange={e => setSkinTone(e.target.value)} className="input-field !py-2">
              <option value="">Any Skin Tone</option>
              {['FAIR', 'LIGHT', 'MEDIUM', 'OLIVE', 'TAN', 'DEEP', 'DARK'].map(s => <option key={s} value={s}>{s}</option>)}
            </select>
          </div>
          <div className="flex items-center gap-2">
            <Star className="h-4 w-4 shrink-0 text-charcoal/40" />
            <select value={minRating} onChange={e => setMinRating(e.target.value)} className="input-field !py-2">
              <option value="">Any Rating</option>
              {[4.5, 4.0, 3.5].map(r => <option key={r} value={r}>{r}+ stars</option>)}
            </select>
          </div>
          <input type="number" placeholder="Min budget ₹" value={minBudget} onChange={e => setMinBudget(e.target.value)} className="input-field !py-2" />
          <input type="number" placeholder="Max budget ₹" value={maxBudget} onChange={e => setMaxBudget(e.target.value)} className="input-field !py-2" />
          <div className="flex items-center gap-2">
            <Calendar className="h-4 w-4 shrink-0 text-charcoal/40" />
            <input type="date" value={availableDate} onChange={e => setAvailableDate(e.target.value)} className="input-field !py-2" />
          </div>
        </div>
      )}
    </form>
  );
}
