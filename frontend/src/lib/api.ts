const API_URL = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080';
const SEARCH_URL = process.env.NEXT_PUBLIC_SEARCH_URL || 'http://localhost:8000';

async function searchFetch<T>(url: string, options: RequestInit = {}): Promise<T> {
  try {
    return await apiFetch<T>(url, options);
  } catch {
    // Fallback: if Python search is down, use Java MUA list
    if (url.includes(SEARCH_URL)) {
      const muas = await apiFetch<MuaProfile[]>(`${API_URL}/api/muas`);
      return {
        results: muas.map(m => ({
          id: m.id,
          display_name: m.displayName,
          bio: m.bio,
          city: m.city,
          locality: m.locality,
          occasions: m.occasions,
          skin_tone_expertise: m.skinToneExpertise,
          min_price: m.minPrice,
          max_price: m.maxPrice,
          rating: m.rating,
          review_count: m.reviewCount,
          total_bookings: m.totalBookings,
          top_artist: m.topArtist,
          verified: m.verified,
          featured: m.featured,
          response_time_label: m.responseTimeLabel || '',
          portfolio_preview: m.portfolio?.map(p => p.imageUrl) || [],
          relevance_score: m.rating * 20,
        })),
        total: muas.length,
        query_summary: 'All artists (fallback)',
      } as T;
    }
    throw new Error('Service unavailable');
  }
}

export function getToken(): string | null {
  if (typeof window === 'undefined') return null;
  return localStorage.getItem('token');
}

export function setAuth(token: string, user: AuthUser) {
  localStorage.setItem('token', token);
  localStorage.setItem('user', JSON.stringify(user));
}

export function clearAuth() {
  localStorage.removeItem('token');
  localStorage.removeItem('user');
}

export function getUser(): AuthUser | null {
  if (typeof window === 'undefined') return null;
  const raw = localStorage.getItem('user');
  return raw ? JSON.parse(raw) : null;
}

export interface AuthUser {
  userId: string;
  email: string;
  fullName: string;
  role: 'CLIENT' | 'MUA' | 'ADMIN';
  muaProfileId?: string;
}

async function apiFetch<T>(url: string, options: RequestInit = {}): Promise<T> {
  const token = getToken();
  const headers: Record<string, string> = {
    'Content-Type': 'application/json',
    ...(options.headers as Record<string, string>),
  };
  if (token) headers['Authorization'] = `Bearer ${token}`;

  const res = await fetch(url, { ...options, headers });
  if (!res.ok) {
    const err = await res.json().catch(() => ({ error: 'Request failed' }));
    throw new Error(err.error || `HTTP ${res.status}`);
  }
  return res.json();
}

