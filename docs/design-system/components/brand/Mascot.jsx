import React from 'react';
const DS_BASE = (() => { const s = Array.from(document.scripts).find(x => /_ds_bundle\.js/.test(x.src)); return s ? s.src.replace(/_ds_bundle\.js.*$/, '') : ''; })();
if (typeof document !== 'undefined' && !document.getElementById('ascendy-bob-kf')) {
  const st = document.createElement('style'); st.id = 'ascendy-bob-kf';
  st.textContent = '@keyframes ascendy-bob{from{transform:translateY(-8px)}to{transform:translateY(8px)}}';
  document.head.appendChild(st);
}
/** The hand-drawn themed mascot (never redraw it). Crossfade-free simple swap; bobs ±8px on a 2400ms loop. */
export function Mascot({ variant = 'kawaii', locked = false, size = 176, bob = true, style }) {
  return <img src={`${DS_BASE}assets/mascots/mascot_${variant}_${locked ? 'locked' : 'unlocked'}.png`} alt={locked ? 'Focusing mascot' : 'Idle mascot'} style={{ width: size, height: size, objectFit: 'contain', animation: bob ? 'ascendy-bob var(--motion-mascot-bob) linear infinite alternate' : 'none', ...style }} />;
}
