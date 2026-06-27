import type { Metadata } from 'next';

const API_URL = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080';

type Props = { params: { id: string }; children: React.ReactNode };

async function fetchArtist(id: string) {
  try {
    const res = await fetch(`${API_URL}/api/muas/${id}`, { next: { revalidate: 300 } });
    if (!res.ok) return null;
    return res.json();
  } catch {
    return null;
  }
}

export async function generateMetadata({ params }: { params: { id: string } }): Promise<Metadata> {
  const artist = await fetchArtist(params.id);
  if (!artist) {
    return { title: 'Makeup Artist — MakeupSeven' };
  }
  const title = `${artist.displayName} — Makeup Artist in ${artist.city} | MakeupSeven`;
  const description = artist.bio?.slice(0, 160) ||
    `Book ${artist.displayName} for bridal, party & editorial makeup in ${artist.locality || artist.city}. Rating ${artist.rating}/5.`;
  return {
    title,
    description,
    openGraph: {
      title,
      description,
      type: 'profile',
      images: artist.portfolio?.[0]?.imageUrl ? [{ url: artist.portfolio[0].imageUrl }] : [],
    },
    keywords: [
      `${artist.displayName}`,
      `makeup artist ${artist.city}`,
      `bridal makeup ${artist.locality || artist.city}`,
      'MakeupSeven',
    ],
  };
}

export default async function ArtistLayout({ params, children }: Props) {
  const artist = await fetchArtist(params.id);
  const jsonLd = artist ? {
    '@context': 'https://schema.org',
    '@type': 'LocalBusiness',
    name: artist.displayName,
    description: artist.bio,
    address: {
      '@type': 'PostalAddress',
      addressLocality: artist.locality || artist.city,
      addressRegion: artist.city,
      addressCountry: artist.country || 'IN',
    },
    aggregateRating: artist.reviewCount > 0 ? {
      '@type': 'AggregateRating',
      ratingValue: artist.rating,
      reviewCount: artist.reviewCount,
    } : undefined,
    priceRange: artist.minPrice && artist.maxPrice ? `₹${artist.minPrice}-₹${artist.maxPrice}` : undefined,
    image: artist.portfolio?.map((p: { imageUrl: string }) => p.imageUrl),
  } : null;

  return (
    <>
      {jsonLd && (
        <script
          type="application/ld+json"
          dangerouslySetInnerHTML={{ __html: JSON.stringify(jsonLd) }}
        />
      )}
      {children}
    </>
  );
}
