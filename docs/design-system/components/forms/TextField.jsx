import React from 'react';
/** Outlined text field (Material 3 style) — label notched into the border, Petal focus ring. */
export function TextField({ label, value, onChange, placeholder, type = 'text', surface = 'var(--surface)', style }) {
  const [focus, setFocus] = React.useState(false);
  return <label style={{ display: 'block', position: 'relative', ...style }}>
    {label ? <span style={{ position: 'absolute', top: -8, left: 12, padding: '0 4px', background: surface, fontSize: 12, lineHeight: '16px', color: focus ? 'var(--petal)' : 'var(--smoke)', fontFamily: 'var(--font-sans)' }}>{label}</span> : null}
    <input type={type} value={value} placeholder={placeholder} onChange={e => onChange && onChange(e.target.value)} onFocus={() => setFocus(true)} onBlur={() => setFocus(false)} style={{ width: '100%', boxSizing: 'border-box', padding: '14px 16px', borderRadius: 'var(--radius-xs)', border: focus ? '2px solid var(--petal)' : '1px solid var(--smoke)', margin: focus ? 0 : 1, background: 'transparent', color: 'var(--ink)', fontFamily: 'var(--font-sans)', fontSize: 'var(--type-body-size)', outline: 'none' }} />
  </label>;
}
