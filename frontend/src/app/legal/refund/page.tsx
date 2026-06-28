import type { Metadata } from 'next';
import LegalPageLayout from '@/components/LegalPageLayout';

export const metadata: Metadata = {
  title: 'Refund & Cancellation Policy',
  description: 'MakeupSeven refund and cancellation policy for bookings and deposits.',
};

export default function RefundPage() {
  return (
    <LegalPageLayout title="Refund & Cancellation Policy" updated="28 June 2025">
      <p>
        This policy applies to all bookings made through MakeupSeven. It is published to comply with
        Razorpay merchant requirements and to set clear expectations for clients and artists.
      </p>

      <h2>1. Deposit & payment</h2>
      <ul>
        <li>Most bookings require a 25% deposit to confirm the appointment.</li>
        <li>The remaining balance is due as agreed with the artist (typically on the day of service).</li>
        <li>All online payments are processed securely via Razorpay.</li>
      </ul>

      <h2>2. Client cancellation</h2>
      <ul>
        <li><strong>More than 48 hours before appointment:</strong> Full deposit refund to original payment method within 5–7 business days.</li>
        <li><strong>Within 48 hours of appointment:</strong> Deposit is non-refundable (artist has blocked the date).</li>
        <li><strong>No-show:</strong> No refund; full service fee may still be owed to the artist.</li>
      </ul>

      <h2>3. Artist cancellation</h2>
      <ul>
        <li>If the artist cancels for any reason, the client receives a <strong>full refund</strong> of any amount paid.</li>
        <li>MakeupSeven will assist in finding an alternative artist where possible.</li>
      </ul>

      <h2>4. Rescheduling</h2>
      <p>
        Rescheduling is free if requested at least 48 hours in advance and subject to artist availability.
        Contact the artist via your booking dashboard or email support@makeupseven.com.
      </p>

      <h2>5. Trial sessions</h2>
      <p>
        Trial bookings are charged at 50% of the standard service price. The same 48-hour cancellation rule applies.
      </p>

      <h2>6. Refund processing</h2>
      <ul>
        <li>Refunds are initiated via Razorpay to the original payment method.</li>
        <li>Bank processing may take 5–7 business days after initiation.</li>
        <li>Refund status is visible in your booking dashboard.</li>
      </ul>

      <h2>7. Disputes</h2>
      <p>
        If you believe a refund was processed incorrectly, contact{' '}
        <a href="mailto:support@makeupseven.com" className="text-rose-600">support@makeupseven.com</a>{' '}
        within 7 days with your booking ID.
      </p>

      <h2>8. Force majeure</h2>
      <p>
        In case of government restrictions, natural disasters, or other events beyond control,
        bookings may be rescheduled without penalty. Refunds handled case-by-case.
      </p>
    </LegalPageLayout>
  );
}
