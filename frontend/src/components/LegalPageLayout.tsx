import Link from 'next/link';

const LEGAL_LINKS = [
  { href: '/legal/privacy', label: 'Privacy Policy' },
  { href: '/legal/terms', label: 'Terms of Service' },
  { href: '/legal/refund', label: 'Refund & Cancellation' },
];

export default function LegalPageLayout({
  title,
  updated,
  children,
}: {
  title: string;
  updated: string;
  children: React.ReactNode;
}) {
  return (
    <article className="mx-auto max-w-3xl px-4 py-12">
      <nav className="mb-8 flex flex-wrap gap-4 text-sm text-charcoal/60">
        {LEGAL_LINKS.map(l => (
          <Link key={l.href} href={l.href} className="hover:text-rose-600">{l.label}</Link>
        ))}
      </nav>
      <h1 className="font-serif text-3xl font-bold">{title}</h1>
      <p className="mt-2 text-sm text-charcoal/50">Last updated: {updated}</p>
      <div className="prose prose-sm mt-8 max-w-none text-charcoal/80 [&_h2]:mt-8 [&_h2]:font-semibold [&_h2]:text-charcoal [&_li]:my-1 [&_p]:my-3 [&_ul]:list-disc [&_ul]:pl-5">
        {children}
      </div>
    </article>
  );
}
