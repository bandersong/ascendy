import React from 'react';
/** Shared selectable pill (Onboarding, PairTag, AppPicker, Schedules, Pomodoro, Settings). */
export function SelectableChip({ label, selected = false, onClick, style }) {
  const [pressed, setPressed] = React.useState(false);
  return <button onClick={onClick} onMouseDown={() => setPressed(true)} onMouseUp={() => setPressed(false)} onMouseLeave={() => setPressed(false)} style={{ appearance: 'none', fontFamily: 'var(--font-sans)', fontSize: 'var(--type-title-sm-size)', fontWeight: 'var(--type-title-sm-weight)', lineHeight: 'var(--type-title-sm-leading)', textTransform: 'var(--type-heading-transform)', textAlign: 'center', padding: '14px', borderRadius: 'var(--radius-md)', cursor: 'pointer', transition: 'transform var(--motion-quick) var(--ease-standard)', transform: pressed ? 'scale(var(--pressed-scale))' : 'none', background: selected ? 'var(--petal)' : 'var(--cloud)', color: selected ? 'var(--on-petal)' : 'var(--ink)', border: selected ? '1px solid transparent' : '1px solid var(--mist)', ...style }}>{label}</button>;
}
