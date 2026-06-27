-- Add GLAMOROUS and HALDI_MEHENDI to occasion check constraints (Hibernate ddl-auto does not update these)

ALTER TABLE bookings DROP CONSTRAINT IF EXISTS bookings_occasion_check;
ALTER TABLE bookings ADD CONSTRAINT bookings_occasion_check CHECK (occasion::text = ANY (ARRAY[
  'WEDDING','BRIDAL','PARTY','GLAMOROUS','HALDI_MEHENDI',
  'EDITORIAL','FILM','PERSONAL_EVENT','ENGAGEMENT','RECEPTION'
]::text[]));

ALTER TABLE quote_requests DROP CONSTRAINT IF EXISTS quote_requests_occasion_check;
ALTER TABLE quote_requests ADD CONSTRAINT quote_requests_occasion_check CHECK (occasion::text = ANY (ARRAY[
  'WEDDING','BRIDAL','PARTY','GLAMOROUS','HALDI_MEHENDI',
  'EDITORIAL','FILM','PERSONAL_EVENT','ENGAGEMENT','RECEPTION'
]::text[]));
