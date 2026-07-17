import React from 'react';
/** Standard page scaffold — Cream bg, centered 640px column, 20px side padding (PageColumn in Decor.kt). */
export function PageColumn({ style, innerStyle, children }) {
  return <div style={{ minHeight: '100%', background: 'var(--cream)', display: 'flex', justifyContent: 'center', padding: '16px var(--page-pad-x) 24px', ...style }}>
    <div style={{ width: '100%', maxWidth: 'var(--page-max-width)', display: 'flex', flexDirection: 'column', ...innerStyle }}>{children}</div>
  </div>;
}
