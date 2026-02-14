const { warn } = require("console");
const { da } = require("zod/locales");

/** @type {import('tailwindcss').Config} */
module.exports = {
  content: ["./index.html", "./src/**/*.{js,ts,jsx,tsx}"],
  theme: {
    extend: {
      colors: {
        primary: "#6B9F33", // 메인 그린
        primaryHover: "#5A8A2C", // 메인 그린 다크
        bgPrimary: "#FAFFF4", // 배경 화이트
        gold: "#F0C434", // 포인트 골드
        rose: "#E6A5A5", // 포인트 로즈핑크
        warning: "#fd5b5b", // 경고 레드
        success: "#16A34A",
        bgScuccess: "#DCFCE7",
        danger: "#F93B3E", // 에러 레드
        bgDanger: "#FDEDED", // 에러 레드 연한 버전
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