export const api = {
  auth: {
    register: (data: object) => apiFetch<AuthUser & { token: string; refreshToken?: string }>(`${API_URL}/api/auth/register`, { method: 'POST', body: JSON.stringify(data) }),
    login: (data: object) => apiFetch<AuthUser & { token: string; refreshToken?: string }>(`${API_URL}/api/auth/login`, { method: 'POST', body: JSON.stringify(data) }),
    refresh: (refreshToken: string) => apiFetch<AuthUser & { token: string }>(`${API_URL}/api/auth/refresh`, { method: 'POST', body: JSON.stringify({ refreshToken }) }),
    sendOtp: (phone: string) => apiFetch<{ message: string }>(`${API_URL}/api/auth/otp/send`, { method: 'POST', body: JSON.stringify({ phone }) }),
    verifyOtp: (data: object) => apiFetch<AuthUser & { token: string }>(`${API_URL}/api/auth/otp/verify`, { method: 'POST', body: JSON.stringify(data) }),
    google: (data: object) => apiFetch<AuthUser & { token: string }>(`${API_URL}/api/auth/google`, { method: 'POST', body: JSON.stringify(data) }),
    completeOnboarding: (data: object) => apiFetch<MuaProfile>(`${API_URL}/api/auth/onboarding`, { method: 'POST', body: JSON.stringify(data) }),
  },
  muas: {
    getAll: () => apiFetch<MuaProfile[]>(`${API_URL}/api/muas`),
    getById: (id: string) => apiFetch<MuaProfile>(`${API_URL}/api/muas/${id}`),
    getMyProfile: () => apiFetch<MuaProfile>(`${API_URL}/api/muas/profile/me`),
    getTop: () => apiFetch<MuaProfile[]>(`${API_URL}/api/muas/top`),
    getByCity: (city: string) => apiFetch<MuaProfile[]>(`${API_URL}/api/muas/city/${city}`),
    updateProfile: (data: object) => apiFetch<MuaProfile>(`${API_URL}/api/muas/profile`, { method: 'PUT', body: JSON.stringify(data) }),
    addPortfolio: (data: object) => apiFetch<{ id: string; imageUrl: string; caption?: string; occasion?: string }>(`${API_URL}/api/muas/portfolio`, { method: 'POST', body: JSON.stringify(data) }),
    addService: (data: object) => apiFetch<{ id: string; name: string; price: number; description?: string; durationMinutes?: number; occasion?: string; category?: string }>(`${API_URL}/api/muas/services`, { method: 'POST', body: JSON.stringify(data) }),
  },
  search: {
    query: (data: SearchParams) => searchFetch<SearchResponse>(`${SEARCH_URL}/api/search`, { method: 'POST', body: JSON.stringify(data) }),
    occasions: () => apiFetch<{ value: string; label: string }[]>(`${SEARCH_URL}/api/search/occasions`),
    cities: () => apiFetch<{ city: string; country: string }[]>(`${SEARCH_URL}/api/search/cities`),
    localities: (city: string) => apiFetch<{ locality: string; pincode: string }[]>(`${SEARCH_URL}/api/search/localities/${encodeURIComponent(city)}`),
  },
  bookings: {
    create: (data: object) => apiFetch<Booking>(`${API_URL}/api/bookings`, { method: 'POST', body: JSON.stringify(data) }),
    get: (id: string) => apiFetch<Booking>(`${API_URL}/api/bookings/${id}`),
    my: () => apiFetch<Booking[]>(`${API_URL}/api/bookings/my`),
    mua: () => apiFetch<Booking[]>(`${API_URL}/api/bookings/mua`),
    confirm: (id: string) => apiFetch<Booking>(`${API_URL}/api/bookings/${id}/confirm`, { method: 'POST' }),
    complete: (id: string) => apiFetch<Booking>(`${API_URL}/api/bookings/${id}/complete`, { method: 'POST' }),
    cancel: (id: string) => apiFetch<Booking>(`${API_URL}/api/bookings/${id}/cancel`, { method: 'POST' }),
    availability: (muaId: string, start: string, end: string) =>
      apiFetch<AvailabilitySlot[]>(`${API_URL}/api/bookings/availability/${muaId}?start=${start}&end=${end}`),
    addAvailability: (data: object) => apiFetch<AvailabilitySlot>(`${API_URL}/api/bookings/availability`, { method: 'POST', body: JSON.stringify(data) }),
  },
  payments: {
    createDeposit: (bookingId: string) => apiFetch<PaymentOrder>(`${API_URL}/api/payments/deposit/${bookingId}`, { method: 'POST' }),
    verify: (data: object) => apiFetch<{ status: string }>(`${API_URL}/api/payments/verify`, { method: 'POST', body: JSON.stringify(data) }),
  },
  contracts: {
    get: (bookingId: string) => apiFetch<Contract>(`${API_URL}/api/contracts/${bookingId}`),
    sign: (bookingId: string, signatureName: string) =>
      apiFetch<Contract>(`${API_URL}/api/contracts/${bookingId}/sign`, { method: 'POST', body: JSON.stringify({ signatureName }) }),
  },
  subscription: {
    createProOrder: () => apiFetch<PaymentOrder>(`${API_URL}/api/subscription/pro/order`, { method: 'POST' }),
    activatePro: () => apiFetch<{ status: string; tier: string }>(`${API_URL}/api/subscription/pro/activate`, { method: 'POST' }),
  },
  reviews: {
    create: (data: object) => apiFetch<Review>(`${API_URL}/api/reviews`, { method: 'POST', body: JSON.stringify(data) }),
    getMua: (muaId: string) => apiFetch<Review[]>(`${API_URL}/api/reviews/mua/${muaId}`),
  },
  courses: {
    list: () => apiFetch<Course[]>(`${API_URL}/api/courses`),
    get: (id: string) => apiFetch<Course>(`${API_URL}/api/courses/${id}`),
    enroll: (id: string) => apiFetch<Course>(`${API_URL}/api/courses/${id}/enroll`, { method: 'POST' }),
  },
  dashboard: {
    stats: () => apiFetch<DashboardStats>(`${API_URL}/api/dashboard/stats`),
    clients: () => apiFetch<ClientProfile[]>(`${API_URL}/api/dashboard/clients`),
    saveClient: (data: object) => apiFetch<ClientProfile>(`${API_URL}/api/dashboard/clients`, { method: 'POST', body: JSON.stringify(data) }),
    kit: () => apiFetch<KitItem[]>(`${API_URL}/api/dashboard/kit`),
    addKit: (data: object) => apiFetch<KitItem>(`${API_URL}/api/dashboard/kit`, { method: 'POST', body: JSON.stringify(data) }),
  },
  quotes: {
    create: (data: object) => apiFetch<QuoteRequest>(`${API_URL}/api/quotes`, { method: 'POST', body: JSON.stringify(data) }),
    my: () => apiFetch<QuoteRequest[]>(`${API_URL}/api/quotes/my`),
    mua: () => apiFetch<QuoteRequest[]>(`${API_URL}/api/quotes/mua`),
    respond: (id: string, data: object) => apiFetch<QuoteRequest>(`${API_URL}/api/quotes/${id}/respond`, { method: 'POST', body: JSON.stringify(data) }),
  },
  referrals: {
    my: () => apiFetch<ReferralInfo>(`${API_URL}/api/referrals/my`),
    apply: (code: string) => apiFetch<ReferralInfo>(`${API_URL}/api/referrals/apply`, { method: 'POST', body: JSON.stringify({ code }) }),
  },
  blockedDates: {
    list: () => apiFetch<BlockedDate[]>(`${API_URL}/api/blocked-dates`),
    add: (data: object) => apiFetch<BlockedDate>(`${API_URL}/api/blocked-dates`, { method: 'POST', body: JSON.stringify(data) }),
    remove: (date: string) => apiFetch<void>(`${API_URL}/api/blocked-dates/${date}`, { method: 'DELETE' }),
  },
  upload: {
    image: async (file: File) => {
      const token = getToken();
      const form = new FormData();
      form.append('file', file);
      const res = await fetch(`${API_URL}/api/upload/image`, {
        method: 'POST',
        headers: token ? { Authorization: `Bearer ${token}` } : {},
        body: form,
      });
      if (!res.ok) throw new Error('Upload failed');
      return res.json() as Promise<{ url: string }>;
    },
  },
  admin: {
    stats: () => apiFetch<AdminStats>(`${API_URL}/api/admin/stats`),
    muas: () => apiFetch<AdminMua[]>(`${API_URL}/api/admin/muas`),
    verifyMua: (id: string) => apiFetch<void>(`${API_URL}/api/admin/muas/${id}/verify`, { method: 'POST' }),
    featureMua: (id: string, featured: boolean) => apiFetch<void>(`${API_URL}/api/admin/muas/${id}/feature?featured=${featured}`, { method: 'POST' }),
  },
  health: {
    check: async () => {
      const [java, python] = await Promise.allSettled([
        fetch(`${API_URL}/actuator/health`),
        fetch(`${SEARCH_URL}/health`),
      ]);
      return {
        java: java.status === 'fulfilled' && java.value.ok,
        python: python.status === 'fulfilled' && python.value.ok,
      };
    },
  },
};

