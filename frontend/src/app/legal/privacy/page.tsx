import type { Metadata } from 'next';
import LegalPageLayout from '@/components/LegalPageLayout';

export const metadata: Metadata = {
  title: 'Privacy Policy',
  description: 'How MakeupSeven collects, uses, and protects your personal data.',
};

export default function PrivacyPage() {
  return (
    <LegalPageLayout title="Privacy Policy" updated="28 June 2025">
      <p>
        MakeupSeven (&quot;we&quot;, &quot;us&quot;, &quot;our&quot;) operates a makeup-artist booking platform
        in Bengaluru, India. This Privacy Policy explains how we handle personal data in accordance with
        the Digital Personal Data Protection Act, 2023 (DPDP Act) and applicable Indian law.
      </p>

      <h2>1. Data we collect</h2>
      <ul>
        <li><strong>Account data:</strong> name, email address, phone number, password (stored hashed).</li>
        <li><strong>Booking data:</strong> appointment date/time, service type, notes, payment status.</li>
        <li><strong>Artist profile data:</strong> bio, portfolio images, pricing, location (city/locality).</li>
        <li><strong>Payment data:</strong> processed by Razorpay; we store order IDs and payment status, not full card numbers.</li>
        <li><strong>Technical data:</strong> IP address, browser type, device information (for security and analytics).</li>
      </ul>

      <h2>2. How we use your data</h2>
      <ul>
        <li>To create and manage your account and bookings.</li>
        <li>To send booking confirmations, reminders, and OTP codes via WhatsApp/SMS/email.</li>
        <li>To process payments and issue refunds where applicable.</li>
        <li>To improve search results and platform security.</li>
        <li>To comply with legal obligations.</li>
      </ul>

      <h2>3. Legal basis & consent</h2>
      <p>
        By registering or booking, you consent to our collection and use of data as described here.
        You may withdraw consent for marketing communications at any time by contacting us.
      </p>

      <h2>4. Data sharing</h2>
      <p>We share data only with:</p>
      <ul>
        <li>Makeup artists you book with (name, phone, booking details).</li>
        <li>Payment processor Razorpay (for transactions).</li>
        <li>WhatsApp/Meta (for notifications, when enabled).</li>
        <li>Cloudinary (for portfolio image hosting, when enabled).</li>
        <li>Law enforcement when required by law.</li>
      </ul>
      <p>We do not sell your personal data to third parties.</p>

      <h2>5. Data retention</h2>
      <p>
        Account and booking records are retained for up to 7 years for tax and dispute purposes,
        or until you request deletion (subject to legal hold requirements).
      </p>

      <h2>6. Your rights</h2>
      <p>Under the DPDP Act, you may:</p>
      <ul>
        <li>Access and correct your personal data.</li>
        <li>Request erasure of data no longer required.</li>
        <li>Nominate another person to exercise rights on your behalf in case of death/incapacity.</li>
        <li>Lodge a complaint with the Data Protection Board of India.</li>
      </ul>
      <p>Contact: <a href="mailto:privacy@makeupseven.com" className="text-rose-600">privacy@makeupseven.com</a></p>

      <h2>7. Security</h2>
      <p>
        We use HTTPS, JWT authentication, webhook signature verification, and encrypted database
        connections. No system is 100% secure; please use a strong password and keep OTP codes private.
      </p>

      <h2>8. Children</h2>
      <p>Our service is not directed at users under 18. We do not knowingly collect data from minors.</p>

      <h2>9. Changes</h2>
      <p>We may update this policy. Material changes will be posted on this page with a revised date.</p>
    </LegalPageLayout>
  );
}
