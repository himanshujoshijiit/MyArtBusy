'use client';

import { useEffect, useState } from 'react';
import Link from 'next/link';
import { useRouter } from 'next/navigation';
import { format, addDays } from 'date-fns';
import {
  api, DashboardStats, Booking, KitItem, ClientProfile, AvailabilitySlot, MuaProfile, formatPrice, statusLabel,
} from '@/lib/api';
import { useAuth } from '@/lib/auth-context';
import {
  Calendar, TrendingUp, Star, Package, AlertTriangle, Users, Crown, Clock, UserCircle, Settings, Plus,
} from 'lucide-react';

type Tab = 'overview' | 'calendar' | 'bookings' | 'clients' | 'kit' | 'profile';

export default function DashboardPage() {
  const { user } = useAuth();
  const router = useRouter();
  const [stats, setStats] = useState<DashboardStats | null>(null);
  const [bookings, setBookings] = useState<Booking[]>([]);
  const [kit, setKit] = useState<KitItem[]>([]);
  const [clients, setClients] = useState<ClientProfile[]>([]);
  const [profile, setProfile] = useState<MuaProfile | null>(null);
  const [tab, setTab] = useState<Tab>('overview');
  const [loading, setLoading] = useState(true);

  const reload = () =>
    Promise.all([
      api.dashboard.stats(),
      api.bookings.mua(),
      api.dashboard.kit(),
      api.dashboard.clients(),
      api.muas.getMyProfile(),
    ]).then(([s, b, k, c, p]) => {
      setStats(s);
      setBookings(b);
      setKit(k);
      setClients(c);
      setProfile(p);
    });

  useEffect(() => {
    if (!user) { router.push('/login'); return; }
    if (user.role !== 'MUA') { router.push('/'); return; }
    reload().finally(() => setLoading(false));
  }, [user, router]);

  if (loading) return <div className="py-20 text-center">Loading dashboard...</div>;
  if (!stats) return null;

  const tabs: { id: Tab; label: string }[] = [
    { id: 'overview', label: 'Overview' },
    { id: 'calendar', label: 'Calendar' },
    { id: 'bookings', label: 'Bookings' },
    { id: 'clients', label: 'Clients' },
    { id: 'kit', label: 'Kit' },
    { id: 'profile', label: 'Profile' },
  ];

  return (
    <div className="mx-auto max-w-6xl px-4 py-10">
      <div className="mb-8 flex flex-wrap items-center justify-between gap-4">
        <div>
          <h1 className="font-display text-3xl font-bold">Business Dashboard</h1>
          <p className="text-charcoal/50">Welcome back, {user?.fullName}</p>
        </div>
        {stats.subscription.tier === 'FREE' ? (
          <Link href="/subscription" className="card flex items-center gap-3 px-5 py-3 transition hover:border-rose-200">
            <Crown className="h-5 w-5 text-gold-500" />
            <div>
              <p className="text-sm font-semibold">Upgrade to Pro — ₹999/mo</p>
              <p className="text-xs text-charcoal/50">{stats.subscription.bookingsUsed}/{stats.subscription.bookingsLimit} free bookings used</p>
            </div>
          </Link>
        ) : (
          <span className="badge-top flex items-center gap-1"><Crown className="h-3 w-3" /> Pro Member</span>
        )}
      </div>

      <div className="mb-8 grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
        {[
          { icon: Calendar, label: 'This Month', value: stats.monthlyBookings, sub: `${stats.pendingBookings} pending` },
          { icon: TrendingUp, label: 'Net Earnings', value: formatPrice(Number(stats.monthlyNetEarnings)), sub: `Gross ${formatPrice(Number(stats.monthlyEarnings))} · Commission ${formatPrice(Number(stats.totalCommission))}` },
          { icon: Star, label: 'Rating', value: stats.averageRating.toFixed(1), sub: `${stats.reviewCount} reviews` },
          { icon: Users, label: 'Total Bookings', value: stats.totalBookings, sub: `${clients.length} client profiles` },
        ].map(({ icon: Icon, label, value, sub }) => (
          <div key={label} className="card p-5">
            <div className="mb-3 flex items-center gap-2 text-charcoal/50">
              <Icon className="h-4 w-4" /><span className="text-sm">{label}</span>
            </div>
            <p className="text-2xl font-bold">{value}</p>
            <p className="mt-1 text-xs text-charcoal/40">{sub}</p>
          </div>
        ))}
      </div>

      {stats.kitAlerts.length > 0 && (
        <div className="mb-8 rounded-xl border border-amber-200 bg-amber-50 p-4">
          <div className="mb-2 flex items-center gap-2 font-medium text-amber-800">
            <AlertTriangle className="h-4 w-4" /> Kit Alerts
          </div>
          {stats.kitAlerts.map(a => (
            <p key={a.id} className="text-sm text-amber-700">{a.name}: {a.message}</p>
          ))}
        </div>
      )}

      <div className="mb-6 flex flex-wrap gap-1 border-b border-charcoal/10">
        {tabs.map(t => (
          <button key={t.id} onClick={() => setTab(t.id)}
            className={`px-4 py-2 text-sm font-medium transition ${tab === t.id ? 'border-b-2 border-rose-600 text-rose-600' : 'text-charcoal/50 hover:text-charcoal'}`}>
            {t.label}
          </button>
        ))}
      </div>

      {tab === 'overview' && (
        <div className="card p-6">
          <h3 className="mb-4 font-semibold">Recent Bookings</h3>
          {bookings.slice(0, 8).map(b => (
            <BookingRow key={b.id} booking={b} onAction={reload} />
          ))}
        </div>
      )}

      {tab === 'calendar' && profile && (
        <CalendarTab muaId={profile.id} bookings={bookings} onAdded={reload} />
      )}

      {tab === 'bookings' && (
        <div className="space-y-3">
          {bookings.map(b => <BookingRow key={b.id} booking={b} onAction={reload} />)}
        </div>
      )}

      {tab === 'clients' && (
        <ClientsTab clients={clients} bookings={bookings} onSaved={reload} />
      )}

      {tab === 'kit' && (
        <KitTab kit={kit} onAdded={item => setKit(k => [...k, item])} />
      )}

      {tab === 'profile' && profile && (
        <ProfileTab profile={profile} onSaved={reload} />
      )}
    </div>
  );
}

