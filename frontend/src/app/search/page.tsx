'use client';

import { useEffect, useState } from 'react';
import SearchBar from '@/components/SearchBar';
import ArtistCard from '@/components/ArtistCard';
import { api, SearchResult } from '@/lib/api';

export default function SearchPage() {
  const [artists, setArtists] = useState<SearchResult[]>([]);
  const [loading, setLoading] = useState(false);
  const [summary, setSummary] = useState('');
  const [sortBy, setSortBy] = useState('rating');
  const [lastParams, setLastParams] = useState<Record<string, string | number | boolean>>({ city: 'Bengaluru' });

  const search = async (params: Record<string, string | number | boolean>) => {
    setLoading(true);
    setLastParams(params);
    try {
      const res = await api.search.query({ ...params, sort_by: sortBy });
      setArtists(res.results);
      setSummary(res.query_summary);
    } catch {
      setArtists([]);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { search({ city: 'Bengaluru' }); }, []);

  const handleSort = (sort: string) => {
    setSortBy(sort);
    search({ ...lastParams, sort_by: sort });
  };

  return (
    <div className="mx-auto max-w-7xl px-4 py-10 sm:px-6">
      <h1 className="font-display mb-2 text-3xl font-bold">Find Your Artist</h1>
      <p className="mb-8 text-charcoal/50">Search by city + occasion — portfolio drives discovery</p>

      <SearchBar onSearch={search} loading={loading} />

      <div className="mt-8 flex flex-wrap items-center justify-between gap-4">
        <p className="text-sm text-charcoal/60">
          {loading ? 'Searching...' : `${artists.length} results · ${summary}`}
        </p>
        <div className="flex gap-2">
          {[
            { value: 'rating', label: 'Top Rated' },
            { value: 'price_low', label: 'Price: Low' },
            { value: 'bookings', label: 'Most Booked' },
          ].map(s => (
            <button
              key={s.value}
              onClick={() => handleSort(s.value)}
              className={`rounded-full px-3 py-1.5 text-xs font-medium transition ${
                sortBy === s.value ? 'bg-rose-600 text-white' : 'bg-charcoal/5 text-charcoal/70 hover:bg-rose-50'
              }`}
            >
              {s.label}
            </button>
          ))}
        </div>
      </div>

      <div className="mt-6 grid gap-6 sm:grid-cols-2 lg:grid-cols-3">
        {artists.map(a => <ArtistCard key={a.id} artist={a} />)}
      </div>
    </div>
  );
}
