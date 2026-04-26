/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        primary: {
          50:  '#fff3e8',
          100: '#ffe8d0',
          200: '#ffd0a8',
          300: '#ffcca0',
          400: '#ffaa6e',
          500: '#ff8c42',
          600: '#e67530',
          700: '#c45e20',
          800: '#9c4a18',
          900: '#7a3a12',
        },
        accent: {
          100: '#d4f5d8',
          500: '#6bcb77',
          600: '#4db85a',
        },
        surface: '#fffbf7',
        'surface-variant': '#fdf3eb',
        'on-surface': '#2d1a0e',
        'on-surface-variant': '#7a5c45',
        outline: '#f0d9c8',
        error: '#d93025',
      },
      fontFamily: {
        sans: ['Nunito', 'system-ui', '-apple-system', 'sans-serif'],
      },
      boxShadow: {
        'elevation-1': '0 2px 8px rgba(255,140,66,0.12), 0 1px 3px rgba(45,26,14,0.08)',
        'elevation-2': '0 4px 16px rgba(255,140,66,0.16), 0 2px 6px rgba(45,26,14,0.10)',
        'elevation-3': '0 8px 24px rgba(255,140,66,0.18), 0 4px 8px rgba(45,26,14,0.12)',
      },
      animation: {
        'fade-in': 'fadeIn 0.2s ease-in-out',
        'fade-in-up': 'fadeInUp 0.3s ease-out both',
        'slide-up': 'slideUp 0.3s ease-out',
      },
      keyframes: {
        fadeIn: {
          '0%': { opacity: '0' },
          '100%': { opacity: '1' },
        },
        fadeInUp: {
          '0%': { opacity: '0', transform: 'translateY(12px)' },
          '100%': { opacity: '1', transform: 'translateY(0)' },
        },
        slideUp: {
          '0%': { transform: 'translateY(16px)', opacity: '0' },
          '100%': { transform: 'translateY(0)', opacity: '1' },
        },
      },
    },
  },
  plugins: [],
}
