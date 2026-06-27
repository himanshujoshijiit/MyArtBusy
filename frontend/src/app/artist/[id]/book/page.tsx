'use client';

import { Suspense } from 'react';
import BookPageContent from './BookPageContent';

export default function BookPage() {
  return (
    <Suspense fallback={<div className="py-20 text-center">Loading...</div>}>
      <BookPageContent />
    </Suspense>
  );
}
