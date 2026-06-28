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

ALTER TABLE portfolio_items DROP CONSTRAINT IF EXISTS portfolio_items_occasion_check;
ALTER TABLE portfolio_items ADD CONSTRAINT portfolio_items_occasion_check CHECK (occasion::text = ANY (ARRAY[
  'WEDDING','BRIDAL','PARTY','GLAMOROUS','HALDI_MEHENDI',
  'EDITORIAL','FILM','PERSONAL_EVENT','ENGAGEMENT','RECEPTION'
]::text[]));

ALTER TABLE mua_services DROP CONSTRAINT IF EXISTS mua_services_occasion_check;
ALTER TABLE mua_services ADD CONSTRAINT mua_services_occasion_check CHECK (occasion IS NULL OR occasion::text = ANY (ARRAY[
  'WEDDING','BRIDAL','PARTY','GLAMOROUS','HALDI_MEHENDI',
  'EDITORIAL','FILM','PERSONAL_EVENT','ENGAGEMENT','RECEPTION'
]::text[]));