export interface MuaProfile {
  id: string;
  userId: string;
  displayName: string;
  bio?: string;
  city: string;
  locality?: string;
  country?: string;
  occasions: string[];
  skinToneExpertise: string[];
  minPrice?: number;
  maxPrice?: number;
  rating: number;
  reviewCount: number;
  totalBookings: number;
  topArtist: boolean;
  verified: boolean;
  featured: boolean;
  responseTimeLabel?: string;
  onboardingComplete?: boolean;
  pincode?: string;
  latitude?: number;
  longitude?: number;
  subscriptionTier: string;
  portfolio: { id: string; imageUrl: string; caption?: string; occasion?: string }[];
  services: { id: string; name: string; description?: string; price: number; durationMinutes?: number; occasion?: string; category?: string }[];
}

export interface SearchParams {
  city?: string;
  locality?: string;
  occasion?: string;
  skin_tone?: string;
  min_budget?: number;
  max_budget?: number;
  min_rating?: number;
  available_date?: string;
  top_artist_only?: boolean;
  pincode?: string;
  latitude?: number;
  longitude?: number;
  radius_km?: number;
  sort_by?: string;
  page?: number;
}

export interface SearchResult {
  id: string;
  display_name: string;
  bio?: string;
  city: string;
  locality?: string;
  occasions: string[];
  skin_tone_expertise: string[];
  min_price?: number;
  max_price?: number;
  rating: number;
  review_count: number;
  total_bookings: number;
  top_artist: boolean;
  verified: boolean;
  featured: boolean;
  response_time_label: string;
  portfolio_preview: string[];
  relevance_score: number;
  distance_km?: number;
  pincode?: string;
}

