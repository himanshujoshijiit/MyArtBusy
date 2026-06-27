'use client';

import { useState } from 'react';
import { useParams, useRouter } from 'next/navigation';
import { api } from '@/lib/api';
import { useAuth } from '@/lib/auth-context';

export default function QuotePage() {
  const { id } = useParams<{ id: string }>();
  const { user } = useAuth();
  const router = useRouter();
  const [details, setDetails] = useState('');
  const [eventDate, setEventDate] = useState('');
  const [occasion, setOccasion] = useState('BRIDAL');
  const [budgetMin, setBudgetMin] = useState('');
  const [budgetMax, setBudgetMax] = useState('');
  const [loading, setLoading] = useState(false);
  const [done, setDone] = useState(false);

  const submit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!user) { router.push('/login'); return; }
    setLoading(true);
    try {
      await api.quotes.create({
        muaId: id,
        occasion,
        eventDate: eventDate || undefined,
        details,
        budgetMin: budgetMin ? Number(budgetMin) : undefined,
        budgetMax: budgetMax ? Number(budgetMax) : undefined,
      });
      setDone(true);
    } catch (err) {
      alert(err instanceof Error ? err.message : 'Failed');
    } finally {
      setLoading(false);
    }
  };

  if (done) {
    return (
      <div className="mx-auto max-w-lg px-4 py-16 text-center">
        <h1 className="text-2xl font-bold">Quote Request Sent!</h1>
        <p className="mt-2 text-charcoal/60">The artist will respond via WhatsApp and your dashboard.</p>
      </div>
    );
  }

  return (
    <div className="mx-auto max-w-lg px-4 py-10">
      <h1 className="font-serif text-2xl font-bold">Request a Custom Quote</h1>
      <p className="mt-2 text-sm text-charcoal/60">Perfect for bridal packages, multi-day events, or custom looks.</p>
      <form onSubmit={submit} className="card mt-8 space-y-4 p-6">
        <select className="input-field" value={occasion} onChange={e => setOccasion(e.target.value)}>
          {['BRIDAL', 'WEDDING', 'PARTY', 'ENGAGEMENT', 'RECEPTION'].map(o => (
            <option key={o} value={o}>{o.replace('_', ' ')}</option>
          ))}
        </select>
        <input type="date" className="input-field" value={eventDate} onChange={e => setEventDate(e.target.value)} />
        <textarea className="input-field min-h-[120px]" placeholder="Describe your event, look preferences, number of people..."
          value={details} onChange={e => setDetails(e.target.value)} required />
        <div className="grid grid-cols-2 gap-3">
          <input className="input-field" placeholder="Budget min ₹" value={budgetMin} onChange={e => setBudgetMin(e.target.value)} />
          <input className="input-field" placeholder="Budget max ₹" value={budgetMax} onChange={e => setBudgetMax(e.target.value)} />
        </div>
        <button type="submit" className="btn-primary w-full" disabled={loading}>
          {loading ? 'Sending...' : 'Send Quote Request'}
        </button>
      </form>
    </div>
  );
}
