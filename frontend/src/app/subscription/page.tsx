'use client';

import { useState } from 'react';
import { useRouter } from 'next/navigation';
import Link from 'next/link';
import { api } from '@/lib/api';
import { useAuth } from '@/lib/auth-context';
import { Crown, CheckCircle } from 'lucide-react';

declare global {
  interface Window {
    Razorpay: new (options: object) => { open: () => void };
  }
}

export default function SubscriptionPage() {
  const { user } = useAuth();
  const router = useRouter();
  const [loading, setLoading] = useState(false);
  const [done, setDone] = useState(false);
  const [error, setError] = useState('');

  if (!user) {
    router.push('/login?redirect=/subscription');
    return null;
  }

  const upgrade = async () => {
    setLoading(true);
    setError('');
    try {
      const order = await api.subscription.createProOrder();
      if (order.mock) {
        await api.subscription.activatePro();
        setDone(true);
        return;
      }
      const script = document.createElement('script');
      script.src = 'https://checkout.razorpay.com/v1/checkout.js';
      script.onload = () => {
        new window.Razorpay({
          key: order.keyId,
          amount: order.amount,
          currency: order.currency,
          order_id: order.orderId,
          name: 'MakeupSeven Pro',
          description: 'Unlimited bookings + full dashboard',
          handler: async () => {
            await api.subscription.activatePro();
            setDone(true);
          },
        }).open();
      };
      document.body.appendChild(script);
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Upgrade failed');
    } finally {
      setLoading(false);
    }
  };

  if (done) {
    return (
      <div className="mx-auto max-w-lg px-4 py-20 text-center">
        <CheckCircle className="mx-auto mb-4 h-16 w-16 text-emerald-500" />
        <h1 className="font-display mb-2 text-3xl font-bold">Welcome to Pro!</h1>
        <p className="mb-6 text-charcoal/60">Unlimited bookings, full analytics, and priority listing activated.</p>
        <Link href="/dashboard" className="btn-primary">Go to Dashboard</Link>
      </div>
    );
  }

  return (
    <div className="mx-auto max-w-2xl px-4 py-16">
      <div className="card overflow-hidden">
        <div className="bg-gradient-to-r from-gold-400 to-gold-500 px-8 py-10 text-center text-white">
          <Crown className="mx-auto mb-4 h-12 w-12" />
          <h1 className="font-display text-3xl font-bold">MakeupSeven Pro</h1>
          <p className="mt-2 text-white/80">₹999/month · Cancel anytime</p>
        </div>
        <div className="p-8">
          <ul className="mb-8 space-y-3 text-sm">
            {['Unlimited bookings per month', 'Full business dashboard & analytics', 'Client face profiles & kit manager', 'Priority in search results', 'Net earnings & commission tracking', 'Contract & e-sign tools'].map(f => (
              <li key={f} className="flex items-center gap-2"><CheckCircle className="h-4 w-4 text-emerald-500" />{f}</li>
            ))}
          </ul>
          {error && <p className="mb-4 text-sm text-red-600">{error}</p>}
          <button onClick={upgrade} disabled={loading} className="btn-gold w-full">
            {loading ? 'Processing...' : 'Upgrade Now — ₹999/month'}
          </button>
          <p className="mt-4 text-center text-xs text-charcoal/40">Free tier: 3 bookings/month · 10% platform commission on all bookings</p>
        </div>
      </div>
    </div>
  );
}