function BookingRow({ booking: b, onAction }: { booking: Booking; onAction: () => void }) {
  const canConfirm = b.status === 'DEPOSIT_PAID';
  const canComplete = b.status === 'CONFIRMED' || b.status === 'DEPOSIT_PAID';
  const canCancel = !['COMPLETED', 'CANCELLED'].includes(b.status);

  return (
    <div className="flex flex-wrap items-center justify-between gap-3 border-b border-charcoal/5 py-4 last:border-0">
      <div>
        <p className="font-medium">{b.clientName}</p>
        <p className="text-sm text-charcoal/50">
          {format(new Date(b.bookingDate), 'MMM d, yyyy')} · {b.startTime?.slice(0, 5)} · {formatPrice(Number(b.totalAmount))}
          {b.commissionAmount ? ` · Commission ${formatPrice(Number(b.commissionAmount))}` : ''}
        </p>
      </div>
      <div className="flex flex-wrap items-center gap-2">
        <span className="badge bg-charcoal/5 text-xs">{statusLabel(b.status)}</span>
        {b.contractUrl && (
          <Link href={`/contracts/${b.id}`} className="text-xs text-rose-600 hover:underline">Contract</Link>
        )}
        {canConfirm && (
          <button onClick={() => api.bookings.confirm(b.id).then(onAction)} className="btn-primary !py-1 !px-3 text-xs">Confirm</button>
        )}
        {canComplete && (
          <button onClick={() => api.bookings.complete(b.id).then(onAction)} className="btn-secondary !py-1 !px-3 text-xs">Complete</button>
        )}
        {canCancel && (
          <button onClick={() => api.bookings.cancel(b.id).then(onAction)} className="text-xs text-red-500 hover:underline">Cancel</button>
        )}
      </div>
    </div>
  );
}

