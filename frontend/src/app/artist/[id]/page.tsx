import ArtistPageLoader from './ArtistPageLoader';

export default function ArtistPage({ params }: { params: { id: string } }) {
  return <ArtistPageLoader artistId={params.id} />;
}
