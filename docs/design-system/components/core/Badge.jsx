import React from 'react';
function lum(rgb) { const m = rgb && rgb.match(/\d+(\.\d+)?/g); if (!m) return 0; const [r, g, b] = m.map(Number); return (0.2126 * r + 0.7152 * g + 0.0722 * b) / 255; }
/** Small status pill. Text auto-picks Ink or Cream by contrast against the fill (onChip in Decor.kt). */
export function Badge({ label, tone = 'sage', style }) {
  const ref = React.useRef(null);
  const [fg, setFg] = React.useState('var(--ink)');
  React.useLayoutEffect(() => {
    if (!ref.current) return;
    const cs = getComputedStyle(ref.current);
    const chip = lum(cs.backgroundColor);
    const el = ref.current, inkL = lum(cs.color);
    const creamProbe = document.createElement('span'); creamProbe.style.color = 'var(--cream)'; el.appendChild(creamProbe);
    const creamL = lum(getComputedStyle(creamProbe).color); el.removeChild(creamProbe);
    const darker = inkL <= creamL ? 'var(--ink)' : 'var(--cream)';
    const lighter = inkL <= creamL ? 'var(--cream)' : 'var(--ink)';
    setFg(chip > 0.45 ? darker : lighter);
  }, [tone]);
  return <span ref={ref} style={{ display: 'inline-block', background: `var(--${tone})`, color: fg, borderRadius: 'var(--radius-xl)', padding: '6px 12px', fontSize: 'var(--type-label-size)', fontWeight: 600, letterSpacing: 'var(--type-label-tracking)', lineHeight: 'var(--type-label-leading)', textTransform: 'var(--type-heading-transform)', whiteSpace: 'nowrap', ...style }}>{label}</span>;
}
