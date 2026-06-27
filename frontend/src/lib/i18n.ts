export type Locale = 'en' | 'kn';

const translations: Record<Locale, Record<string, string>> = {
  en: {
    'nav.home': 'Home',
    'nav.search': 'Search',
    'nav.bookings': 'Bookings',
    'nav.dashboard': 'Dashboard',
    'nav.login': 'Login',
    'nav.logout': 'Logout',
    'search.title': 'Find Your Perfect Makeup Artist',
    'search.nearMe': 'Near Me',
    'search.city': 'City',
    'search.locality': 'Locality / Area',
    'search.pincode': 'PIN Code',
    'search.occasion': 'Occasion',
    'book.now': 'Book Now',
    'book.trial': 'Book Trial Session',
    'quote.request': 'Request a Quote',
    'onboarding.title': 'Complete Your Artist Profile',
    'onboarding.step1': 'Basic Info',
    'onboarding.step2': 'Services & Pricing',
    'onboarding.step3': 'Portfolio',
    'onboarding.goLive': 'Go Live',
    'admin.title': 'Admin Dashboard',
    'referral.title': 'Refer a Friend',
    'referral.desc': 'Share your code — they get ₹200 off, you earn credit!',
  },
  kn: {
    'nav.home': 'ಮುಖಪುಟ',
    'nav.search': 'ಹುಡುಕಿ',
    'nav.bookings': 'ಬುಕಿಂಗ್',
    'nav.dashboard': 'ಡ್ಯಾಶ್‌ಬೋರ್ಡ್',
    'nav.login': 'ಲಾಗಿನ್',
    'nav.logout': 'ಲಾಗ್ ಔಟ್',
    'search.title': 'ನಿಮ್ಮ ಪರಿಪೂರ್ಣ ಮೇಕಪ್ ಕಲಾವಿದರನ್ನು ಹುಡುಕಿ',
    'search.nearMe': 'ಹತ್ತಿರದಲ್ಲಿ',
    'search.city': 'ನಗರ',
    'search.locality': 'ಪ್ರದೇಶ',
    'search.pincode': 'ಪಿನ್ ಕೋಡ್',
    'search.occasion': 'ಸಂದರ್ಭ',
    'book.now': 'ಈಗ ಬುಕ್ ಮಾಡಿ',
    'book.trial': 'ಟ್ರಯಲ್ ಸೆಷನ್',
    'quote.request': 'ಕೋಟ್ ಕೇಳಿ',
    'onboarding.title': 'ನಿಮ್ಮ ಪ್ರೊಫೈಲ್ ಪೂರ್ಣಗೊಳಿಸಿ',
    'onboarding.step1': 'ಮೂಲ ಮಾಹಿತಿ',
    'onboarding.step2': 'ಸೇವೆಗಳು & ಬೆಲೆ',
    'onboarding.step3': 'ಪೋರ್ಟ್‌ಫೋಲಿಯೋ',
    'onboarding.goLive': 'ಲೈವ್ ಆಗಿ',
    'admin.title': 'ಅಡ್ಮಿನ್',
    'referral.title': 'ಸ್ನೇಹಿತರನ್ನು ಉಲ್ಲೇಖಿಸಿ',
    'referral.desc': '₹200 ರಿಯಾಯಿತಿ — ನಿಮಗೂ ಕ್ರೆಡಿಟ್!',
  },
};

export function t(key: string, locale: Locale = 'en'): string {
  return translations[locale][key] || translations.en[key] || key;
}

export function getLocale(): Locale {
  if (typeof window === 'undefined') return 'en';
  return (localStorage.getItem('locale') as Locale) || 'en';
}

export function setLocale(locale: Locale) {
  localStorage.setItem('locale', locale);
}
