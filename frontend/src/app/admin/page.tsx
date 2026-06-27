'use client';

import { useEffect, useState } from 'react';
import { api, AdminStats, formatPrice } from '@/lib/api';
import { useAuth } from '@/lib/auth-context';

export default function AdminPage() {
  const { user } = useAuth();
  const [stats, setStats] = useState<AdminStats | null>(null);
  const [error, setError] = useState('');

  useEffect(() => {
    if (user?.role !== 'ADMIN') return;
    api.admin.stats().then(setStats).catch(e => setError(e.message));
  }, [user]);

  if (!user) return <div className="px-4 py-16 text-center">Login required.</div>;
  if (user.role !== 'ADMIN') return <div className="px-4 py-16 text-center">Admin access only.</div>;

  const verify = async (id: string) => {
    await api.admin.verifyMua(id);
    setStats(await api.admin.stats());
  };

  return (
    <div className="mx-auto max-w-5xl px-4 py-10">
      <h1 className="font-serif text-3xl font-bold">Admin Dashboard</h1>
      {error && <p className="mt-2 text-red-600">{error}</p>}
      {stats && (
        <>
          <div className="mt-8 grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
            {[
              ['Users', stats.totalUsers],
              ['MUAs', stats.totalMuas],
              ['Bookings', stats.totalBookings],
              ['Revenue', formatPrice(Number(stats.totalRevenue))],
            ].map(([label, val]) => (
              <div key={label as string} className="card p-4">
                <p className="text-xs text-charcoal/50">{label}</p>
                <p className="text-2xl font-bold">{val}</p>
              </div>
            ))}
          </div>
          <h2 className="mt-10 text-lg font-semibold">Pending Verification ({stats.pendingVerification})</h2>
          <div className="mt-4 space-y-3">
            {stats.pendingMuas?.map(m => (
              <div key={m.id} className="card flex flex-wrap items-center justify-between gap-3 p-4">
                <div>
                  <p className="font-medium">{m.displayName}</p>
                  <p className="text-sm text-charcoal/60">{m.email} · {m.city}</p>
                </div>
                <button type="button" className="btn-primary text-sm" onClick={() => verify(m.id)}>Verify ✓</button>
              </div>
            ))}
          </div>
        </>
      )}
    </div>
  );
}
