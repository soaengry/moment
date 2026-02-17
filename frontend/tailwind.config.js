/** @type {import('tailwindcss').Config} */
module.exports = {
  content: ["./index.html", "./src/**/*.{js,ts,jsx,tsx}"],
  theme: {
    extend: {
      fontFamily: {
        sans: ['"Pretendard Variable"', 'Pretendard', '-apple-system', 'BlinkMacSystemFont', '"Segoe UI"', '"Noto Sans KR"', 'sans-serif'],
      },
      keyframes: {
        'slide-up': {
          '0%': { transform: 'translateY(100%)' },
          '100%': { transform: 'translateY(0)' },
        },
      },
      animation: {
        'slide-up': 'slide-up 0.3s ease-out',
      },
      colors: {
        primary: "#6B9F33",
        primaryHover: "#5A8A2C",
        bgPrimary: "#FAFFF4",
        gold: "#F0C434",
        rose: "#E6A5A5",
        success: "#16A34A",
        bgSuccess: "#DCFCE7",
        error: "#FD5B5B",
        bgError: "#FDEDED",
      },
    },
  },
  plugins: [
    function ({ addBase }) {
      addBase({
        "*:not(input):not(textarea):not(select)": {
          userSelect: "none",
          outline: "none",
        },
      });
    },
  ],
};
