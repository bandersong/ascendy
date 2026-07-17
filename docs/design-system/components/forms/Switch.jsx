import React from 'react';
/** Material-3-style switch — Petal track when on, Cloud + Mist hairline when off. */
export function Switch({ checked = false, onChange, disabled = false, style }) {
  return <button role="switch" aria-checked={checked} disabled={disabled} onClick={() => onChange && onChange(!checked)} style={{ appearance: 'none', width: 52, height: 32, borderRadius: 16, position: 'relative', cursor: disabled ? 'default' : 'pointer', opacity: disabled ? 'var(--disabled-alpha)' : 1, background: checked ? 'var(--petal)' : 'var(--cloud)', border: checked ? '2px solid var(--petal)' : '2px solid var(--mist)', transition: 'background var(--motion-quick) var(--ease-standard)', padding: 0, ...style }}>
    <span style={{ position: 'absolute', top: '50%', left: checked ? 24 : 6, transform: 'translateY(-50%)', width: checked ? 22 : 16, height: checked ? 22 : 16, borderRadius: '50%', background: checked ? 'var(--on-petal)' : 'var(--smoke)', transition: 'all var(--motion-quick) var(--ease-standard)' }}></span>
  </button>;
}
