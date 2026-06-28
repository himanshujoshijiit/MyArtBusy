/** Platform features & architecture content */

export const PLATFORM_FEATURES = [
  {
    category: 'For Clients',
    items: [
      { title: 'Search & Book', desc: 'Find artists by city, occasion, skin tone, and portfolio. Book slots with real-time availability.' },
      { title: 'Secure Payments', desc: 'Pay booking deposit via Razorpay (UPI, cards). Mock mode for local demo without keys.' },
      { title: 'WhatsApp Alerts', desc: 'Booking confirmations and reminders sent to your phone.' },
      { title: 'E-Contracts', desc: 'Digitally sign service agreements after deposit payment.' },
      { title: 'Custom Quotes', desc: 'Request personalised quotes for multi-day bridal packages.' },
      { title: 'Trial Sessions', desc: 'Book a trial makeup session at 50% price before your event.' },
      { title: 'Reviews', desc: 'Leave verified reviews after completed bookings.' },
      { title: 'Referrals', desc: 'Share referral codes and earn rewards on friend bookings.' },
    ],
  },
  {
    category: 'For Artists (Priya Prachi — studio owner)',
    items: [
      { title: 'Owner Dashboard', desc: 'Bookings, earnings, calendar, and client profiles in one place.' },
      { title: 'Profile & Portfolio', desc: 'Onboarding wizard — bio, services, pricing, and photo gallery.' },
      { title: 'Availability Calendar', desc: 'Manage open slots and block dates for holidays or travel.' },
      { title: 'Kit Inventory', desc: 'Track makeup products, expiry dates, and restock alerts.' },
      { title: 'Quote Management', desc: 'Respond to custom quote requests from clients.' },
      { title: 'MakeupSeven Pro', desc: 'Subscription tier for unlimited bookings and premium visibility.' },
    ],
  },
  {
    category: 'Academy & Learning',
    items: [
      { title: 'Online Courses', desc: 'Learn bridal, editorial, and beginner makeup from verified instructors.' },
      { title: 'Enroll & Pay', desc: 'Enroll with login; paid courses checkout via Razorpay (demo/mock when keys absent).' },
      { title: 'Instructor Revenue', desc: '70% of course fees go to the instructor.' },
    ],
  },
  {
    category: 'Platform & Ops',
    items: [
      { title: 'Single-Artist Mode', desc: 'Launch with one studio owner (Priya Prachi); other demo profiles deactivated.' },
      { title: 'Admin Panel', desc: 'Platform admin for moderation and oversight.' },
      { title: 'Legal Pages', desc: 'Privacy, Terms, and Refund policies for Razorpay KYC compliance.' },
      { title: 'PWA', desc: 'Installable mobile app with offline-ready shell.' },
      { title: 'SEO', desc: 'City pages, sitemap, JSON-LD structured data for artist profiles.' },
    ],
  },
] as const;

export const ARCHITECTURE_LAYERS = [
  { name: 'Website', tech: 'Next.js 14', role: 'Homepage, search, booking, dashboard, academy, legal pages' },
  { name: 'Booking API', tech: 'Spring Boot (Java)', role: 'Auth, bookings, payments, contracts, courses, dashboard' },
  { name: 'Search & Alerts', tech: 'FastAPI (Python)', role: 'Artist search, WhatsApp/email notifications, analytics' },
  { name: 'Database', tech: 'PostgreSQL 16', role: 'Users, bookings, payments, courses, enrollments' },
  { name: 'Payments', tech: 'Razorpay', role: 'Booking deposits and academy course checkout' },
  { name: 'Production', tech: 'Docker + Caddy', role: 'HTTPS, firewall, daily DB backups' },
] as const;

export const OWNER = {
  name: 'Priya Prachi',
  email: 'priya@priyaprachi.com',
  password: 'priya123',
} as const;