export interface SearchResponse {
  results: SearchResult[];
  total: number;
  query_summary: string;
}

export interface Booking {
  id: string;
  clientId: string;
  clientName: string;
  muaId: string;
  muaName: string;
  serviceId?: string;
  serviceName?: string;
  bookingDate: string;
  startTime: string;
  endTime?: string;
  occasion?: string;
  notes?: string;
  totalAmount: number;
  depositAmount: number;
  commissionAmount?: number;
  remainingAmount?: number;
  bookingType?: string;
  refundAmount?: number;
  status: string;
  paymentStatus: string;
  razorpayOrderId?: string;
  contractUrl?: string;
  contractSigned?: boolean;
  contractSignedAt?: string;
  createdAt: string;
}

export interface AvailabilitySlot {
  id: string;
  slotDate: string;
  startTime: string;
  endTime: string;
  available: boolean;
}

export interface PaymentOrder {
  orderId: string;
  amount: number;
  currency: string;
  keyId: string;
  mock?: boolean;
}

export interface Contract {
  bookingId: string;
  clientName: string;
  muaName: string;
  serviceName: string;
  bookingDate: string;
  startTime: string;
  totalAmount: number;
  depositAmount: number;
  remainingAmount: number;
  terms: string;
  contractSignedAt?: string;
  signed: boolean;
}

export interface Review {
  id: string;
  bookingId: string;
  clientName: string;
  rating: number;
  comment?: string;
  verified: boolean;
  createdAt: string;
}

export interface Course {
  id: string;
  title: string;
  description: string;
  instructorName: string;
  thumbnailUrl?: string;
  price: number;
  durationHours?: number;
  level?: string;
  enrollmentCount: number;
}

export interface DashboardStats {
  totalBookings: number;
  monthlyBookings: number;
  pendingBookings: number;
  totalEarnings: number;
  monthlyEarnings: number;
  netEarnings: number;
  monthlyNetEarnings: number;
  totalCommission: number;
  averageRating: number;
  reviewCount: number;
  subscription: { tier: string; bookingsUsed: number; bookingsLimit?: number };
  kitAlerts: { id: string; name: string; alertType: string; message: string }[];
}

export interface ClientProfile {
  id: string;
  clientId: string;
  clientName: string;
  skinTone?: string;
  allergies?: string;
  notes?: string;
  pastLooks?: string[];
}

export interface KitItem {
  id: string;
  name: string;
  brand?: string;
  category?: string;
  quantity: number;
  expiryDate?: string;
  lowStockAlert: boolean;
}

export interface BlockedDate {
  id: string;
  blockDate: string;
  reason?: string;
}

export interface QuoteRequest {
  id: string;
  clientName: string;
  muaName: string;
  occasion?: string;
  eventDate?: string;
  details?: string;
  budgetMin?: number;
  budgetMax?: number;
  status: string;
  quotedAmount?: number;
  muaResponse?: string;
}

export interface ReferralInfo {
  referralCode: string;
  creditAmount: number;
  totalReferrals: number;
}

export interface AdminStats {
  totalUsers: number;
  totalMuas: number;
  pendingVerification: number;
  totalBookings: number;
  totalRevenue: number;
  activeBookings: number;
  pendingMuas: AdminMua[];
}

export interface AdminMua {
  id: string;
  displayName: string;
  email: string;
  city: string;
  onboardingComplete: boolean;
}

export function formatPrice(amount?: number): string {
  if (!amount && amount !== 0) return 'Contact for price';
  return new Intl.NumberFormat('en-IN', { style: 'currency', currency: 'INR', maximumFractionDigits: 0 }).format(amount);
}

export function formatOccasion(o: string): string {
  return o.replace(/_/g, ' ').replace(/\b\w/g, c => c.toUpperCase());
}

export function statusLabel(status: string): string {
  return status.replace(/_/g, ' ').replace(/\b\w/g, c => c.toUpperCase());
}
