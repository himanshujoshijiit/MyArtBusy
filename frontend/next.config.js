/** @type {import('next').NextConfig} */
const cdnHost = process.env.NEXT_PUBLIC_CDN_HOSTNAME || '';

const remotePatterns = [
  { protocol: 'https', hostname: 'images.unsplash.com' },
  { protocol: 'http', hostname: 'localhost', port: '8080' },
  { protocol: 'https', hostname: 'res.cloudinary.com' },
];

if (cdnHost) {
  remotePatterns.push({ protocol: 'https', hostname: cdnHost });
}

const nextConfig = {
  output: 'standalone',
  images: {
    remotePatterns,
    formats: ['image/webp'],
    deviceSizes: [640, 750, 828, 1080, 1200],
  },
  headers: async () => [
    {
      source: '/sw.js',
      headers: [{ key: 'Cache-Control', value: 'public, max-age=0, must-revalidate' }],
    },
  ],
};

module.exports = nextConfig;
