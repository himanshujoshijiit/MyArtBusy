/** @type {import('tailwindcss').Config} */
module.exports = {
  content: ['./src/**/*.{js,ts,jsx,tsx,mdx}'],
  theme: {
    extend: {
      colors: {
        rose: {
          50: '#fff5f7',
          100: '#ffe4ea',
          200: '#fecdd8',
          300: '#fda4b8',
          400: '#fb7194',
          500: '#f43f6e',
          600: '#e11d5a',
          700: '#be1249',
          800: '#9f123f',
          900: '#88133a',
        },
        gold: {
          50: '#fdfaf3',
          100: '#f9f0d9',
          200: '#f2deb3',
          300: '#e9c882',
          400: '#dfad52',
          500: '#d4952f',
          600: '#b87724',
          700: '#9a6118',
          800: '#7c4d12',
        },
        cream: '#faf8f5',
        charcoal: '#1a1a2e',
      },
      fontFamily: {
        display: ['Georgia', 'Cambria', 'Times New Roman', 'serif'],
        sans: ['system-ui', '-apple-system', 'Segoe UI', 'Roboto', 'sans-serif'],
      },
      boxShadow: {
        card: '0 4px 24px rgba(0,0,0,0.06)',
        elevated: '0 12px 40px rgba(0,0,0,0.12)',
      },
    },
  },
  plugins: [],
};
