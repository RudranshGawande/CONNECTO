import React from "react";

type LocationPinProps = React.SVGProps<SVGSVGElement>;

/**
 * Inline location pin glyph to avoid external assets.
 * Marked aria-hidden to keep screen readers focused on the text label.
 */
export default function LocationPin(props: LocationPinProps) {
  return (
    <svg
      width={22}
      height={22}
      viewBox="0 0 24 24"
      fill="currentColor"
      aria-hidden="true"
      focusable="false"
      {...props}
    >
      <path d="M12 2.75a6.25 6.25 0 0 0-6.25 6.25c0 1.43.48 2.89 1.46 4.43.89 1.38 2.12 2.8 3.46 4.26.66.71 1.34 1.44 2 2.19a.75.75 0 0 0 1.12 0c.66-.75 1.34-1.48 2-2.19 1.34-1.46 2.57-2.88 3.46-4.26.98-1.54 1.46-3.1 1.46-4.43A6.25 6.25 0 0 0 12 2.75Zm0 8.75a2.5 2.5 0 1 1 0-5 2.5 2.5 0 0 1 0 5Z" />
    </svg>
  );
}

