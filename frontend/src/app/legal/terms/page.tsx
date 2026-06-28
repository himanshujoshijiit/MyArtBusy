import type { Metadata } from 'next';
import LegalPageLayout from '@/components/LegalPageLayout';

export const metadata: Metadata = {
  title: 'Terms of Service',
  description: 'Terms governing use of the MakeupSeven booking platform.',
};

export default function TermsPage() {
  return (
    <LegalPageLayout title="Terms of Service" updated="28 June 2025">
      <p>
        By accessing or using MakeupSeven, you agree to these Terms. If you do not agree, do not use the platform.
      </p>

      <h2>1. Service description</h2>
      <p>
        MakeupSeven connects clients with independent makeup artists (MUAs) in Bengaluru.
        We facilitate discovery, booking, and payment — we are not the service provider for makeup services.
      </p>

      <h2>2. Accounts</h2>
      <ul>
        <li>You must provide accurate registration information.</li>
        <li>You are responsible for keeping your login credentials secure.</li>
        <li>Artists must accurately represent skills, pricing, and availability.</li>
      </ul>

      <h2>3. Bookings & payments</h2>
      <ul>
        <li>A booking is confirmed once deposit payment is received (where applicable).</li>
        <li>Prices are set by individual artists and displayed before booking.</li>
        <li>Payments are processed by Razorpay; their terms also apply.</li>
        <li>Cancellation and refund rules are in our <a href="/legal/refund" className="text-rose-600">Refund & Cancellation Policy</a>.</li>
      </ul>

      <h2>4. Artist responsibilities</h2>
      <ul>
        <li>Arrive on time and deliver the agreed service professionally.</li>
        <li>Maintain hygiene standards and use safe products.</li>
        <li>Honour confirmed bookings or provide reasonable notice of cancellation.</li>
      </ul>

      <h2>5. Client responsibilities</h2>
      <ul>
        <li>Provide accurate event details and arrive prepared for the appointment.</li>
        <li>Pay agreed amounts on time.</li>
        <li>Treat artists with respect.</li>
      </ul>

      <h2>6. Reviews</h2>
      <p>Only clients with completed verified bookings may leave reviews. Fake or abusive reviews may be removed.</p>

      <h2>7. Limitation of liability</h2>
      <p>
        MakeupSeven is a platform, not a party to the service contract between client and artist.
        We are not liable for service quality, allergic reactions, or event outcomes.
        Our liability is limited to the platform fees paid to us in the preceding 12 months.
      </p>

      <h2>8. Disputes</h2>
      <p>
        Disputes between clients and artists should first be resolved directly.
        Unresolved disputes may be escalated to <a href="mailto:support@makeupseven.com" className="text-rose-600">support@makeupseven.com</a>.
        These Terms are governed by the laws of India; courts in Bengaluru, Karnataka have jurisdiction.
      </p>

      <h2>9. Termination</h2>
      <p>We may suspend accounts that violate these Terms or engage in fraud.</p>
    </LegalPageLayout>
  );
}
