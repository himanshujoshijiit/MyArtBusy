'use client';

import { useState } from 'react';
import { useRouter, useSearchParams } from 'next/navigation';
import Link from 'next/link';
import { api } from '@/lib/api';
import { useAuth } from '@/lib/auth-context';

export default function LoginContent() {
  const router = useRouter();
  const searchParams = useSearchParams();
  const { login } = useAuth();
  const [email, setEmail] = useState('demo@makeupseven.com');
  const [password, setPassword] = useState('demo123');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    setError('');
    try {
      const res = await api.auth.login({ email, password });
      login(res.token, {
        userId: res.userId,
        email: res.email,
        fullName: res.fullName,
        role: res.role,
        muaProfileId: res.muaProfileId,
      });
      router.push(searchParams.get('redirect') || '/');
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Login failed');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="mx-auto flex min-h-[70vh] max-w-md flex-col justify-center px-4 py-12">
      <h1 className="font-display mb-2 text-center text-3xl font-bold">Welcome Back</h1>
      <p className="mb-8 text-center text-charcoal/50">Sign in to book artists and manage bookings</p>

      <form onSubmit={handleSubmit} className="card space-y-4 p-8">
        {error && <div className="rounded-lg bg-red-50 p-3 text-sm text-red-600">{error}</div>}
        <div>
          <label className="mb-1 block text-sm font-medium">Email</label>
          <input type="email" value={email} onChange={e => setEmail(e.target.value)} className="input-field" required />
        </div>
        <div>
          <label className="mb-1 block text-sm font-medium">Password</label>
          <input type="password" value={password} onChange={e => setPassword(e.target.value)} className="input-field" required />
        </div>
        <button type="submit" disabled={loading} className="btn-primary w-full">
          {loading ? 'Signing in...' : 'Sign In'}
        </button>
        <p className="text-center text-sm text-charcoal/50">
          Demo: demo@makeupseven.com / demo123<br />
          Artist: priya@makeupseven.com / artist123
        </p>
      </form>

      <p className="mt-6 text-center text-sm">
        Don&apos;t have an account? <Link href="/register" className="font-medium text-rose-600 hover:underline">Sign up</Link>
      </p>
    </div>
  );
}
