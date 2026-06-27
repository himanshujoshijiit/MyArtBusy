import { AuthProvider } from '@/lib/auth-context';
import Navbar from '@/components/Navbar';
import PwaRegister from '@/components/PwaRegister';
import './globals.css';
import type { Metadata, Viewport } from 'next';

export const metadata: Metadata = {
  title: {
    default: 'MakeupSeven — Book Top Makeup Artists in Bengaluru',
    template: '%s | MakeupSeven',
  },
  description: 'Discover and book verified makeup artists by occasion. Bridal, party, editorial & more. PWA-ready mobile booking.',
  keywords: ['makeup artist Bengaluru', 'bridal makeup', 'MUA booking', 'MakeupSeven'],
  manifest: '/manifest.json',
  appleWebApp: { capable: true, statusBarStyle: 'default', title: 'MakeupSeven' },
  openGraph: {
    type: 'website',
    locale: 'en_IN',
    siteName: 'MakeupSeven',
  },
};

export const viewport: Viewport = {
  themeColor: '#e11d5a',
  width: 'device-width',
  initialScale: 1,
  maximumScale: 5,
};

export default function RootLayout({ children }: { children: React.ReactNode }) {
  return (
    <html lang="en">
      <head>
        <link rel="apple-touch-icon" href="/icons/icon-192.png" />
      </head>
      <body className="pb-safe">
        <AuthProvider>
          <PwaRegister />
          <Navbar />
          <main>{children}</main>
        </AuthProvider>
      </body>
    </html>
  );
}
