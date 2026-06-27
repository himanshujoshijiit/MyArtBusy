'use client';

import { useEffect, useState } from 'react';
import Link from 'next/link';
import { useRouter } from 'next/navigation';
import { api, Booking, formatPrice, formatOccasion, statusLabel } from '@/lib/api';
import { useAuth } from '@/lib/auth-context';
import { Calendar, Star, FileText } from 'lucide-react';
import { format } from 'date-fns';

export default function BookingsPage() {
  const { user } = useAuth();
  const router = useRouter();
  const [bookings, setBookings] = useState<Booking[]>([]);
  const [loading, setLoading] = useState(true);

  const reload = () => {
    const fetch = user?.role === 'MUA' ? api.bookings.mua : api.bookings.my;
    fetch().then(setBookings);
  };

  useEffect(() => {
    if (!user) { router.push('/login'); return; }
    reload();
    setLoading(false);
  }, [user, router]);

  const statusColor = (s: string) => {
    if (s === 'COMPLETED') return 'bg-emerald-100 text-emerald-700';
    if (s === 'CONFIRMED') return 'bg-blue-100 text-blue-700';
    if (s === 'DEPOSIT_PAID') return 'bg-indigo-100 text-indigo-700';
    if (s === 'PENDING') return 'bg-amber-100 text-amber-700';
    if (s === 'CANCELLED') return 'bg-red-100 text-red-600';
    return 'bg-charcoal/10 text-charcoal/60';
  };

  if (loading) return <div className="py-20 text-center">Loading...</div>;

  return (
    <div className="mx-auto max-w-3xl px-4 py-10">
      <h1 className="font-display mb-8 text-3xl font-bold">My Bookings</h1>

      {bookings.length === 0 ? (
        <div className="card py-16 text-center">
          <Calendar className="mx-auto mb-4 h-12 w-12 text-charcoal/20" />
          <p className="text-charcoal/50">No bookings yet</p>
          <Link href="/search" className="mt-4 inline-block text-rose-600 hover:underline">Find an artist →</Link>
        </div>
      ) : (
        <div className="space-y-4">
          {bookings.map(b => (
            <div key={b.id} className="card p-6">
              <div className="mb-3 flex items-start justify-between gap-3">
                <div>
                  <h3 className="font-semibold">{user?.role === 'MUA' ? b.clientName : b.muaName}</h3>
                  {b.serviceName && <p className="text-sm text-charcoal/50">{b.serviceName}</p>}
                </div>
                <span className={`badge shrink-0 ${statusColor(b.status)}`}>{statusLabel(b.status)}</span>
              </div>

              <div className="mb-3 flex flex-wrap gap-x-4 gap-y-1 text-sm text-charcoal/60">
                <span>{format(new Date(b.bookingDate), 'EEE, MMM d, yyyy')} at {b.startTime?.slice(0, 5)}</span>
                <span className="font-semibold text-rose-600">{formatPrice(Number(b.totalAmount))}</span>
                {b.depositAmount && b.status !== 'PENDING' && (
                  <span className="text-emerald-600">Deposit {formatPrice(Number(b.depositAmount))} paid</span>
                )}
                {b.remainingAmount != null && b.remainingAmount > 0 && (
                  <span>Balance {formatPrice(Number(b.remainingAmount))}</span>
                )}
                {b.occasion && <span>{formatOccasion(b.occasion)}</span>}
              </div>

              <div className="flex flex-wrap gap-3">
                {(b.status === 'DEPOSIT_PAID' || b.status === 'CONFIRMED') && b.contractUrl && (
                  <Link href={`/contracts/${b.id}`} className="flex items-center gap-1 text-sm text-rose-600 hover:underline">
                    <FileText className="h-4 w-4" />
                    {b.contractSigned ? 'View signed contract' : 'Sign contract'}
                  </Link>
                )}

                {user?.role === 'MUA' && b.status === 'DEPOSIT_PAID' && (
                  <button onClick={() => api.bookings.confirm(b.id).then(reload)} className="btn-primary !py-1.5 !px-4 text-xs">Confirm Booking</button>
                )}
                {user?.role === 'MUA' && (b.status === 'CONFIRMED' || b.status === 'DEPOSIT_PAID') && (
                  <button onClick={() => api.bookings.complete(b.id).then(reload)} className="btn-secondary !py-1.5 !px-4 text-xs">Mark Complete</button>
                )}
                {!['COMPLETED', 'CANCELLED'].includes(b.status) && (
                  <button onClick={() => api.bookings.cancel(b.id).then(reload)} className="text-xs text-red-500 hover:underline">Cancel</button>
                )}
              </div>

              {user?.role === 'CLIENT' && b.status === 'COMPLETED' && (
                <ReviewForm bookingId={b.id} />
              )}
            </div>
          ))}
        </div>
      )}
    </div>
  );
}

function ReviewForm({ bookingId }: { bookingId: string }) {
  const [rating, setRating] = useState(5);
  const [comment, setComment] = useState('');
  const [submitted, setSubmitted] = useState(false);

  const submit = async () => {
    await api.reviews.create({ bookingId, rating, comment });
    setSubmitted(true);
  };

  if (submitted) return <p className="mt-4 text-sm text-emerald-600">✓ Review submitted — thank you!</p>;

  return (
    <div className="mt-4 border-t border-charcoal/5 pt-4">
      <p className="mb-2 text-sm font-medium">Leave a verified review</p>
      <div className="mb-2 flex gap-1">
        {[1, 2, 3, 4, 5].map(n => (
          <button key={n} onClick={() => setRating(n)}>
            <Star className={`h-5 w-5 ${n <= rating ? 'fill-gold-400 text-gold-400' : 'text-charcoal/20'}`} />
          </button>
        ))}
      </div>
      <textarea value={comment} onChange={e => setComment(e.target.value)} placeholder="Share your experience..." className="input-field mb-2 !py-2" rows={2} />
      <button onClick={submit} className="text-sm font-medium text-rose-600 hover:underline">Submit Review</button>
    </div>
  );
}
