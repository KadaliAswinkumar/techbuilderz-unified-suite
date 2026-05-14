export const THEME_KEY = "vidyalaya_theme";

export type ThemeMode = "light" | "dark";

export function getStoredTheme(): ThemeMode | null {
  try {
    const v = localStorage.getItem(THEME_KEY);
    if (v === "light" || v === "dark") {
      return v;
    }
  } catch {
    /* ignore */
  }
  return null;
}

export function applyTheme(mode: ThemeMode) {
  try {
    localStorage.setItem(THEME_KEY, mode);
  } catch {
    /* ignore */
  }
  if (mode === "dark") {
    document.documentElement.classList.add("dark");
  } else {
    document.documentElement.classList.remove("dark");
  }
}

export function initThemeFromStorage() {
  const stored = getStoredTheme();
  const prefersDark = window.matchMedia?.("(prefers-color-scheme: dark)")?.matches ?? false;
  applyTheme(stored === "light" ? "light" : stored === "dark" ? "dark" : prefersDark ? "dark" : "light");
}

export function toggleStoredTheme(): ThemeMode {
  const next = document.documentElement.classList.contains("dark") ? "light" : "dark";
  applyTheme(next);
  return next;
}
