import React, { useState } from "react";
import TopBar from "../src/components/TopBar";

export default function TopBarDemo() {
  const [menuOpen, setMenuOpen] = useState(false);

  return (
    <div className="min-h-screen bg-[#f6f8fb]">
      <TopBar
        menuOpen={menuOpen}
        onToggleMenu={() => setMenuOpen((open) => !open)}
        showShadow={menuOpen}
      />

      <main className="p-4 md:p-6 space-y-4 text-[#222]">
        <p className="text-lg font-semibold">TopBar demo</p>
        <p>
          Menu state:{" "}
          <span className="font-bold">{menuOpen ? "Menu open" : "Menu closed"}</span>
        </p>
        <button
          type="button"
          onClick={() => setMenuOpen((open) => !open)}
          className="px-4 py-2 rounded-md bg-[#0D8BD9] text-white font-medium focus-visible:outline focus-visible:outline-3 focus-visible:outline-offset-2 focus-visible:outline-[rgba(45,140,255,0.16)]"
        >
          Toggle menu
        </button>
        <p className="text-sm text-[#555]">
          Use keyboard (Tab, Enter, Space) to verify focus behavior on the brand link and
          menu button. The hamburger button toggles aria-expanded based on the menu state.
        </p>
      </main>
    </div>
  );
}



