'use client';

import { useEffect, useState } from 'react';
import Image from 'next/image';
import Link from 'next/link';
import { api, Course, formatPrice } from '@/lib/api';
import { useAuth } from '@/lib/auth-context';
import { GraduationCap, Clock, Users, BookOpen } from 'lucide-react';

export default function CoursesPage() {
  const { user } = useAuth();
  const [courses, setCourses] = useState<Course[]>([]);
  const [loading, setLoading] = useState(true);
  const [enrolled, setEnrolled] = useState<string | null>(null);

  useEffect(() => {
    api.courses.list().then(setCourses).finally(() => setLoading(false));
  }, []);

  const enroll = async (id: string) => {
    if (!user) { window.location.href = '/login?redirect=/courses'; return; }
    await api.courses.enroll(id);
    setEnrolled(id);
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
      </div>

      {loading ? (
        <div className="grid gap-6 sm:grid-cols-2 lg:grid-cols-3">
          {[1, 2, 3].map(i => <div key={i} className="card h-80 animate-pulse bg-charcoal/5" />)}
        </div>
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
                  {enrolled === c.id ? (
                    <span className="text-sm font-medium text-emerald-600">Enrolled ✓</span>
                  ) : (
                    <button onClick={() => enroll(c.id)} className="btn-primary !py-2 !px-4 text-xs">
                      <BookOpen className="h-3 w-3" /> Enroll
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
