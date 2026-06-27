'use client';

import { Suspense } from 'react';
import LoginContent from './LoginContent';

export default function LoginPage() {
  return (
    <Suspense fallback={<div className="py-20 text-center">Loading...</div>}>
      <LoginContent />
    </Suspense>
  );
}
