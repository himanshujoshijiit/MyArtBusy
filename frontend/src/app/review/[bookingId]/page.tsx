'use client';

import { useEffect, useState } from 'react';
import { useParams, useRouter } from 'next/navigation';
import Link from 'next/link';
import { api } from '@/lib/api';
import { useAuth } from '@/lib/auth-context';
import { Star, CheckCircle } from 'lucide-react';

export default function ReviewPage() {
  const { bookingId } = useParams<{ bookingId: string }>();
  const { user } = useAuth();
  const router = useRouter();
  const [rating, setRating] = useState(5);
  const [comment, setComment] = useState('');
  const [done, setDone] = useState(false);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    if (!user) router.push(`/login?redirect=/review/${bookingId}`);
  }, [user, bookingId, router]);

  const submit = async () => {
    setLoading(true);
    setError('');
    try {
      await api.reviews.create({ bookingId, rating, comment });
      setDone(true);
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Review failed');
    } finally {
      setLoading(false);
    }
  };

  if (done) {
    return (
      <div className="mx-auto max-w-md px-4 py-20 text-center">
        <CheckCircle className="mx-auto mb-4 h-16 w-16 text-emerald-500" />
        <h1 className="font-display mb-2 text-2xl font-bold">Thank You!</h1>
        <p className="mb-6 text-charcoal/60">Your verified review helps others find great artists.</p>
        <Link href="/" className="btn-primary">Browse More Artists</Link>
      </div>
    );
  }

  return (
    <div className="mx-auto max-w-md px-4 py-16">
      <h1 className="font-display mb-2 text-center text-2xl font-bold">How was your experience?</h1>
      <p className="mb-8 text-center text-sm text-charcoal/50">Verified review — only clients who booked can review</p>

      <div className="card space-y-4 p-8">
        {error && <p className="text-sm text-red-600">{error}</p>}
        <div className="flex justify-center gap-2">
          {[1, 2, 3, 4, 5].map(n => (
            <button key={n} onClick={() => setRating(n)}>
              <Star className={`h-8 w-8 ${n <= rating ? 'fill-gold-400 text-gold-400' : 'text-charcoal/20'}`} />
            </button>
          ))}
        </div>
        <textarea value={comment} onChange={e => setComment(e.target.value)} rows={4} className="input-field" placeholder="Share your experience..." />
        <button onClick={submit} disabled={loading} className="btn-primary w-full">
          {loading ? 'Submitting...' : 'Submit Verified Review'}
        </button>
      </div>
    </div>
  );
}