function CalendarTab({ muaId, bookings, onAdded }: { muaId: string; bookings: Booking[]; onAdded: () => void }) {
  const [slots, setSlots] = useState<AvailabilitySlot[]>([]);
  const [blocked, setBlocked] = useState<{ id: string; blockDate: string; reason?: string }[]>([]);
  const [date, setDate] = useState(format(addDays(new Date(), 1), 'yyyy-MM-dd'));
  const [blockDate, setBlockDate] = useState('');
  const [blockReason, setBlockReason] = useState('');
  const [startTime, setStartTime] = useState('10:00');
  const [endTime, setEndTime] = useState('12:00');

  const reloadSlots = () => {
    const start = format(new Date(), 'yyyy-MM-dd');
    const end = format(addDays(new Date(), 21), 'yyyy-MM-dd');
    api.bookings.availability(muaId, start, end).then(setSlots);
    api.blockedDates.list().then(setBlocked).catch(() => {});
  };

  useEffect(() => { reloadSlots(); }, [muaId]);

  const addSlot = async () => {
    await api.bookings.addAvailability({ slotDate: date, startTime: `${startTime}:00`, endTime: `${endTime}:00`, available: true });
    onAdded();
    reloadSlots();
  };

  const addBlock = async () => {
    if (!blockDate) return;
    await api.blockedDates.add({ blockDate, reason: blockReason || 'Personal' });
    setBlockDate('');
    setBlockReason('');
    reloadSlots();
  };

  const removeBlock = async (d: string) => {
    await api.blockedDates.remove(d);
    reloadSlots();
  };

  const bookedDates = new Set(bookings.filter(b => !['CANCELLED'].includes(b.status)).map(b => b.bookingDate));

  return (
    <div className="grid gap-6 lg:grid-cols-2">
      <div className="card p-6">
        <h3 className="mb-4 flex items-center gap-2 font-semibold"><Clock className="h-4 w-4" /> Add Availability</h3>
        <div className="space-y-3">
          <input type="date" value={date} onChange={e => setDate(e.target.value)} className="input-field" />
          <div className="flex gap-3">
            <input type="time" value={startTime} onChange={e => setStartTime(e.target.value)} className="input-field" />
            <input type="time" value={endTime} onChange={e => setEndTime(e.target.value)} className="input-field" />
          </div>
          <button onClick={addSlot} className="btn-primary w-full"><Plus className="h-4 w-4" /> Add Slot</button>
        </div>
        <div className="mt-6 border-t border-charcoal/5 pt-4">
          <h4 className="mb-2 text-sm font-semibold">Block Dates (travel / personal)</h4>
          <input type="date" value={blockDate} onChange={e => setBlockDate(e.target.value)} className="input-field mb-2" />
          <input type="text" placeholder="Reason" value={blockReason} onChange={e => setBlockReason(e.target.value)} className="input-field mb-2" />
          <button type="button" onClick={addBlock} className="btn-secondary w-full text-sm">Block Date</button>
          {blocked.length > 0 && (
            <ul className="mt-3 space-y-1 text-sm">
              {blocked.map(b => (
                <li key={b.id} className="flex justify-between rounded bg-red-50 px-2 py-1">
                  <span>{b.blockDate} {b.reason && `— ${b.reason}`}</span>
                  <button type="button" onClick={() => removeBlock(b.blockDate)} className="text-red-600">×</button>
                </li>
              ))}
            </ul>
          )}
        </div>
      </div>
      <div className="card p-6">
        <h3 className="mb-4 font-semibold">Upcoming ({slots.length} open slots)</h3>
        <div className="max-h-80 space-y-2 overflow-y-auto">
          {slots.map(s => (
            <div key={s.id} className="flex justify-between rounded-lg bg-charcoal/5 px-3 py-2 text-sm">
              <span>{format(new Date(s.slotDate), 'EEE MMM d')} · {s.startTime.slice(0, 5)}–{s.endTime.slice(0, 5)}</span>
              <span className="text-emerald-600">Open</span>
            </div>
          ))}
          {bookings.filter(b => !['CANCELLED', 'COMPLETED'].includes(b.status)).map(b => (
            <div key={b.id} className="flex justify-between rounded-lg bg-rose-50 px-3 py-2 text-sm">
              <span>{format(new Date(b.bookingDate), 'EEE MMM d')} · {b.startTime?.slice(0, 5)} — {b.clientName}</span>
              <span className="text-rose-600">{statusLabel(b.status)}</span>
            </div>
          ))}
        </div>
        {bookedDates.size > 0 && <p className="mt-3 text-xs text-charcoal/40">{bookedDates.size} dates with bookings</p>}
      </div>
    </div>
  );
}

