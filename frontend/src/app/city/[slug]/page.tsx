import Link from 'next/link';
import { Metadata } from 'next';

const CITY_DATA: Record<string, { title: string; description: string; locality?: string }> = {
  bengaluru: {
    title: 'Makeup Artists in Bengaluru',
    description: 'Book verified bridal, party & editorial makeup artists across Bengaluru. Top-rated MUAs with instant booking.',
  },
  indiranagar: {
    title: 'Makeup Artists in Indiranagar, Bengaluru',
    description: 'Find top makeup artists near Indiranagar for bridal, party and engagement looks.',
    locality: 'Indiranagar',
  },
  koramangala: {
    title: 'Makeup Artists in Koramangala, Bengaluru',
    description: 'Book Koramangala\'s best makeup artists for weddings, parties and photoshoots.',
    locality: 'Koramangala',
  },
  jayanagar: {
    title: 'Makeup Artists in Jayanagar',
    description: 'Affordable and premium makeup artists in Jayanagar, Bengaluru.',
    locality: 'Jayanagar',
  },
  whitefield: {
    title: 'Makeup Artists in Whitefield',
    description: 'Film, TV and bridal makeup artists in Whitefield, Bengaluru.',
    locality: 'Whitefield',
  },
};

type Props = { params: { slug: string } };

export async function generateMetadata({ params }: Props): Promise<Metadata> {
  const data = CITY_DATA[params.slug] || CITY_DATA.bengaluru;
  return { title: `${data.title} | MakeupSeven`, description: data.description };
}

export default function CityPage({ params }: Props) {
  const data = CITY_DATA[params.slug] || CITY_DATA.bengaluru;
  const searchParams = new URLSearchParams({ city: 'Bengaluru' });
  if (data.locality) searchParams.set('locality', data.locality);

  return (
    <div className="mx-auto max-w-4xl px-4 py-12">
      <h1 className="font-serif text-4xl font-bold">{data.title}</h1>
      <p className="mt-4 text-lg text-charcoal/70">{data.description}</p>
      <div className="mt-8 flex flex-wrap gap-4">
        <Link href={`/search?${searchParams.toString()}`} className="btn-primary">
          Browse Artists →
        </Link>
        <Link href="/register?role=MUA" className="btn-secondary">
          Join as MUA
        </Link>
      </div>
      <section className="mt-12">
        <h2 className="text-xl font-semibold">Popular in {data.locality || 'Bengaluru'}</h2>
        <ul className="mt-4 space-y-2 text-charcoal/70">
          <li>Bridal makeup — HD & airbrush specialists</li>
          <li>Engagement & reception looks</li>
          <li>Party glam & editorial makeup</li>
          <li>Trial sessions before your wedding</li>
        </ul>
      </section>
    </div>
  );
}
