import React from 'react';
/** The Ascendy card: hairline Mist border, theme-large radius, 20px padding, whisper shadow in light mode. */
export function SoftCard({ tone = 'surface', onClick, style, children }) {
  const [pressed, setPressed] = React.useState(false);
  const press = onClick ? { onMouseDown: () => setPressed(true), onMouseUp: () => setPressed(false), onMouseLeave: () => setPressed(false) } : {};
  return <div onClick={onClick} {...press} style={{ background: tone === 'cloud' ? 'var(--cloud)' : 'var(--surface)', border: '1px solid var(--mist)', borderRadius: 'var(--radius-lg)', boxShadow: 'var(--shadow-card)', padding: 'var(--space-xl)', cursor: onClick ? 'pointer' : undefined, transition: 'transform var(--motion-quick) var(--ease-standard)', transform: pressed ? 'scale(var(--pressed-scale))' : 'none', ...style }}>{children}</div>;
}
