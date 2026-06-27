'use client';

import { useState } from 'react';
import { useRouter } from 'next/navigation';
import { api } from '@/lib/api';
import { useAuth } from '@/lib/auth-context';

const OCCASIONS = [
  'BRIDAL', 'WEDDING', 'PARTY', 'GLAMOROUS', 'HALDI_MEHENDI',
  'EDITORIAL', 'FILM', 'PERSONAL_EVENT', 'ENGAGEMENT', 'RECEPTION',
];
const SKIN_TONES = ['FAIR', 'LIGHT', 'MEDIUM', 'OLIVE', 'TAN', 'DEEP', 'DARK', 'ALL'];
const SALON_PRESETS = ['Threading', 'Waxing', 'Facial', 'Hair Styling'];

function formatLabel(value: string) {
  return value.replace(/_/g, ' ').replace(/\b\w/g, c => c.toUpperCase());
}

export default function OnboardingPage() {
  const { user } = useAuth();
  const router = useRouter();
  const [step, setStep] = useState(1);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [form, setForm] = useState({
    displayName: user?.fullName || '',
    bio: '',
    city: 'Bengaluru',
    locality: '',
    pincode: '',
    minPrice: '',
    maxPrice: '',
    occasions: [] as string[],
    skinToneExpertise: [] as string[],
  });
  const [portfolioUrl, setPortfolioUrl] = useState('');
  const [serviceName, setServiceName] = useState('Bridal Makeup');
  const [servicePrice, setServicePrice] = useState('');
  const [serviceCategory, setServiceCategory] = useState<'MAKEUP' | 'SALON'>('MAKEUP');

  if (!user || user.role !== 'MUA') {
    return <div className="mx-auto max-w-lg px-4 py-16 text-center">MUA login required.</div>;
  }

  const toggle = (arr: string[], val: string, key: 'occasions' | 'skinToneExpertise') => {
    const next = arr.includes(val) ? arr.filter(x => x !== val) : [...arr, val];
    setForm({ ...form, [key]: next });
  };

  const submitProfile = async () => {
    setLoading(true);
    setError('');
    try {
      await api.auth.completeOnboarding({
        displayName: form.displayName,
        bio: form.bio,
        city: form.city,
        locality: form.locality,
        pincode: form.pincode,
        occasions: form.occasions,
        skinToneExpertise: form.skinToneExpertise,
        minPrice: form.minPrice ? Number(form.minPrice) : undefined,
        maxPrice: form.maxPrice ? Number(form.maxPrice) : undefined,
      });
      setStep(3);
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Failed');
    } finally {
      setLoading(false);
    }
  };

  const addPortfolio = async () => {
    if (!portfolioUrl) return;
    await api.muas.addPortfolio({ imageUrl: portfolioUrl, caption: 'Portfolio look' });
    setPortfolioUrl('');
  };

  const addService = async () => {
    if (!servicePrice) return;
    await api.muas.addService({
      name: serviceName,
      price: Number(servicePrice),
      durationMinutes: serviceCategory === 'SALON' ? 45 : 120,
      category: serviceCategory,
    });
  };

  const goLive = () => router.push('/dashboard');

  return (
    <div className="mx-auto max-w-2xl px-4 py-10">
      <h1 className="font-serif text-3xl font-bold">Artist Onboarding</h1>
      <p className="mt-2 text-charcoal/60">Step {step} of 3 — get your profile live on MakeupSeven</p>
      <div className="mt-4 flex gap-2">
        {[1, 2, 3].map(s => (
          <div key={s} className={`h-2 flex-1 rounded-full ${s <= step ? 'bg-rose-500' : 'bg-charcoal/10'}`} />
        ))}
      </div>

      {error && <p className="mt-4 text-sm text-red-600">{error}</p>}

      {step === 1 && (
        <div className="card mt-8 space-y-4 p-6">
          <input className="input-field" placeholder="Display name" value={form.displayName}
            onChange={e => setForm({ ...form, displayName: e.target.value })} />
          <textarea className="input-field min-h-[100px]" placeholder="Bio — your style, experience, specialties"
            value={form.bio} onChange={e => setForm({ ...form, bio: e.target.value })} />
          <div className="grid gap-3 sm:grid-cols-2">
            <input className="input-field" placeholder="City" value={form.city} onChange={e => setForm({ ...form, city: e.target.value })} />
            <input className="input-field" placeholder="Locality (Koramangala)" value={form.locality} onChange={e => setForm({ ...form, locality: e.target.value })} />
            <input className="input-field" placeholder="PIN code" value={form.pincode} onChange={e => setForm({ ...form, pincode: e.target.value })} />
          </div>
          <button type="button" className="btn-primary w-full" onClick={() => setStep(2)}>Next →</button>
        </div>
      )}

      {step === 2 && (
        <div className="card mt-8 space-y-4 p-6">
          <div>
            <p className="mb-2 text-sm font-medium">Occasions</p>
            <div className="flex flex-wrap gap-2">
              {OCCASIONS.map(o => (
                <button key={o} type="button" onClick={() => toggle(form.occasions, o, 'occasions')}
                  className={`rounded-full px-3 py-1 text-xs ${form.occasions.includes(o) ? 'bg-rose-500 text-white' : 'bg-charcoal/5'}`}>{formatLabel(o)}</button>
              ))}
            </div>
          </div>
          <div>
            <p className="mb-2 text-sm font-medium">Skin tone expertise</p>
            <div className="flex flex-wrap gap-2">
              {SKIN_TONES.map(s => (
                <button key={s} type="button" onClick={() => toggle(form.skinToneExpertise, s, 'skinToneExpertise')}
                  className={`rounded-full px-3 py-1 text-xs ${form.skinToneExpertise.includes(s) ? 'bg-rose-500 text-white' : 'bg-charcoal/5'}`}>{s}</button>
              ))}
            </div>
          </div>
          <div className="grid gap-3 sm:grid-cols-2">
            <input className="input-field" placeholder="Min price ₹" value={form.minPrice} onChange={e => setForm({ ...form, minPrice: e.target.value })} />
            <input className="input-field" placeholder="Max price ₹" value={form.maxPrice} onChange={e => setForm({ ...form, maxPrice: e.target.value })} />
          </div>
          <button type="button" className="btn-primary w-full" disabled={loading} onClick={submitProfile}>
            {loading ? 'Saving...' : 'Save & Continue →'}
          </button>
        </div>
      )}

      {step === 3 && (
        <div className="card mt-8 space-y-4 p-6">
          <p className="text-sm text-charcoal/70">Add at least one portfolio image and service to go live.</p>
          <div className="flex gap-2">
            <input className="input-field flex-1" placeholder="Portfolio image URL" value={portfolioUrl} onChange={e => setPortfolioUrl(e.target.value)} />
            <button type="button" className="btn-secondary" onClick={addPortfolio}>Add</button>
          </div>
          <div className="flex flex-wrap gap-2">
            <select className="input-field !py-2" value={serviceCategory} onChange={e => setServiceCategory(e.target.value as 'MAKEUP' | 'SALON')}>
              <option value="MAKEUP">Makeup service</option>
              <option value="SALON">Salon add-on</option>
            </select>
            {serviceCategory === 'SALON' && SALON_PRESETS.map(p => (
              <button key={p} type="button" className="rounded-full bg-charcoal/5 px-3 py-1 text-xs"
                onClick={() => setServiceName(p)}>{p}</button>
            ))}
          </div>
          <div className="flex gap-2">
            <input className="input-field" placeholder="Service name" value={serviceName} onChange={e => setServiceName(e.target.value)} />
            <input className="input-field w-32" placeholder="Price ₹" value={servicePrice} onChange={e => setServicePrice(e.target.value)} />
            <button type="button" className="btn-secondary" onClick={addService}>Add</button>
          </div>
          <label className="block text-sm">
            Or upload a photo:
            <input type="file" accept="image/*" className="mt-1 text-sm" onChange={async e => {
              const f = e.target.files?.[0];
              if (f) {
                const { url } = await api.upload.image(f);
                await api.muas.addPortfolio({ imageUrl: `${process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080'}${url}` });
              }
            }} />
          </label>
          <button type="button" className="btn-primary w-full" onClick={goLive}>🚀 Go Live — Open Dashboard</button>
        </div>
      )}
    </div>
  );
}
