import { MetadataRoute } from 'next';

const API_URL = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080';
const BASE = process.env.NEXT_PUBLIC_SITE_URL || 'http://localhost:3000';

export default async function sitemap(): Promise<MetadataRoute.Sitemap> {
  const staticPages: MetadataRoute.Sitemap = [
    { url: BASE, changeFrequency: 'daily', priority: 1 },
    { url: `${BASE}/search`, changeFrequency: 'daily', priority: 0.9 },
    { url: `${BASE}/courses`, changeFrequency: 'weekly', priority: 0.7 },
    { url: `${BASE}/city/bengaluru`, changeFrequency: 'daily', priority: 0.9 },
    { url: `${BASE}/city/indiranagar`, changeFrequency: 'daily', priority: 0.8 },
    { url: `${BASE}/city/koramangala`, changeFrequency: 'daily', priority: 0.8 },
    { url: `${BASE}/legal/privacy`, changeFrequency: 'monthly', priority: 0.5 },
    { url: `${BASE}/legal/terms`, changeFrequency: 'monthly', priority: 0.5 },
    { url: `${BASE}/legal/refund`, changeFrequency: 'monthly', priority: 0.6 },
  ];

  try {
    const res = await fetch(`${API_URL}/api/muas`, { next: { revalidate: 3600 } });
    if (res.ok) {
      const muas: { id: string; displayName: string }[] = await res.json();
      return [
        ...staticPages,
        ...muas.map((m) => ({
          url: `${BASE}/artist/${m.id}`,
          changeFrequency: 'weekly' as const,
          priority: 0.8,
        })),
      ];
    }
  } catch {
    // fallback to static only
  }
  return staticPages;
}
