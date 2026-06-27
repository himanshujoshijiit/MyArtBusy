'use client';

import { useState } from 'react';
import { useRouter, useSearchParams } from 'next/navigation';
import Link from 'next/link';
import { api } from '@/lib/api';
import { useAuth } from '@/lib/auth-context';

export default function RegisterContent() {
  const router = useRouter();
  const searchParams = useSearchParams();
  const { login } = useAuth();
  const defaultRole = searchParams.get('role') === 'MUA' ? 'MUA' : 'CLIENT';

  const [form, setForm] = useState({
    email: '', password: '', fullName: '', phone: '', role: defaultRole,
  });
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    setError('');
    try {
      const res = await api.auth.register(form);
      login(res.token, {
        userId: res.userId,
        email: res.email,
        fullName: res.fullName,
        role: res.role,
        muaProfileId: res.muaProfileId,
      });
      router.push(res.role === 'MUA' ? '/dashboard' : '/');
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Registration failed');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="mx-auto flex min-h-[70vh] max-w-md flex-col justify-center px-4 py-12">
      <h1 className="font-display mb-2 text-center text-3xl font-bold">Join MakeupSeven</h1>
      <p className="mb-8 text-center text-charcoal/50">
        {form.role === 'MUA' ? 'List your profile — 3 free bookings/month' : 'Book verified makeup artists'}
      </p>

      <form onSubmit={handleSubmit} className="card space-y-4 p-8">
        {error && <div className="rounded-lg bg-red-50 p-3 text-sm text-red-600">{error}</div>}

        <div className="flex rounded-xl bg-charcoal/5 p-1">
          {['CLIENT', 'MUA'].map(r => (
            <button
              key={r}
              type="button"
              onClick={() => setForm(f => ({ ...f, role: r }))}
              className={`flex-1 rounded-lg py-2 text-sm font-medium transition ${
                form.role === r ? 'bg-white shadow-sm text-rose-600' : 'text-charcoal/50'
              }`}
            >
              {r === 'CLIENT' ? 'I need makeup' : "I'm an artist"}
            </button>
          ))}
        </div>

        <div>
          <label className="mb-1 block text-sm font-medium">Full Name</label>
          <input value={form.fullName} onChange={e => setForm(f => ({ ...f, fullName: e.target.value }))} className="input-field" required />
        </div>
        <div>
          <label className="mb-1 block text-sm font-medium">Email</label>
          <input type="email" value={form.email} onChange={e => setForm(f => ({ ...f, email: e.target.value }))} className="input-field" required />
        </div>
        <div>
          <label className="mb-1 block text-sm font-medium">Phone</label>
          <input type="tel" value={form.phone} onChange={e => setForm(f => ({ ...f, phone: e.target.value }))} className="input-field" placeholder="9876543210" required />
        </div>
        <div>
          <label className="mb-1 block text-sm font-medium">Password</label>
          <input type="password" value={form.password} onChange={e => setForm(f => ({ ...f, password: e.target.value }))} className="input-field" minLength={6} required />
        </div>
        <button type="submit" disabled={loading} className="btn-primary w-full">
          {loading ? 'Creating account...' : 'Create Account'}
        </button>
      </form>

      <p className="mt-6 text-center text-sm">
        Already have an account? <Link href="/login" className="font-medium text-rose-600 hover:underline">Sign in</Link>
      </p>
    </div>
  );
}
