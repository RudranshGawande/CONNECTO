# Urban Space TopBar

This repository includes a reusable TopBar component that matches the provided Urban Space header spec, along with a demo page and unit tests.

## Quick start
1. Install dependencies: `npm install`
2. Run dev server: `npm run dev`
3. Open demo: http://localhost:3000/topbar-demo
4. Run tests: `npm run test`

## Files
- `src/components/TopBar.tsx` – Top bar component
- `src/components/icons/LocationPin.tsx` – Inline SVG icon for the logo
- `pages/topbar-demo.tsx` – Demo page showcasing TopBar
- `__tests__/TopBar.test.tsx` – Jest + React Testing Library tests
- `tailwind.config.js` – Tailwind content paths

## Notes
- Component is mobile-first: 56px height on mobile, 64px on md+.
- Uses Tailwind utility classes; includes focus-visible outlines for accessibility.
- No extra header icons are rendered here—only brand link and hamburger menu.

