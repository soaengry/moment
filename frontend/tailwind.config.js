/** @type {import('tailwindcss').Config} */
module.exports = {
  content: ["./index.html", "./src/**/*.{js,ts,jsx,tsx}"],
  theme: {
    extend: {
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
