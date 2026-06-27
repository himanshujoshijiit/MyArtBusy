'use client';

import Link from 'next/link';
import { useEffect, useState } from 'react';
import { useParams, useRouter, useSearchParams } from 'next/navigation';
import { api, MuaProfile, AvailabilitySlot, formatPrice } from '@/lib/api';
import { useAuth } from '@/lib/auth-context';
import { format, addDays } from 'date-fns';
import { Calendar, CreditCard, CheckCircle } from 'lucide-react';

declare global {
  interface Window {
    Razorpay: new (options: object) => { open: () => void };
  }
}

export default function BookPageContent() {
  const { id } = useParams<{ id: string }>();
  const searchParams = useSearchParams();
  const router = useRouter();
  const { user } = useAuth();
  const serviceId = searchParams.get('service');

  const [artist, setArtist] = useState<MuaProfile | null>(null);
  const [slots, setSlots] = useState<AvailabilitySlot[]>([]);
  const [selectedService, setSelectedService] = useState(serviceId || '');
  const [selectedDate, setSelectedDate] = useState('');
  const [selectedTime, setSelectedTime] = useState('');
  const [notes, setNotes] = useState('');
  const [step, setStep] = useState(1);
  const [bookingId, setBookingId] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [done, setDone] = useState(false);

  useEffect(() => {
    if (!user) router.push(`/login?redirect=/artist/${id}/book`);
  }, [user, id, router]);

  useEffect(() => {
    api.muas.getById(id).then(a => {
      setArtist(a);
      if (serviceId) setSelectedService(serviceId);
    });
    const start = format(new Date(), 'yyyy-MM-dd');
    const end = format(addDays(new Date(), 14), 'yyyy-MM-dd');
    api.bookings.availability(id, start, end).then(setSlots);
  }, [id, serviceId]);

  const service = artist?.services.find(s => s.id === selectedService);
  const price = service?.price || artist?.minPrice || 5000;
  const deposit = Math.round(Number(price) * 0.25);

  const datesWithSlots = Array.from(new Set(slots.filter(s => s.available).map(s => s.slotDate)));
  const timesForDate = slots.filter(s => s.slotDate === selectedDate && s.available);

  const handleCreateBooking = async () => {
    if (!selectedDate || !selectedTime) {
      setError('Please select date and time');
      return;
    }
    setLoading(true);
    setError('');
    try {
      const booking = await api.bookings.create({
        muaId: id,
        serviceId: selectedService || undefined,
        bookingDate: selectedDate,
        startTime: selectedTime,
        occasion: service?.occasion || artist?.occasions[0],
        notes,
      });
      setBookingId(booking.id);
      setStep(2);
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Booking failed');
    } finally {
      setLoading(false);
    }
  };

  const handlePayment = async () => {
    setLoading(true);
    try {
      const order = await api.payments.createDeposit(bookingId);

      if (order.mock) {
        await api.payments.verify({ orderId: order.orderId, paymentId: 'pay_mock_' + Date.now(), signature: 'mock' });
        setDone(true);
        return;
      }

      const script = document.createElement('script');
      script.src = 'https://checkout.razorpay.com/v1/checkout.js';
      script.onload = () => {
        const rzp = new window.Razorpay({
          key: order.keyId,
          amount: order.amount,
          currency: order.currency,
          order_id: order.orderId,
          name: 'MakeupSeven',
          description: `Deposit for ${artist?.displayName}`,
          handler: async (response: { razorpay_order_id: string; razorpay_payment_id: string; razorpay_signature: string }) => {
            await api.payments.verify({
              orderId: response.razorpay_order_id,
              paymentId: response.razorpay_payment_id,
              signature: response.razorpay_signature,
            });
            setDone(true);
          },
        });
        rzp.open();
      };
      document.body.appendChild(script);
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Payment failed');
    } finally {
      setLoading(false);
    }
  };

  if (!artist) return <div className="py-20 text-center">Loading...</div>;

  if (done) {
    return (
      <div className="mx-auto max-w-lg px-4 py-20 text-center">
        <CheckCircle className="mx-auto mb-4 h-16 w-16 text-emerald-500" />
        <h1 className="font-display mb-2 text-3xl font-bold">Deposit Paid!</h1>
        <p className="mb-4 text-charcoal/60">
          WhatsApp confirmation sent. {artist.displayName} will confirm your booking shortly.
        </p>
        <p className="mb-6 text-sm text-charcoal/50">
          Sign your service contract to complete the booking process.
        </p>
        <div className="flex flex-col gap-3 sm:flex-row sm:justify-center">
          <Link href={`/contracts/${bookingId}`} className="btn-primary">Sign Contract</Link>
          <button onClick={() => router.push('/bookings')} className="btn-secondary">View Bookings</button>
        </div>
      </div>
    );
  }

  return (
    <div className="mx-auto max-w-2xl px-4 py-10">
      <h1 className="font-display mb-2 text-3xl font-bold">Book {artist.displayName}</h1>
      <p className="mb-8 text-charcoal/50">{artist.locality}, {artist.city}</p>

      <div className="mb-8 flex gap-4">
        {['Select Slot', 'Pay Deposit'].map((label, i) => (
          <div key={label} className={`flex items-center gap-2 text-sm ${step > i ? 'text-rose-600 font-semibold' : 'text-charcoal/40'}`}>
            <span className={`flex h-7 w-7 items-center justify-center rounded-full text-xs ${step > i ? 'bg-rose-600 text-white' : 'bg-charcoal/10'}`}>{i + 1}</span>
            {label}
          </div>
        ))}
      </div>

      {error && <div className="mb-4 rounded-xl bg-red-50 p-4 text-sm text-red-600">{error}</div>}

      {step === 1 && (
        <div className="space-y-6">
          {!serviceId && (
            <div>
              <label className="mb-2 block text-sm font-medium">Service</label>
              <select value={selectedService} onChange={e => setSelectedService(e.target.value)} className="input-field">
                <option value="">General session — {formatPrice(artist.minPrice)}</option>
                {artist.services.map(s => (
                  <option key={s.id} value={s.id}>{s.name} — {formatPrice(s.price)}</option>
                ))}
              </select>
            </div>
          )}

          <div>
            <label className="mb-2 block text-sm font-medium"><Calendar className="inline h-4 w-4 mr-1" />Date</label>
            <div className="flex flex-wrap gap-2">
              {datesWithSlots.map(d => (
                <button
                  key={d}
                  onClick={() => { setSelectedDate(d); setSelectedTime(''); }}
                  className={`rounded-xl px-4 py-2 text-sm font-medium transition ${
                    selectedDate === d ? 'bg-rose-600 text-white' : 'bg-charcoal/5 hover:bg-rose-50'
                  }`}
                >
                  {format(new Date(d), 'EEE, MMM d')}
                </button>
              ))}
            </div>
          </div>

          {selectedDate && (
            <div>
              <label className="mb-2 block text-sm font-medium">Time</label>
              <div className="flex flex-wrap gap-2">
                {timesForDate.map(s => (
                  <button
                    key={s.id}
                    onClick={() => setSelectedTime(s.startTime.slice(0, 5))}
                    className={`rounded-xl px-4 py-2 text-sm font-medium transition ${
                      selectedTime === s.startTime.slice(0, 5) ? 'bg-rose-600 text-white' : 'bg-charcoal/5 hover:bg-rose-50'
                    }`}
                  >
                    {s.startTime.slice(0, 5)}
                  </button>
                ))}
              </div>
            </div>
          )}

          <div>
            <label className="mb-2 block text-sm font-medium">Notes (optional)</label>
            <textarea value={notes} onChange={e => setNotes(e.target.value)} rows={3} className="input-field" placeholder="Skin concerns, look preferences..." />
          </div>

          <div className="card p-5">
            <div className="flex justify-between text-sm"><span>Total</span><span className="font-semibold">{formatPrice(Number(price))}</span></div>
            <div className="mt-2 flex justify-between text-sm text-rose-600"><span>Deposit (25%)</span><span className="font-semibold">{formatPrice(deposit)}</span></div>
          </div>

          <button onClick={handleCreateBooking} disabled={loading} className="btn-primary w-full">
            {loading ? 'Creating booking...' : 'Continue to Payment'}
          </button>
        </div>
      )}

      {step === 2 && (
        <div className="card p-8 text-center">
          <CreditCard className="mx-auto mb-4 h-12 w-12 text-rose-500" />
          <h2 className="mb-2 text-xl font-bold">Pay Deposit — {formatPrice(deposit)}</h2>
          <p className="mb-6 text-sm text-charcoal/50">Secure payment via Razorpay. Contract & WhatsApp confirmation on success.</p>
          <button onClick={handlePayment} disabled={loading} className="btn-primary w-full">
            {loading ? 'Processing...' : 'Pay Deposit Now'}
          </button>
        </div>
      )}
    </div>
  );
}