function ClientsTab({ clients, bookings, onSaved }: { clients: ClientProfile[]; bookings: Booking[]; onSaved: () => void }) {
  const [editing, setEditing] = useState<string | null>(null);
  const [form, setForm] = useState({ skinTone: '', allergies: '', notes: '' });

  const startEdit = (c: ClientProfile) => {
    setEditing(c.clientId);
    setForm({ skinTone: c.skinTone || '', allergies: c.allergies || '', notes: c.notes || '' });
  };

  const save = async (clientId: string) => {
    await api.dashboard.saveClient({ clientId, ...form });
    setEditing(null);
    onSaved();
  };

  if (clients.length === 0 && bookings.length === 0) {
    return <div className="card py-12 text-center text-charcoal/50">Client profiles appear after completed bookings</div>;
  }

  return (
    <div className="space-y-4">
      {clients.map(c => (
        <div key={c.id} className="card p-5">
          <div className="mb-3 flex items-center justify-between">
            <div className="flex items-center gap-2">
              <UserCircle className="h-5 w-5 text-rose-400" />
              <span className="font-semibold">{c.clientName}</span>
            </div>
            <button onClick={() => startEdit(c)} className="text-sm text-rose-600 hover:underline">Edit</button>
          </div>
          {editing === c.clientId ? (
            <div className="space-y-2">
              <select value={form.skinTone} onChange={e => setForm(f => ({ ...f, skinTone: e.target.value }))} className="input-field !py-2">
                <option value="">Skin tone</option>
                {['FAIR', 'LIGHT', 'MEDIUM', 'OLIVE', 'TAN', 'DEEP', 'DARK'].map(s => <option key={s} value={s}>{s}</option>)}
              </select>
              <input placeholder="Allergies" value={form.allergies} onChange={e => setForm(f => ({ ...f, allergies: e.target.value }))} className="input-field !py-2" />
              <textarea placeholder="Notes" value={form.notes} onChange={e => setForm(f => ({ ...f, notes: e.target.value }))} className="input-field !py-2" rows={2} />
              <button onClick={() => save(c.clientId)} className="btn-primary !py-2 text-xs">Save</button>
            </div>
          ) : (
            <div className="text-sm text-charcoal/60 space-y-1">
              {c.skinTone && <p>Skin: {c.skinTone}</p>}
              {c.allergies && <p>Allergies: {c.allergies}</p>}
              {c.notes && <p>{c.notes}</p>}
            </div>
          )}
        </div>
      ))}
    </div>
  );
}

function KitTab({ kit, onAdded }: { kit: KitItem[]; onAdded: (item: KitItem) => void }) {
  const [name, setName] = useState('');
  const [brand, setBrand] = useState('');
  const [quantity, setQuantity] = useState('1');
  const [expiry, setExpiry] = useState('');

  const submit = async (e: React.FormEvent) => {
    e.preventDefault();
    const item = await api.dashboard.addKit({ name, brand, quantity: Number(quantity), expiryDate: expiry || undefined, category: 'Makeup' });
    onAdded(item);
    setName(''); setBrand(''); setQuantity('1'); setExpiry('');
  };

  return (
    <div>
      <form onSubmit={submit} className="card mb-4 flex flex-wrap gap-3 p-4">
        <input placeholder="Product name" value={name} onChange={e => setName(e.target.value)} className="input-field !py-2 flex-1 min-w-[140px]" required />
        <input placeholder="Brand" value={brand} onChange={e => setBrand(e.target.value)} className="input-field !py-2 w-32" />
        <input type="number" placeholder="Qty" value={quantity} onChange={e => setQuantity(e.target.value)} className="input-field !py-2 w-20" />
        <input type="date" value={expiry} onChange={e => setExpiry(e.target.value)} className="input-field !py-2" />
        <button type="submit" className="btn-primary !py-2 !px-4 text-xs">Add</button>
      </form>
      <div className="space-y-3">
        {kit.map(k => (
          <div key={k.id} className="card flex items-center justify-between p-4">
            <div className="flex items-center gap-3">
              <Package className="h-5 w-5 text-charcoal/30" />
              <div>
                <p className="font-medium">{k.name}</p>
                <p className="text-xs text-charcoal/50">{k.brand} · Qty: {k.quantity}{k.expiryDate ? ` · Exp: ${k.expiryDate}` : ''}</p>
              </div>
            </div>
            {k.lowStockAlert && <span className="badge bg-amber-100 text-amber-700">Low stock</span>}
          </div>
        ))}
      </div>
    </div>
  );
}

