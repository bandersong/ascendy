import React from 'react';
/** Applies an Ascendy theme scope. Wrap any subtree; sets data-theme, bg, text color, and font. */
export function ThemeScope({ theme = 'kawaii', dark = false, padded = false, style, children }) {
  return <div data-theme={theme + (dark ? '-dark' : '')} style={{ background: 'var(--cream)', color: 'var(--ink)', fontFamily: 'var(--font-sans)', fontSize: 'var(--type-body-size)', lineHeight: 1.5, padding: padded ? 'var(--space-xl)' : 0, ...style }}>{children}</div>;
}
