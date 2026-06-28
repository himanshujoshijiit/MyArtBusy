'use client';

import { useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import Image from 'next/image';
import Link from 'next/link';
import { api, Course, formatPrice } from '@/lib/api';
import { useAuth } from '@/lib/auth-context';
import { GraduationCap, Clock, Users, BookOpen, CheckCircle } from 'lucide-react';

declare global {
  interface Window {
    Razorpay: new (options: object) => { open: () => void };
  }
}

function loadRazorpay(): Promise<void> {
  return new Promise((resolve, reject) => {
    if (typeof window !== 'undefined' && 'Razorpay' in window) {
      resolve();
      return;
    }
    const script = document.createElement('script');
    script.src = 'https://checkout.razorpay.com/v1/checkout.js';
    script.onload = () => resolve();
    script.onerror = () => reject(new Error('Could not load payment gateway'));
    document.body.appendChild(script);
  });
}

export default function CoursesPage() {
  const router = useRouter();
  const { user, loading: authLoading } = useAuth();
  const [courses, setCourses] = useState<Course[]>([]);
  const [enrolledIds, setEnrolledIds] = useState<Set<string>>(new Set());
  const [loading, setLoading] = useState(true);
  const [enrollingId, setEnrollingId] = useState<string | null>(null);
  const [error, setError] = useState('');
  const [successMsg, setSuccessMsg] = useState('');

  useEffect(() => {
    api.courses.list().then(setCourses).finally(() => setLoading(false));
  }, []);

  useEffect(() => {
    if (authLoading || !user) return;
    api.courses.myEnrollments()
      .then(ids => setEnrolledIds(new Set(ids)))
      .catch(() => {});
  }, [user, authLoading]);

  const markEnrolled = (course: Course) => {
    setEnrolledIds(prev => new Set(prev).add(course.id));
    setCourses(prev => prev.map(c => (c.id === course.id ? course : c)));
  };

  const payWithRazorpay = async (course: Course, order: NonNullable<import('@/lib/api').EnrollResponse['payment']>) => {
    if (order.mock) {
      await api.courses.verifyPayment({
        orderId: order.orderId,
        paymentId: 'pay_mock_' + Date.now(),
        signature: 'mock',
      });
      markEnrolled(course);
      setSuccessMsg(`Enrolled in "${course.title}" successfully!`);
      return;
    }

    await loadRazorpay();
    return new Promise<void>((resolve, reject) => {
      const rzp = new window.Razorpay({
        key: order.keyId,
        amount: order.amount,
        currency: order.currency,
        order_id: order.orderId,
        name: 'MakeupSeven Academy',
        description: course.title,
        handler: async (response: { razorpay_order_id: string; razorpay_payment_id: string; razorpay_signature: string }) => {
          try {
            const result = await api.courses.verifyPayment({
              orderId: response.razorpay_order_id,
              paymentId: response.razorpay_payment_id,
              signature: response.razorpay_signature,
            });
            markEnrolled(result.course);
            setSuccessMsg(`Enrolled in "${course.title}" successfully!`);
            resolve();
          } catch (e) {
            reject(e);
          }
        },
        modal: { ondismiss: () => reject(new Error('Payment cancelled')) },
      });
      rzp.open();
    });
  };

  const enroll = async (course: Course) => {
    setError('');
    setSuccessMsg('');

    if (authLoading) return;
    if (!user) {
      router.push(`/login?redirect=${encodeURIComponent('/courses')}`);
      return;
    }

    if (enrolledIds.has(course.id)) return;

    setEnrollingId(course.id);
    try {
      const result = await api.courses.enroll(course.id);
      if (result.enrolled) {
        markEnrolled(result.course);
        setSuccessMsg(`Enrolled in "${course.title}" successfully!`);
      } else if (result.payment) {
        await payWithRazorpay(course, result.payment);
      } else {
        setError('Enrollment could not be completed. Please try again.');
      }
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Enrollment failed. Please log in and try again.');
    } finally {
      setEnrollingId(null);
    }
  };

  return (
    <div className="mx-auto max-w-6xl px-4 py-10">
      <div className="mb-10 text-center">
        <p className="mb-2 inline-flex items-center gap-2 rounded-full bg-rose-50 px-4 py-1 text-sm font-medium text-rose-600">
          <GraduationCap className="h-4 w-4" /> MakeupSeven Academy
        </p>
        <h1 className="font-display text-3xl font-bold sm:text-4xl">Learn from Top Artists</h1>
        <p className="mx-auto mt-3 max-w-xl text-charcoal/60">
          Online courses taught by verified MUAs. 70% goes to the instructor — support artists while you learn.
        </p>
        <Link href="/platform" className="mt-3 inline-block text-sm font-medium text-rose-600 hover:underline">
          View all platform features →
        </Link>
      </div>

      {error && (
        <div className="mb-6 rounded-xl bg-red-50 p-4 text-sm text-red-600">{error}</div>
      )}
      {successMsg && (
        <div className="mb-6 flex items-center gap-2 rounded-xl bg-emerald-50 p-4 text-sm text-emerald-700">
          <CheckCircle className="h-5 w-5 shrink-0" /> {successMsg}
        </div>
      )}

      {loading ? (
        <div className="grid gap-6 sm:grid-cols-2 lg:grid-cols-3">
          {[1, 2, 3].map(i => <div key={i} className="card h-80 animate-pulse bg-charcoal/5" />)}
        </div>
      ) : courses.length === 0 ? (
        <p className="text-center text-charcoal/50">No courses published yet.</p>
      ) : (
        <div className="grid gap-6 sm:grid-cols-2 lg:grid-cols-3">
          {courses.map(c => (
            <div key={c.id} className="card overflow-hidden">
              <div className="relative aspect-video">
                <Image src={c.thumbnailUrl || 'https://images.unsplash.com/photo-1522335789203-aabd1fc54bc9?w=600'} alt={c.title} fill className="object-cover" sizes="33vw" />
                {c.level && (
                  <span className="absolute left-3 top-3 rounded-full bg-white/90 px-2 py-0.5 text-xs font-medium">{c.level}</span>
                )}
              </div>
              <div className="p-5">
                <h3 className="font-display mb-1 text-lg font-semibold">{c.title}</h3>
                <p className="mb-3 text-sm text-charcoal/50">by {c.instructorName}</p>
                <p className="mb-4 line-clamp-2 text-sm text-charcoal/70">{c.description}</p>
                <div className="mb-4 flex gap-4 text-xs text-charcoal/50">
                  {c.durationHours && <span className="flex items-center gap-1"><Clock className="h-3 w-3" />{c.durationHours}h</span>}
                  <span className="flex items-center gap-1"><Users className="h-3 w-3" />{c.enrollmentCount} enrolled</span>
                </div>
                <div className="flex items-center justify-between">
                  <span className="text-lg font-bold text-rose-600">{formatPrice(Number(c.price))}</span>
                  {enrolledIds.has(c.id) ? (
                    <span className="text-sm font-medium text-emerald-600">Enrolled ✓</span>
                  ) : (
                    <button
                      onClick={() => enroll(c)}
                      disabled={enrollingId === c.id}
                      className="btn-primary !py-2 !px-4 text-xs disabled:opacity-60"
                    >
                      <BookOpen className="h-3 w-3" />
                      {enrollingId === c.id ? 'Processing…' : 'Enroll'}
                    </button>
                  )}
                </div>
              </div>
            </div>
          ))}
        </div>
      )}

      <div className="mt-12 card bg-charcoal p-8 text-center text-white">
        <h2 className="font-display mb-2 text-xl font-bold">Want to teach on MakeupSeven?</h2>
        <p className="mb-4 text-white/70">Artists earn 70% of course revenue. Contact us to publish your course.</p>
        <Link href="/register?role=MUA" className="btn-gold">Become an Instructor</Link>
      </div>
    </div>
  );
}
