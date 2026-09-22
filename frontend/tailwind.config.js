/** @type {import('tailwindcss').Config} */
export default {
  content: ['./index.html', './src/**/*.{js,jsx}'],
  theme: {
    extend: {
      colors: {
        brand: {
          50: '#eef7ff',
          100: '#d9edff',
          200: '#bce0ff',
          300: '#8ecdff',
          400: '#59b0ff',
          500: '#3291fb',
          600: '#1b73e8',
          700: '#145bc5',
          800: '#174ba0',
          900: '#19407e',
        },
      },
    },
  },
  plugins: [],
}