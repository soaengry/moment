/** @type {import('tailwindcss').Config} */
module.exports = {
  content: ["./index.html", "./src/**/*.{js,ts,jsx,tsx}"],
  theme: {
    extend: {
      fontFamily: {
        sans: [
          '"Pretendard Variable"',
          "Pretendard",
          "-apple-system",
          "BlinkMacSystemFont",
          '"Segoe UI"',
          '"Noto Sans KR"',
          "sans-serif",
        ],
      },
      keyframes: {
        "slide-up": {
          "0%": { transform: "translateY(100%)" },
          "100%": { transform: "translateY(0)" },
        },
        "fade-in": {
          "0%": { opacity: "0" },
          "100%": { opacity: "1" },
        },
        "slide-in-right": {
          "0%": { transform: "translateX(100%)", opacity: "0" },
          "100%": { transform: "translateX(0)", opacity: "1" },
        },
        "bounce-in": {
          "0%": { transform: "scale(0.8)", opacity: "0" },
          "50%": { transform: "scale(1.05)" },
          "100%": { transform: "scale(1)", opacity: "1" },
        },
      },
      animation: {
        "slide-up": "slide-up 0.3s ease-out",
        "fade-in": "fade-in 0.5s ease-out",
        "slide-in-right": "slide-in-right 0.4s ease-out",
        "bounce-in": "bounce-in 0.5s cubic-bezier(0.68, -0.55, 0.265, 1.55)",
      },
      colors: {
        primary: "#75bd28",
        primaryHover: "#5f9920",
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
