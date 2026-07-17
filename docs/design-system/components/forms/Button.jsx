import React from 'react';
/** Filled / text button, Material-3-shaped (full pill), Petal accent, press-scales to 0.97. */
export function Button({ children, variant = 'filled', disabled = false, onClick, style }) {
  const [pressed, setPressed] = React.useState(false);
  const base = { appearance: 'none', border: 'none', fontFamily: 'var(--font-sans)', fontSize: 'var(--type-label-size)', fontWeight: 'var(--type-label-weight)', letterSpacing: 'var(--type-label-tracking)', textTransform: 'var(--type-heading-transform)', borderRadius: '999px', padding: '10px 24px', cursor: disabled ? 'default' : 'pointer', opacity: disabled ? 'var(--disabled-alpha)' : 1, transition: 'transform var(--motion-quick) var(--ease-standard)', transform: pressed && !disabled ? 'scale(var(--pressed-scale))' : 'none' };
  const look = variant === 'text' ? { background: 'transparent', color: 'var(--petal)' } : variant === 'outlined' ? { background: 'transparent', color: 'var(--petal)', border: '1px solid var(--mist)' } : { background: 'var(--petal)', color: 'var(--on-petal)' };
  return <button disabled={disabled} onClick={onClick} onMouseDown={() => setPressed(true)} onMouseUp={() => setPressed(false)} onMouseLeave={() => setPressed(false)} style={{ ...base, ...look, ...style }}>{children}</button>;
}
