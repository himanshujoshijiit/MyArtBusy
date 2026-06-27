'use client';

import { useEffect, useState } from 'react';
import { useParams, useRouter } from 'next/navigation';
import Link from 'next/link';
import { api, Contract, formatPrice } from '@/lib/api';
import { useAuth } from '@/lib/auth-context';
import { FileText, CheckCircle, PenLine } from 'lucide-react';

export default function ContractPage() {
  const { id } = useParams<{ id: string }>();
  const { user } = useAuth();
  const router = useRouter();
  const [contract, setContract] = useState<Contract | null>(null);
  const [signature, setSignature] = useState('');
  const [loading, setLoading] = useState(true);
  const [signing, setSigning] = useState(false);

  useEffect(() => {
    if (!user) { router.push(`/login?redirect=/contracts/${id}`); return; }
    api.contracts.get(id).then(setContract).catch(() => setContract(null)).finally(() => setLoading(false));
  }, [id, user, router]);

  const sign = async () => {
    if (!signature.trim()) return;
    setSigning(true);
    const updated = await api.contracts.sign(id, signature.trim());
    setContract(updated);
    setSigning(false);
  };

  if (loading) return <div className="py-20 text-center">Loading contract...</div>;
  if (!contract) return <div className="py-20 text-center">Contract not found</div>;

  const isClient = user?.role === 'CLIENT';

  return (
    <div className="mx-auto max-w-2xl px-4 py-10">
      <div className="mb-6 flex items-center gap-3">
        <FileText className="h-8 w-8 text-rose-500" />
        <div>
          <h1 className="font-display text-2xl font-bold">Service Agreement</h1>
          <p className="text-sm text-charcoal/50">{contract.muaName} · {contract.serviceName}</p>
        </div>
      </div>

      <div className="card mb-6 p-6">
        <div className="mb-4 grid grid-cols-2 gap-4 text-sm">
          <div><span className="text-charcoal/50">Client</span><p className="font-medium">{contract.clientName}</p></div>
          <div><span className="text-charcoal/50">Artist</span><p className="font-medium">{contract.muaName}</p></div>
          <div><span className="text-charcoal/50">Date</span><p className="font-medium">{contract.bookingDate} at {contract.startTime?.slice(0, 5)}</p></div>
          <div><span className="text-charcoal/50">Total</span><p className="font-medium">{formatPrice(Number(contract.totalAmount))}</p></div>
          <div><span className="text-charcoal/50">Deposit paid</span><p className="font-medium text-emerald-600">{formatPrice(Number(contract.depositAmount))}</p></div>
          <div><span className="text-charcoal/50">Balance due</span><p className="font-medium">{formatPrice(Number(contract.remainingAmount))}</p></div>
        </div>
        <pre className="whitespace-pre-wrap rounded-xl bg-charcoal/5 p-4 text-xs leading-relaxed text-charcoal/70">{contract.terms}</pre>
      </div>

      {contract.signed ? (
        <div className="card flex items-center gap-3 p-6 text-emerald-700">
          <CheckCircle className="h-6 w-6" />
          <div>
            <p className="font-semibold">Contract Signed</p>
            <p className="text-sm">Digitally signed and stored securely</p>
          </div>
        </div>
      ) : isClient ? (
        <div className="card p-6">
          <h3 className="mb-3 flex items-center gap-2 font-semibold"><PenLine className="h-4 w-4" /> E-Sign Contract</h3>
          <p className="mb-4 text-sm text-charcoal/50">Type your full name to digitally sign this agreement</p>
          <input value={signature} onChange={e => setSignature(e.target.value)} className="input-field mb-3" placeholder="Your full name" />
          <button onClick={sign} disabled={signing || !signature.trim()} className="btn-primary w-full">
            {signing ? 'Signing...' : 'Sign Contract'}
          </button>
        </div>
      ) : (
        <p className="text-center text-sm text-charcoal/50">Waiting for client to sign</p>
      )}

      <div className="mt-6 text-center">
        <Link href="/bookings" className="text-sm text-rose-600 hover:underline">← Back to bookings</Link>
      </div>
    </div>
  );
}
