import React from "react";
import LocationPin from "./icons/LocationPin";

export interface TopBarProps {
  onToggleMenu?: () => void;
  menuOpen?: boolean;
  showShadow?: boolean;
}

// Provide a safe Link fallback if Next.js is not available.
// eslint-disable-next-line @typescript-eslint/no-explicit-any
let LinkComponent: any;
try {
  // eslint-disable-next-line @typescript-eslint/no-var-requires, @typescript-eslint/no-unsafe-assignment
  LinkComponent = require("next/link").default;
} catch {
  LinkComponent = ({ href, children, ...rest }: { href: string; children: React.ReactNode }) => (
    <a href={href} {...rest}>
      {children}
    </a>
  );
}

/**
 * Single, reusable top bar that matches the Urban Space spec.
 * Accessibility: focus-visible rings, meaningful aria labels, and keyboard-activatable controls.
 * No duplicate notification/action icons are rendered here; keep the header minimal.
 */
export default function TopBar({
  onToggleMenu,
  menuOpen = false,
  showShadow = false,
}: TopBarProps): JSX.Element {
  const handleToggle = () => {
    if (onToggleMenu) {
      onToggleMenu();
    } else {
      // Fallback to avoid silent clicks in consuming apps.
      console.log("menu toggle");
    }
  };

  const shadowClass =
    menuOpen || showShadow
      ? "shadow-[0_2px_8px_rgba(0,0,0,0.06)]"
      : "shadow-none";

  return (
    <header
      className={`flex items-center justify-between bg-white border-b border-black/5 ${shadowClass} h-14 md:h-16 px-4 md:px-6`}
      style={{ WebkitFontSmoothing: "antialiased", MozOsxFontSmoothing: "grayscale" }}
    >
      <LinkComponent
        href="/"
        aria-label="Go to home, Urban Space"
        className="flex items-center focus-visible:outline focus-visible:outline-3 focus-visible:outline-offset-2 focus-visible:outline-[rgba(45,140,255,0.16)]"
      >
        <div
          className="flex items-center justify-center rounded-xl w-9 h-9 md:w-10 md:h-10"
          style={{
            background: "linear-gradient(90deg, #2D8CFF 0%, #2CC5FF 100%)",
          }}
        >
          <LocationPin className="text-white" />
        </div>
        <span className="ml-3 font-bold text-[16px] md:text-[18px] text-[#222222]">
          Urban Space
        </span>
      </LinkComponent>

      <button
        type="button"
        aria-label="Open menu"
        aria-expanded={menuOpen}
        onClick={handleToggle}
        className="w-11 h-11 flex items-center justify-center rounded-full ml-2 mr-3 md:mr-4 focus-visible:outline focus-visible:outline-3 focus-visible:outline-offset-2 focus-visible:outline-[rgba(45,140,255,0.16)]"
      >
        <span className="relative block w-[22px] h-[14px]" aria-hidden="true">
          <span className="absolute inset-x-0 top-0 h-[2px] bg-[#333333] rounded-full" />
          <span className="absolute inset-x-0 top-1/2 -translate-y-1/2 h-[2px] bg-[#333333] rounded-full" />
          <span className="absolute inset-x-0 bottom-0 h-[2px] bg-[#333333] rounded-full" />
        </span>
      </button>
    </header>
  );
}

