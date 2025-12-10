import React from "react";
import { render, screen, fireEvent } from "@testing-library/react";
import TopBar from "../src/components/TopBar";

describe("TopBar", () => {
  it("renders brand text and hamburger button", () => {
    render(<TopBar />);
    expect(screen.getByText("Urban Space")).toBeInTheDocument();
    const button = screen.getByLabelText("Open menu");
    expect(button).toBeInTheDocument();
  });

  it("calls onToggleMenu and reflects aria-expanded", () => {
    const handleToggle = jest.fn();
    const { rerender } = render(<TopBar onToggleMenu={handleToggle} />);

    fireEvent.click(screen.getByLabelText("Open menu"));
    expect(handleToggle).toHaveBeenCalledTimes(1);

    rerender(<TopBar menuOpen onToggleMenu={handleToggle} />);
    expect(screen.getByLabelText("Open menu")).toHaveAttribute("aria-expanded", "true");
  });
});

