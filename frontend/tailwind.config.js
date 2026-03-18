/** @type {import('tailwindcss').Config} */
module.exports = {
  content: ["./src/**/*.{html,ts}"],
  theme: {
    extend: {
      fontFamily: {
        display: ["Inter", "sans-serif"],
        comic: ["Bangers", "cursive"],
      },
      colors: {
        "sentinel-primary": "#38ff14",
        "sentinel-dark": "#0f1f17",
        "sentinel-ink": "#0a1408",
        "background-dark": "#12230f",
        "card-dark": "#162235",
        "accent-pink": "#ff00ff",
        "comic-red": "#ff3e3e",
        "comic-yellow": "#ffee00",
        "navy-deep": "#0a1408",
      },
      borderWidth: {
        '3': '3px',
      },
      boxShadow: {
        'neo': '4px 4px 0px 0px rgba(0, 0, 0, 1)',
        'neo-primary': '4px 4px 0px 0px #38ff14',
      },
    }
  },
  daisyui: {
    themes: [
      {
        sentinel: {
          "primary": "#38ff14",
          "secondary": "#ff00ff",
          "accent": "#ffee00",
          "neutral": "#0a1408",
          "base-100": "#0f1f17",
          "base-200": "#132819",
          "base-300": "#1a3121",
          "info": "#67e8f9",
          "success": "#22c55e",
          "warning": "#f59e0b",
          "error": "#ef4444"
        }
      }
    ]
  },
  plugins: [require("daisyui")]
};
