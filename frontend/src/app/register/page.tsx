'use client';

import { Suspense } from 'react';
import RegisterContent from './RegisterContent';

export default function RegisterPage() {
  return (
    <Suspense fallback={<div className="py-20 text-center">Loading...</div>}>
      <RegisterContent />
    </Suspense>
  );
}
