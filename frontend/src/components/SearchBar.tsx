'use client';

import { useEffect, useState } from 'react';
import { Search, MapPin, Palette, Star, Calendar, Navigation } from 'lucide-react';
import { t, getLocale, type Locale } from '@/lib/i18n';

interface Props {
  onSearch: (params: Record<string, string | number | boolean>) => void;
  loading?: boolean;
}

export default function SearchBar({ onSearch, loading }: Props) {
  const [locale, setLocaleState] = useState<Locale>('en');
  const [city, setCity] = useState('Bengaluru');
  const [locality, setLocality] = useState('');
  const [pincode, setPincode] = useState('');
  const [occasion, setOccasion] = useState('');
  const [skinTone, setSkinTone] = useState('');
  const [minBudget, setMinBudget] = useState('');
  const [maxBudget, setMaxBudget] = useState('');
  const [minRating, setMinRating] = useState('');
  const [availableDate, setAvailableDate] = useState('');
  const [topArtistOnly, setTopArtistOnly] = useState(false);
  const [showFilters, setShowFilters] = useState(false);
  const [nearMe, setNearMe] = useState(false);
  const [coords, setCoords] = useState<{ lat: number; lng: number } | null>(null);
  const [occasions, setOccasions] = useState<{ value: string; label: string }[]>([]);

  useEffect(() => {
    setLocaleState(getLocale());
    fetch(`${process.env.NEXT_PUBLIC_SEARCH_URL || 'http://localhost:8000'}/api/search/occasions`)
      .then(r => r.json())
      .then(setOccasions)
      .catch(() => setOccasions([
        { value: 'BRIDAL', label: 'Bridal' }, { value: 'PARTY', label: 'Party' },
        { value: 'WEDDING', label: 'Wedding' }, { value: 'EDITORIAL', label: 'Editorial' },
      ]));
  }, []);

  const useMyLocation = () => {
    if (!navigator.geolocation) return;
    navigator.geolocation.getCurrentPosition(
      pos => {
        setCoords({ lat: pos.coords.latitude, lng: pos.coords.longitude });
        setNearMe(true);
      },
      () => alert('Location access denied')
    );
  };

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    const params: Record<string, string | number | boolean> = { city };
    if (locality) params.locality = locality;
    if (pincode) params.pincode = pincode;
    if (occasion) params.occasion = occasion;
    if (skinTone) params.skin_tone = skinTone;
    if (minBudget) params.min_budget = Number(minBudget);
    if (maxBudget) params.max_budget = Number(maxBudget);
    if (minRating) params.min_rating = Number(minRating);
    if (availableDate) params.available_date = availableDate;
    if (topArtistOnly) params.top_artist_only = true;
    if (nearMe && coords) {
      params.latitude = coords.lat;
      params.longitude = coords.lng;
      params.sort_by = 'distance';
      params.radius_km = 15;
    }
    onSearch(params);
  };

  return (
    <form onSubmit={handleSubmit} className="card mx-auto max-w-4xl p-2 shadow-elevated">
      <div className="flex flex-col gap-2 sm:flex-row sm:items-center">
        <div className="flex flex-1 items-center gap-2 rounded-xl bg-charcoal/5 px-4 py-3">
          <MapPin className="h-5 w-5 shrink-0 text-rose-500" />
          <input type="text" placeholder={t('search.city', locale)} value={city} onChange={e => setCity(e.target.value)}
            className="w-full bg-transparent text-sm outline-none placeholder:text-charcoal/40" />
        </div>
        <div className="flex flex-1 items-center gap-2 rounded-xl bg-charcoal/5 px-4 py-3">
          <Search className="h-5 w-5 shrink-0 text-rose-500" />
          <select value={occasion} onChange={e => setOccasion(e.target.value)} className="w-full bg-transparent text-sm outline-none">
            <option value="">{t('search.occasion', locale)}</option>
            {occasions.map(o => <option key={o.value} value={o.value}>{o.label}</option>)}
          </select>
        </div>
        <button type="button" onClick={useMyLocation}
          className={`flex shrink-0 items-center gap-1 rounded-xl px-3 py-3 text-sm font-medium ${nearMe ? 'bg-rose-500 text-white' : 'bg-charcoal/5 text-charcoal/70'}`}>
          <Navigation className="h-4 w-4" /> {t('search.nearMe', locale)}
        </button>
        <button type="submit" disabled={loading} className="btn-primary shrink-0 min-h-[44px]">
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
          <input type="text" placeholder={t('search.locality', locale)} value={locality} onChange={e => setLocality(e.target.value)} className="input-field !py-2" />
          <input type="text" placeholder={t('search.pincode', locale)} value={pincode} onChange={e => setPincode(e.target.value)} className="input-field !py-2" maxLength={6} />
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
