import { AuthProvider } from '@/lib/auth-context';
import Navbar from '@/components/Navbar';
import './globals.css';

export const metadata = {
  title: 'MakeupSeven — Book Top Makeup Artists',
  description: 'Discover and book verified makeup artists by occasion. Bridal, party, editorial & more.',
};

export default function RootLayout({ children }: { children: React.ReactNode }) {
  return (
    <html lang="en">
      <body>
        <AuthProvider>
          <Navbar />
          <main>{children}</main>
        </AuthProvider>
      </body>
    </html>
  );
}