function ProfileTab({ profile, onSaved }: { profile: MuaProfile; onSaved: () => void }) {
  const [form, setForm] = useState({
    displayName: profile.displayName,
    bio: profile.bio || '',
    city: profile.city,
    locality: profile.locality || '',
    minPrice: profile.minPrice?.toString() || '',
    maxPrice: profile.maxPrice?.toString() || '',
  });
  const [portfolioUrl, setPortfolioUrl] = useState('');
  const [serviceForm, setServiceForm] = useState({ name: '', price: '', durationMinutes: '90' });
  const [msg, setMsg] = useState('');

  const saveProfile = async () => {
    await api.muas.updateProfile({
      displayName: form.displayName,
      bio: form.bio,
      city: form.city,
      locality: form.locality,
      minPrice: form.minPrice ? Number(form.minPrice) : undefined,
      maxPrice: form.maxPrice ? Number(form.maxPrice) : undefined,
      occasions: profile.occasions,
      skinToneExpertise: profile.skinToneExpertise,
    });
    setMsg('Profile saved!');
    onSaved();
  };

  const addPortfolio = async () => {
    if (!portfolioUrl) return;
    await api.muas.addPortfolio({ imageUrl: portfolioUrl, caption: 'Portfolio look' });
    setPortfolioUrl('');
    setMsg('Portfolio item added!');
    onSaved();
  };

  const addService = async () => {
    await api.muas.addService({ name: serviceForm.name, price: Number(serviceForm.price), durationMinutes: Number(serviceForm.durationMinutes) });
    setServiceForm({ name: '', price: '', durationMinutes: '90' });
    setMsg('Service added!');
    onSaved();
  };

  return (
    <div className="grid gap-6 lg:grid-cols-2">
      <div className="card p-6">
        <h3 className="mb-4 flex items-center gap-2 font-semibold"><Settings className="h-4 w-4" /> Edit Profile</h3>
        {msg && <p className="mb-3 text-sm text-emerald-600">{msg}</p>}
        <div className="space-y-3">
          <input value={form.displayName} onChange={e => setForm(f => ({ ...f, displayName: e.target.value }))} className="input-field" placeholder="Display name" />
          <textarea value={form.bio} onChange={e => setForm(f => ({ ...f, bio: e.target.value }))} className="input-field" rows={3} placeholder="Bio" />
          <div className="flex gap-3">
            <input value={form.city} onChange={e => setForm(f => ({ ...f, city: e.target.value }))} className="input-field" placeholder="City" />
            <input value={form.locality} onChange={e => setForm(f => ({ ...f, locality: e.target.value }))} className="input-field" placeholder="Locality" />
          </div>
          <div className="flex gap-3">
            <input value={form.minPrice} onChange={e => setForm(f => ({ ...f, minPrice: e.target.value }))} className="input-field" placeholder="Min price ₹" />
            <input value={form.maxPrice} onChange={e => setForm(f => ({ ...f, maxPrice: e.target.value }))} className="input-field" placeholder="Max price ₹" />
          </div>
          <button onClick={saveProfile} className="btn-primary w-full">Save Profile</button>
        </div>
      </div>
      <div className="space-y-6">
        <div className="card p-6">
          <h3 className="mb-3 font-semibold">Add Portfolio Image</h3>
          <input value={portfolioUrl} onChange={e => setPortfolioUrl(e.target.value)} className="input-field mb-2" placeholder="Image URL (Unsplash link)" />
          <button onClick={addPortfolio} className="btn-secondary w-full">Add to Portfolio</button>
        </div>
        <div className="card p-6">
          <h3 className="mb-3 font-semibold">Add Service</h3>
          <input value={serviceForm.name} onChange={e => setServiceForm(f => ({ ...f, name: e.target.value }))} className="input-field mb-2" placeholder="Service name" />
          <div className="flex gap-2">
            <input value={serviceForm.price} onChange={e => setServiceForm(f => ({ ...f, price: e.target.value }))} className="input-field" placeholder="Price ₹" />
            <input value={serviceForm.durationMinutes} onChange={e => setServiceForm(f => ({ ...f, durationMinutes: e.target.value }))} className="input-field w-24" placeholder="Mins" />
          </div>
          <button onClick={addService} className="btn-secondary mt-2 w-full">Add Service</button>
        </div>
      </div>
    </div>
  );
}
