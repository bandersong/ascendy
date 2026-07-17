/* @ds-bundle: {"format":4,"namespace":"AscendyDesignSystem_01b7f9","components":[{"name":"Mascot","sourcePath":"components/brand/Mascot.jsx"},{"name":"Badge","sourcePath":"components/core/Badge.jsx"},{"name":"PageColumn","sourcePath":"components/core/PageColumn.jsx"},{"name":"SelectableChip","sourcePath":"components/core/SelectableChip.jsx"},{"name":"SoftCard","sourcePath":"components/core/SoftCard.jsx"},{"name":"ThemeScope","sourcePath":"components/core/ThemeScope.jsx"},{"name":"Button","sourcePath":"components/forms/Button.jsx"},{"name":"Switch","sourcePath":"components/forms/Switch.jsx"},{"name":"TextField","sourcePath":"components/forms/TextField.jsx"}],"sourceHashes":{"components/brand/Mascot.jsx":"8ea027a859e8","components/core/Badge.jsx":"e449c308b16f","components/core/PageColumn.jsx":"bbd88457fc91","components/core/SelectableChip.jsx":"975cce56df6f","components/core/SoftCard.jsx":"1fe486e7935b","components/core/ThemeScope.jsx":"133c228c1a44","components/forms/Button.jsx":"a728dc941f5a","components/forms/Switch.jsx":"cc74c32810b7","components/forms/TextField.jsx":"3da4a0cfc614","ui_kits/app/app.jsx":"d7023c8b64b6","ui_kits/app/home_screen.jsx":"9af61f3e0506","ui_kits/app/screens.jsx":"f4bf80ce7f56","ui_kits/app/vocab.js":"67716573d2b1"},"inlinedExternals":[],"unexposedExports":[]} */

(() => {

const __ds_ns = (window.AscendyDesignSystem_01b7f9 = window.AscendyDesignSystem_01b7f9 || {});

const __ds_scope = {};

(__ds_ns.__errors = __ds_ns.__errors || []);

// components/brand/Mascot.jsx
try { (() => {
const DS_BASE = (() => {
  const s = Array.from(document.scripts).find(x => /_ds_bundle\.js/.test(x.src));
  return s ? s.src.replace(/_ds_bundle\.js.*$/, '') : '';
})();
if (typeof document !== 'undefined' && !document.getElementById('ascendy-bob-kf')) {
  const st = document.createElement('style');
  st.id = 'ascendy-bob-kf';
  st.textContent = '@keyframes ascendy-bob{from{transform:translateY(-8px)}to{transform:translateY(8px)}}';
  document.head.appendChild(st);
}
/** The hand-drawn themed mascot (never redraw it). Crossfade-free simple swap; bobs ±8px on a 2400ms loop. */
function Mascot({
  variant = 'kawaii',
  locked = false,
  size = 176,
  bob = true,
  style
}) {
  return /*#__PURE__*/React.createElement("img", {
    src: `${DS_BASE}assets/mascots/mascot_${variant}_${locked ? 'locked' : 'unlocked'}.png`,
    alt: locked ? 'Focusing mascot' : 'Idle mascot',
    style: {
      width: size,
      height: size,
      objectFit: 'contain',
      animation: bob ? 'ascendy-bob var(--motion-mascot-bob) linear infinite alternate' : 'none',
      ...style
    }
  });
}
Object.assign(__ds_scope, { Mascot });
})(); } catch (e) { __ds_ns.__errors.push({ path: "components/brand/Mascot.jsx", error: String((e && e.message) || e) }); }

// components/core/Badge.jsx
try { (() => {
function lum(rgb) {
  const m = rgb && rgb.match(/\d+(\.\d+)?/g);
  if (!m) return 0;
  const [r, g, b] = m.map(Number);
  return (0.2126 * r + 0.7152 * g + 0.0722 * b) / 255;
}
/** Small status pill. Text auto-picks Ink or Cream by contrast against the fill (onChip in Decor.kt). */
function Badge({
  label,
  tone = 'sage',
  style
}) {
  const ref = React.useRef(null);
  const [fg, setFg] = React.useState('var(--ink)');
  React.useLayoutEffect(() => {
    if (!ref.current) return;
    const cs = getComputedStyle(ref.current);
    const chip = lum(cs.backgroundColor);
    const el = ref.current,
      inkL = lum(cs.color);
    const creamProbe = document.createElement('span');
    creamProbe.style.color = 'var(--cream)';
    el.appendChild(creamProbe);
    const creamL = lum(getComputedStyle(creamProbe).color);
    el.removeChild(creamProbe);
    const darker = inkL <= creamL ? 'var(--ink)' : 'var(--cream)';
    const lighter = inkL <= creamL ? 'var(--cream)' : 'var(--ink)';
    setFg(chip > 0.45 ? darker : lighter);
  }, [tone]);
  return /*#__PURE__*/React.createElement("span", {
    ref: ref,
    style: {
      display: 'inline-block',
      background: `var(--${tone})`,
      color: fg,
      borderRadius: 'var(--radius-xl)',
      padding: '6px 12px',
      fontSize: 'var(--type-label-size)',
      fontWeight: 600,
      letterSpacing: 'var(--type-label-tracking)',
      lineHeight: 'var(--type-label-leading)',
      textTransform: 'var(--type-heading-transform)',
      whiteSpace: 'nowrap',
      ...style
    }
  }, label);
}
Object.assign(__ds_scope, { Badge });
})(); } catch (e) { __ds_ns.__errors.push({ path: "components/core/Badge.jsx", error: String((e && e.message) || e) }); }

// components/core/PageColumn.jsx
try { (() => {
/** Standard page scaffold — Cream bg, centered 640px column, 20px side padding (PageColumn in Decor.kt). */
function PageColumn({
  style,
  innerStyle,
  children
}) {
  return /*#__PURE__*/React.createElement("div", {
    style: {
      minHeight: '100%',
      background: 'var(--cream)',
      display: 'flex',
      justifyContent: 'center',
      padding: '16px var(--page-pad-x) 24px',
      ...style
    }
  }, /*#__PURE__*/React.createElement("div", {
    style: {
      width: '100%',
      maxWidth: 'var(--page-max-width)',
      display: 'flex',
      flexDirection: 'column',
      ...innerStyle
    }
  }, children));
}
Object.assign(__ds_scope, { PageColumn });
})(); } catch (e) { __ds_ns.__errors.push({ path: "components/core/PageColumn.jsx", error: String((e && e.message) || e) }); }

// components/core/SelectableChip.jsx
try { (() => {
/** Shared selectable pill (Onboarding, PairTag, AppPicker, Schedules, Pomodoro, Settings). */
function SelectableChip({
  label,
  selected = false,
  onClick,
  style
}) {
  const [pressed, setPressed] = React.useState(false);
  return /*#__PURE__*/React.createElement("button", {
    onClick: onClick,
    onMouseDown: () => setPressed(true),
    onMouseUp: () => setPressed(false),
    onMouseLeave: () => setPressed(false),
    style: {
      appearance: 'none',
      fontFamily: 'var(--font-sans)',
      fontSize: 'var(--type-title-sm-size)',
      fontWeight: 'var(--type-title-sm-weight)',
      lineHeight: 'var(--type-title-sm-leading)',
      textTransform: 'var(--type-heading-transform)',
      textAlign: 'center',
      padding: '14px',
      borderRadius: 'var(--radius-md)',
      cursor: 'pointer',
      transition: 'transform var(--motion-quick) var(--ease-standard)',
      transform: pressed ? 'scale(var(--pressed-scale))' : 'none',
      background: selected ? 'var(--petal)' : 'var(--cloud)',
      color: selected ? 'var(--on-petal)' : 'var(--ink)',
      border: selected ? '1px solid transparent' : '1px solid var(--mist)',
      ...style
    }
  }, label);
}
Object.assign(__ds_scope, { SelectableChip });
})(); } catch (e) { __ds_ns.__errors.push({ path: "components/core/SelectableChip.jsx", error: String((e && e.message) || e) }); }

// components/core/SoftCard.jsx
try { (() => {
function _extends() { return _extends = Object.assign ? Object.assign.bind() : function (n) { for (var e = 1; e < arguments.length; e++) { var t = arguments[e]; for (var r in t) ({}).hasOwnProperty.call(t, r) && (n[r] = t[r]); } return n; }, _extends.apply(null, arguments); }
/** The Ascendy card: hairline Mist border, theme-large radius, 20px padding, whisper shadow in light mode. */
function SoftCard({
  tone = 'surface',
  onClick,
  style,
  children
}) {
  const [pressed, setPressed] = React.useState(false);
  const press = onClick ? {
    onMouseDown: () => setPressed(true),
    onMouseUp: () => setPressed(false),
    onMouseLeave: () => setPressed(false)
  } : {};
  return /*#__PURE__*/React.createElement("div", _extends({
    onClick: onClick
  }, press, {
    style: {
      background: tone === 'cloud' ? 'var(--cloud)' : 'var(--surface)',
      border: '1px solid var(--mist)',
      borderRadius: 'var(--radius-lg)',
      boxShadow: 'var(--shadow-card)',
      padding: 'var(--space-xl)',
      cursor: onClick ? 'pointer' : undefined,
      transition: 'transform var(--motion-quick) var(--ease-standard)',
      transform: pressed ? 'scale(var(--pressed-scale))' : 'none',
      ...style
    }
  }), children);
}
Object.assign(__ds_scope, { SoftCard });
})(); } catch (e) { __ds_ns.__errors.push({ path: "components/core/SoftCard.jsx", error: String((e && e.message) || e) }); }

// components/core/ThemeScope.jsx
try { (() => {
/** Applies an Ascendy theme scope. Wrap any subtree; sets data-theme, bg, text color, and font. */
function ThemeScope({
  theme = 'kawaii',
  dark = false,
  padded = false,
  style,
  children
}) {
  return /*#__PURE__*/React.createElement("div", {
    "data-theme": theme + (dark ? '-dark' : ''),
    style: {
      background: 'var(--cream)',
      color: 'var(--ink)',
      fontFamily: 'var(--font-sans)',
      fontSize: 'var(--type-body-size)',
      lineHeight: 1.5,
      padding: padded ? 'var(--space-xl)' : 0,
      ...style
    }
  }, children);
}
Object.assign(__ds_scope, { ThemeScope });
})(); } catch (e) { __ds_ns.__errors.push({ path: "components/core/ThemeScope.jsx", error: String((e && e.message) || e) }); }

// components/forms/Button.jsx
try { (() => {
/** Filled / text button, Material-3-shaped (full pill), Petal accent, press-scales to 0.97. */
function Button({
  children,
  variant = 'filled',
  disabled = false,
  onClick,
  style
}) {
  const [pressed, setPressed] = React.useState(false);
  const base = {
    appearance: 'none',
    border: 'none',
    fontFamily: 'var(--font-sans)',
    fontSize: 'var(--type-label-size)',
    fontWeight: 'var(--type-label-weight)',
    letterSpacing: 'var(--type-label-tracking)',
    textTransform: 'var(--type-heading-transform)',
    borderRadius: '999px',
    padding: '10px 24px',
    cursor: disabled ? 'default' : 'pointer',
    opacity: disabled ? 'var(--disabled-alpha)' : 1,
    transition: 'transform var(--motion-quick) var(--ease-standard)',
    transform: pressed && !disabled ? 'scale(var(--pressed-scale))' : 'none'
  };
  const look = variant === 'text' ? {
    background: 'transparent',
    color: 'var(--petal)'
  } : variant === 'outlined' ? {
    background: 'transparent',
    color: 'var(--petal)',
    border: '1px solid var(--mist)'
  } : {
    background: 'var(--petal)',
    color: 'var(--on-petal)'
  };
  return /*#__PURE__*/React.createElement("button", {
    disabled: disabled,
    onClick: onClick,
    onMouseDown: () => setPressed(true),
    onMouseUp: () => setPressed(false),
    onMouseLeave: () => setPressed(false),
    style: {
      ...base,
      ...look,
      ...style
    }
  }, children);
}
Object.assign(__ds_scope, { Button });
})(); } catch (e) { __ds_ns.__errors.push({ path: "components/forms/Button.jsx", error: String((e && e.message) || e) }); }

// components/forms/Switch.jsx
try { (() => {
/** Material-3-style switch — Petal track when on, Cloud + Mist hairline when off. */
function Switch({
  checked = false,
  onChange,
  disabled = false,
  style
}) {
  return /*#__PURE__*/React.createElement("button", {
    role: "switch",
    "aria-checked": checked,
    disabled: disabled,
    onClick: () => onChange && onChange(!checked),
    style: {
      appearance: 'none',
      width: 52,
      height: 32,
      borderRadius: 16,
      position: 'relative',
      cursor: disabled ? 'default' : 'pointer',
      opacity: disabled ? 'var(--disabled-alpha)' : 1,
      background: checked ? 'var(--petal)' : 'var(--cloud)',
      border: checked ? '2px solid var(--petal)' : '2px solid var(--mist)',
      transition: 'background var(--motion-quick) var(--ease-standard)',
      padding: 0,
      ...style
    }
  }, /*#__PURE__*/React.createElement("span", {
    style: {
      position: 'absolute',
      top: '50%',
      left: checked ? 24 : 6,
      transform: 'translateY(-50%)',
      width: checked ? 22 : 16,
      height: checked ? 22 : 16,
      borderRadius: '50%',
      background: checked ? 'var(--on-petal)' : 'var(--smoke)',
      transition: 'all var(--motion-quick) var(--ease-standard)'
    }
  }));
}
Object.assign(__ds_scope, { Switch });
})(); } catch (e) { __ds_ns.__errors.push({ path: "components/forms/Switch.jsx", error: String((e && e.message) || e) }); }

// components/forms/TextField.jsx
try { (() => {
/** Outlined text field (Material 3 style) — label notched into the border, Petal focus ring. */
function TextField({
  label,
  value,
  onChange,
  placeholder,
  type = 'text',
  surface = 'var(--surface)',
  style
}) {
  const [focus, setFocus] = React.useState(false);
  return /*#__PURE__*/React.createElement("label", {
    style: {
      display: 'block',
      position: 'relative',
      ...style
    }
  }, label ? /*#__PURE__*/React.createElement("span", {
    style: {
      position: 'absolute',
      top: -8,
      left: 12,
      padding: '0 4px',
      background: surface,
      fontSize: 12,
      lineHeight: '16px',
      color: focus ? 'var(--petal)' : 'var(--smoke)',
      fontFamily: 'var(--font-sans)'
    }
  }, label) : null, /*#__PURE__*/React.createElement("input", {
    type: type,
    value: value,
    placeholder: placeholder,
    onChange: e => onChange && onChange(e.target.value),
    onFocus: () => setFocus(true),
    onBlur: () => setFocus(false),
    style: {
      width: '100%',
      boxSizing: 'border-box',
      padding: '14px 16px',
      borderRadius: 'var(--radius-xs)',
      border: focus ? '2px solid var(--petal)' : '1px solid var(--smoke)',
      margin: focus ? 0 : 1,
      background: 'transparent',
      color: 'var(--ink)',
      fontFamily: 'var(--font-sans)',
      fontSize: 'var(--type-body-size)',
      outline: 'none'
    }
  }));
}
Object.assign(__ds_scope, { TextField });
})(); } catch (e) { __ds_ns.__errors.push({ path: "components/forms/TextField.jsx", error: String((e && e.message) || e) }); }

// ui_kits/app/app.jsx
try { (() => {
const {
  ThemeScope
} = window.AscendyDesignSystem_01b7f9;
function AscendyApp() {
  const saved = (() => {
    try {
      return JSON.parse(localStorage.getItem('ascendy-uikit') || '{}');
    } catch (e) {
      return {};
    }
  })();
  const [theme, setThemeState] = React.useState(saved.theme || 'kawaii');
  const [dark, setDark] = React.useState(!!saved.dark);
  const [screen, setScreen] = React.useState('home');
  const [active, setActive] = React.useState(false);
  const [minutes, setMinutes] = React.useState(0);
  const [goal, setGoal] = React.useState(60);
  const [safety, setSafety] = React.useState(480);
  const [lockdown, setLockdown] = React.useState(false);
  React.useEffect(() => {
    localStorage.setItem('ascendy-uikit', JSON.stringify({
      theme,
      dark
    }));
  }, [theme, dark]);
  React.useEffect(() => {
    if (!active) return;
    const t = setInterval(() => setMinutes(m => m + 1), 4000); // sped-up demo clock
    return () => clearInterval(t);
  }, [active]);
  const v = window.ascendyVocab[theme];
  const app = {
    go: setScreen,
    setTheme: setThemeState,
    goal,
    setGoal,
    safety,
    setSafety,
    lockdown,
    setLockdown,
    active,
    minutes: Math.max(1, minutes),
    streak: 12,
    todayMin: 42,
    setupDone: false,
    toggleSession: () => {
      setActive(a => !a);
      setMinutes(0);
    }
  };
  const Screen = screen === 'settings' ? SettingsScreen : screen === 'stats' ? StatsScreen : HomeScreen;
  return /*#__PURE__*/React.createElement("div", {
    style: {
      display: 'flex',
      justifyContent: 'center'
    }
  }, /*#__PURE__*/React.createElement("div", {
    style: {
      width: 412,
      position: 'relative'
    }
  }, /*#__PURE__*/React.createElement(ThemeScope, {
    theme: theme,
    dark: dark,
    style: {
      minHeight: '100vh'
    }
  }, /*#__PURE__*/React.createElement("div", {
    style: {
      display: 'flex',
      justifyContent: 'space-between',
      padding: '8px 20px 0',
      fontSize: 12,
      fontWeight: 500,
      color: 'var(--smoke)',
      fontFamily: 'var(--font-sans)'
    }
  }, /*#__PURE__*/React.createElement("span", null, "9:41"), /*#__PURE__*/React.createElement("span", {
    className: "msr",
    style: {
      fontSize: 14
    }
  }, "signal_cellular_alt")), /*#__PURE__*/React.createElement("div", {
    "data-screen-label": screen
  }, /*#__PURE__*/React.createElement(Screen, {
    v: v,
    theme: theme,
    app: app
  }))), /*#__PURE__*/React.createElement("button", {
    onClick: () => setDark(d => !d),
    title: "preview dark mode",
    style: {
      position: 'fixed',
      bottom: 12,
      right: 12,
      width: 36,
      height: 36,
      borderRadius: 18,
      border: '1px solid #ccc',
      background: '#fff',
      cursor: 'pointer',
      fontSize: 16,
      zIndex: 9
    }
  }, dark ? '☀' : '☾')));
}
ReactDOM.createRoot(document.getElementById('root')).render(/*#__PURE__*/React.createElement(AscendyApp, null));
})(); } catch (e) { __ds_ns.__errors.push({ path: "ui_kits/app/app.jsx", error: String((e && e.message) || e) }); }

// ui_kits/app/home_screen.jsx
try { (() => {
const DS = window.AscendyDesignSystem_01b7f9;
const {
  SoftCard,
  Badge,
  SelectableChip,
  Mascot,
  Switch: DSSwitch
} = DS;
const Icon = ({
  name,
  onClick,
  label
}) => /*#__PURE__*/React.createElement("span", {
  className: "msr",
  role: onClick ? 'button' : undefined,
  "aria-label": label,
  onClick: onClick,
  style: {
    cursor: onClick ? 'pointer' : undefined,
    padding: onClick ? 10 : 0,
    color: 'var(--ink)'
  }
}, name);
const H = ({
  children,
  style
}) => /*#__PURE__*/React.createElement("div", {
  style: {
    fontSize: 'var(--type-headline-size)',
    fontWeight: 'var(--type-headline-weight)',
    letterSpacing: 'var(--type-headline-tracking)',
    lineHeight: 'var(--type-headline-leading)',
    textTransform: 'var(--type-heading-transform)',
    color: 'var(--ink)',
    ...style
  }
}, children);
const SectionLabel = ({
  children
}) => /*#__PURE__*/React.createElement("div", {
  style: {
    fontSize: 'var(--type-title-size)',
    fontWeight: 'var(--type-title-weight)',
    letterSpacing: 'var(--type-title-tracking)',
    textTransform: 'var(--type-heading-transform)',
    color: 'var(--smoke)',
    margin: '20px 0 8px'
  }
}, children);
const TitleSm = {
  fontSize: 'var(--type-title-sm-size)',
  fontWeight: 'var(--type-title-sm-weight)',
  textTransform: 'var(--type-heading-transform)',
  color: 'var(--ink)'
};
const BodySm = {
  fontSize: 'var(--type-body-sm-size)',
  lineHeight: 'var(--type-body-sm-leading)',
  color: 'var(--smoke)'
};
function HomeScreen({
  v,
  theme,
  app
}) {
  const {
    active,
    minutes,
    streak,
    goal,
    todayMin,
    setupDone
  } = app;
  return /*#__PURE__*/React.createElement("div", {
    style: {
      padding: '16px 20px 24px'
    }
  }, /*#__PURE__*/React.createElement("div", {
    style: {
      display: 'flex',
      alignItems: 'center'
    }
  }, /*#__PURE__*/React.createElement(H, {
    style: {
      flex: 1
    }
  }, v.appTitle), /*#__PURE__*/React.createElement(Icon, {
    name: "qr_code_scanner",
    label: "scan qr",
    onClick: () => {}
  }), /*#__PURE__*/React.createElement(Icon, {
    name: "settings",
    label: "settings",
    onClick: () => app.go('settings')
  })), /*#__PURE__*/React.createElement("div", {
    style: {
      height: 16
    }
  }), /*#__PURE__*/React.createElement(SoftCard, {
    tone: "cloud",
    style: {
      display: 'flex',
      flexDirection: 'column',
      alignItems: 'center',
      gap: 8
    }
  }, /*#__PURE__*/React.createElement("div", {
    onClick: app.toggleSession,
    style: {
      cursor: 'pointer'
    },
    title: "long-press in the real app"
  }, /*#__PURE__*/React.createElement(Mascot, {
    variant: theme,
    locked: active,
    size: 176
  })), /*#__PURE__*/React.createElement("div", {
    style: {
      display: 'flex',
      gap: 6
    }
  }, /*#__PURE__*/React.createElement(Badge, {
    label: active ? v.statusFocusing : v.statusReady,
    tone: active ? 'lilac' : 'sage'
  }), streak > 0 && /*#__PURE__*/React.createElement(Badge, {
    label: fmt(v.streakBadgeFmt, streak),
    tone: "mint"
  })), /*#__PURE__*/React.createElement("div", {
    style: BodySm
  }, todayMin >= goal ? fmt(v.goalFmt, todayMin, goal) : fmt(v.goalFmt, todayMin, goal)), active ? /*#__PURE__*/React.createElement("div", {
    style: {
      textAlign: 'center'
    }
  }, /*#__PURE__*/React.createElement(H, null, fmt(v.timerMinFmt, minutes)), /*#__PURE__*/React.createElement("div", {
    style: {
      ...BodySm,
      marginTop: 4
    }
  }, v.heroActive), /*#__PURE__*/React.createElement("div", {
    style: {
      ...BodySm,
      marginTop: 6
    }
  }, fmt(v.appsSitesFmt, 7, 3))) : /*#__PURE__*/React.createElement("div", {
    style: {
      textAlign: 'center'
    }
  }, /*#__PURE__*/React.createElement("div", {
    style: TitleSm
  }, v.heroIdle), !setupDone && /*#__PURE__*/React.createElement("div", {
    style: {
      ...BodySm,
      marginTop: 4
    }
  }, v.longPressHint))), setupDone ? /*#__PURE__*/React.createElement("div", {
    style: {
      marginTop: 20
    }
  }, /*#__PURE__*/React.createElement(SoftCard, {
    tone: "cloud",
    style: {
      display: 'flex',
      alignItems: 'center',
      gap: 8,
      padding: '14px 20px'
    }
  }, /*#__PURE__*/React.createElement("span", {
    className: "msr",
    style: {
      color: 'var(--sage)',
      fontSize: 20
    }
  }, "check"), /*#__PURE__*/React.createElement("span", {
    style: TitleSm
  }, v.setupAllDone))) : /*#__PURE__*/React.createElement("div", null, /*#__PURE__*/React.createElement(SectionLabel, null, v.sectionSetup), /*#__PURE__*/React.createElement(SoftCard, {
    style: {
      padding: '4px 16px'
    }
  }, [[v.rowPair, true, '2'], [v.rowList, true, '3'], [v.rowPerms, false, v.badgeTodo]].map(([row, done, badge], i) => /*#__PURE__*/React.createElement("div", {
    key: i
  }, i > 0 && /*#__PURE__*/React.createElement("div", {
    style: {
      height: 1,
      background: 'var(--mist)'
    }
  }), /*#__PURE__*/React.createElement("div", {
    style: {
      display: 'flex',
      alignItems: 'center',
      gap: 10,
      padding: '14px 4px',
      cursor: 'pointer'
    }
  }, done ? /*#__PURE__*/React.createElement("span", {
    className: "msr",
    style: {
      color: 'var(--sage)',
      fontSize: 20
    }
  }, "check") : row[0] ? /*#__PURE__*/React.createElement("span", {
    style: {
      fontSize: 18
    }
  }, row[0]) : null, /*#__PURE__*/React.createElement("span", {
    style: {
      ...TitleSm,
      flex: 1
    }
  }, row[1]), /*#__PURE__*/React.createElement(Badge, {
    label: done ? badge : v.badgeTodo,
    tone: done ? 'sage' : 'petal'
  })))))), /*#__PURE__*/React.createElement("div", {
    style: {
      display: 'flex',
      gap: 8,
      marginTop: 16
    }
  }, /*#__PURE__*/React.createElement(SoftCard, {
    onClick: () => app.go('stats'),
    style: {
      flex: 1,
      padding: 16
    }
  }, /*#__PURE__*/React.createElement("div", {
    style: TitleSm
  }, v.statsTitle), /*#__PURE__*/React.createElement("div", {
    style: {
      ...BodySm,
      marginTop: 4
    }
  }, fmt(v.streakFmt, streak))), /*#__PURE__*/React.createElement(SoftCard, {
    style: {
      flex: 1,
      padding: 16,
      opacity: active ? 'var(--disabled-alpha)' : 1
    }
  }, /*#__PURE__*/React.createElement("div", {
    style: TitleSm
  }, v.pomodoroTitle), /*#__PURE__*/React.createElement("div", {
    style: {
      ...BodySm,
      marginTop: 4
    }
  }, v.pomodoro25))), active && /*#__PURE__*/React.createElement(SoftCard, {
    tone: "cloud",
    style: {
      marginTop: 16
    }
  }, /*#__PURE__*/React.createElement("div", {
    style: TitleSm
  }, v.emergencyTitle), /*#__PURE__*/React.createElement("div", {
    style: {
      ...BodySm,
      marginTop: 4
    }
  }, v.emergencyBody), /*#__PURE__*/React.createElement("div", {
    style: {
      marginTop: 8,
      color: 'var(--petal)',
      fontSize: 'var(--type-label-size)',
      fontWeight: 600,
      textTransform: 'var(--type-heading-transform)',
      cursor: 'pointer'
    },
    onClick: app.toggleSession
  }, v.emergencyButton)));
}
window.HomeScreen = HomeScreen;
})(); } catch (e) { __ds_ns.__errors.push({ path: "ui_kits/app/home_screen.jsx", error: String((e && e.message) || e) }); }

// ui_kits/app/screens.jsx
try { (() => {
const DS2 = window.AscendyDesignSystem_01b7f9;
const settingsStyles = {
  row: {
    background: 'var(--surface)',
    border: '1px solid var(--mist)',
    borderRadius: 'var(--radius-lg)',
    boxShadow: 'var(--shadow-card)',
    padding: '18px 20px',
    cursor: 'pointer',
    marginBottom: 8
  }
};
function BackHeader({
  v,
  title,
  app
}) {
  return /*#__PURE__*/React.createElement("div", {
    style: {
      display: 'flex',
      alignItems: 'center',
      gap: 4
    }
  }, /*#__PURE__*/React.createElement("span", {
    className: "msr",
    role: "button",
    "aria-label": v.back,
    onClick: () => app.go('home'),
    style: {
      cursor: 'pointer',
      padding: 10,
      color: 'var(--ink)'
    }
  }, "arrow_back"), /*#__PURE__*/React.createElement("span", {
    style: {
      fontSize: 'var(--type-headline-size)',
      fontWeight: 'var(--type-headline-weight)',
      letterSpacing: 'var(--type-headline-tracking)',
      textTransform: 'var(--type-heading-transform)',
      color: 'var(--ink)'
    }
  }, title));
}
window.BackHeader = BackHeader;
function SettingsScreen({
  v,
  theme,
  app
}) {
  const {
    SoftCard,
    Badge,
    SelectableChip,
    Switch: DSSwitch
  } = DS2;
  const titleSm = {
    fontSize: 'var(--type-title-sm-size)',
    fontWeight: 'var(--type-title-sm-weight)',
    textTransform: 'var(--type-heading-transform)',
    color: 'var(--ink)'
  };
  const bodySm = {
    fontSize: 'var(--type-body-sm-size)',
    lineHeight: 'var(--type-body-sm-leading)',
    color: 'var(--smoke)'
  };
  const sect = {
    fontSize: 'var(--type-title-size)',
    fontWeight: 'var(--type-title-weight)',
    textTransform: 'var(--type-heading-transform)',
    color: 'var(--smoke)',
    margin: '16px 0 8px'
  };
  const chipRow = (choices, cur, set) => /*#__PURE__*/React.createElement("div", {
    style: {
      display: 'flex',
      gap: 6,
      marginTop: 10
    }
  }, choices.map(m => /*#__PURE__*/React.createElement(SelectableChip, {
    key: m,
    label: m % 60 === 0 ? `${m / 60}h` : `${m}m`,
    selected: cur === m,
    onClick: () => set(m),
    style: {
      flex: 1,
      padding: '10px 0'
    }
  })));
  return /*#__PURE__*/React.createElement("div", {
    style: {
      padding: '16px 20px 24px'
    }
  }, /*#__PURE__*/React.createElement(BackHeader, {
    v: v,
    title: v.settingsTitle,
    app: app
  }), /*#__PURE__*/React.createElement("div", {
    style: {
      ...sect,
      marginTop: 8
    }
  }, fmt(v.currentFmt, v.themes[theme][0])), ['kawaii', 'tough', 'neutral'].map(t => /*#__PURE__*/React.createElement("div", {
    key: t,
    style: {
      ...settingsStyles.row,
      padding: '14px 18px',
      display: 'flex',
      alignItems: 'center',
      gap: 14
    },
    onClick: () => app.setTheme(t)
  }, /*#__PURE__*/React.createElement("img", {
    src: `../../assets/theme-icons/theme_icon_${t}.png`,
    alt: "",
    style: {
      width: 52,
      height: 52,
      borderRadius: 'var(--radius-md)',
      objectFit: 'cover'
    }
  }), /*#__PURE__*/React.createElement("div", {
    style: {
      flex: 1
    }
  }, /*#__PURE__*/React.createElement("div", {
    style: titleSm
  }, v.themes[t][0]), /*#__PURE__*/React.createElement("div", {
    style: bodySm
  }, v.themes[t][1])), /*#__PURE__*/React.createElement(Badge, {
    label: theme === t ? v.badgeActive : v.badgeSelect,
    tone: theme === t ? 'sage' : 'mint'
  }))), /*#__PURE__*/React.createElement("div", {
    style: sect
  }, v.sectionMore), [v.rowStats, v.rowSchedules, v.rowPomodoro, v.rowAbout].map((label, i) => /*#__PURE__*/React.createElement("div", {
    key: i,
    style: {
      ...settingsStyles.row,
      ...titleSm
    },
    onClick: i === 0 ? () => app.go('stats') : undefined
  }, label)), /*#__PURE__*/React.createElement(SoftCard, {
    style: {
      marginTop: 12
    }
  }, /*#__PURE__*/React.createElement("div", {
    style: titleSm
  }, v.goalTitle), /*#__PURE__*/React.createElement("div", {
    style: {
      ...bodySm,
      marginTop: 4
    }
  }, v.goalBody), chipRow([30, 60, 120], app.goal, app.setGoal), chipRow([180, 240, 360], app.goal, app.setGoal)), /*#__PURE__*/React.createElement(SoftCard, {
    style: {
      marginTop: 8
    }
  }, /*#__PURE__*/React.createElement("div", {
    style: titleSm
  }, v.safetyTitle), /*#__PURE__*/React.createElement("div", {
    style: {
      ...bodySm,
      marginTop: 4
    }
  }, v.safetyBody), chipRow([60, 120, 240], app.safety, app.setSafety), chipRow([480, 720, 1440], app.safety, app.setSafety)), /*#__PURE__*/React.createElement(SoftCard, {
    style: {
      marginTop: 8
    }
  }, /*#__PURE__*/React.createElement("div", {
    style: {
      display: 'flex',
      alignItems: 'center',
      gap: 12
    }
  }, /*#__PURE__*/React.createElement("div", {
    style: {
      flex: 1
    }
  }, /*#__PURE__*/React.createElement("div", {
    style: titleSm
  }, v.lockdownTitle), /*#__PURE__*/React.createElement("div", {
    style: {
      ...bodySm,
      marginTop: 4
    }
  }, v.lockdownBody)), /*#__PURE__*/React.createElement(DSSwitch, {
    checked: app.lockdown,
    onChange: app.setLockdown
  }))), /*#__PURE__*/React.createElement(SoftCard, {
    tone: "cloud",
    style: {
      marginTop: 24
    }
  }, /*#__PURE__*/React.createElement("div", {
    style: {
      fontSize: 'var(--type-body-md-size)',
      color: 'var(--smoke)'
    }
  }, v.footer)));
}
window.SettingsScreen = SettingsScreen;
function StatsScreen({
  v,
  theme,
  app
}) {
  const {
    SoftCard
  } = DS2;
  const buckets = [35, 80, 20, 65, 120, 45, 90],
    best = 4,
    days = ['S', 'M', 'T', 'W', 'T', 'F', 'S'];
  const max = 120;
  const titleSm = {
    fontSize: 'var(--type-title-sm-size)',
    fontWeight: 'var(--type-title-sm-weight)',
    textTransform: 'var(--type-heading-transform)',
    color: 'var(--ink)'
  };
  const bodySm = {
    fontSize: 'var(--type-body-sm-size)',
    color: 'var(--smoke)'
  };
  const sect = {
    fontSize: 'var(--type-title-size)',
    fontWeight: 'var(--type-title-weight)',
    textTransform: 'var(--type-heading-transform)',
    color: 'var(--smoke)',
    margin: '20px 0 8px'
  };
  const fmtMin = m => m >= 60 ? `${Math.floor(m / 60)}h ${m % 60}m` : `${m}m`;
  return /*#__PURE__*/React.createElement("div", {
    style: {
      padding: '16px 20px 24px'
    }
  }, /*#__PURE__*/React.createElement(BackHeader, {
    v: v,
    title: v.statsTitle,
    app: app
  }), /*#__PURE__*/React.createElement("div", {
    style: {
      height: 8
    }
  }), /*#__PURE__*/React.createElement(SoftCard, {
    tone: "cloud",
    style: {
      textAlign: 'center'
    }
  }, /*#__PURE__*/React.createElement("div", {
    style: {
      fontSize: 20,
      fontWeight: 'var(--type-headline-weight)',
      textTransform: 'var(--type-heading-transform)',
      color: 'var(--ink)'
    }
  }, fmt(v.streakFmt, app.streak)), /*#__PURE__*/React.createElement("div", {
    style: {
      ...bodySm,
      marginTop: 4
    }
  }, v.achievement)), /*#__PURE__*/React.createElement("div", {
    style: {
      display: 'flex',
      gap: 8,
      marginTop: 12
    }
  }, [[v.statsToday, fmtMin(app.todayMin)], [v.statsWeek, '7h 35m'], [v.statsAllTime, '31h 10m']].map(([l, val], i) => /*#__PURE__*/React.createElement(SoftCard, {
    key: i,
    style: {
      flex: 1,
      textAlign: 'center',
      padding: 14
    }
  }, /*#__PURE__*/React.createElement("div", {
    style: titleSm
  }, val), /*#__PURE__*/React.createElement("div", {
    style: {
      ...bodySm,
      marginTop: 2
    }
  }, l)))), /*#__PURE__*/React.createElement("div", {
    style: sect
  }, v.chartLabel), /*#__PURE__*/React.createElement(SoftCard, null, /*#__PURE__*/React.createElement("div", {
    style: {
      display: 'flex',
      alignItems: 'flex-end',
      height: 110,
      gap: 6
    }
  }, buckets.map((b, i) => /*#__PURE__*/React.createElement("div", {
    key: i,
    style: {
      flex: 1,
      display: 'flex',
      justifyContent: 'center'
    }
  }, /*#__PURE__*/React.createElement("div", {
    style: {
      width: '55%',
      maxWidth: 44,
      height: Math.max(2, b / max * 100),
      background: i === best ? 'var(--lilac)' : 'var(--petal)',
      borderRadius: theme === 'tough' ? 3 : 10
    }
  })))), /*#__PURE__*/React.createElement("div", {
    style: {
      display: 'flex',
      marginTop: 6
    }
  }, days.map((d, i) => /*#__PURE__*/React.createElement("div", {
    key: i,
    style: {
      flex: 1,
      textAlign: 'center',
      fontSize: 'var(--type-body-sm-size)',
      color: i === best ? 'var(--ink)' : 'var(--smoke)'
    }
  }, d))), /*#__PURE__*/React.createElement("div", {
    style: {
      ...bodySm,
      marginTop: 6
    }
  }, v.bestDay, ": T \xB7 2h 0m")), /*#__PURE__*/React.createElement("div", {
    style: sect
  }, v.statsRecent), [['Tue Jul 14, 09:12', '1h 45m · nfc'], ['Mon Jul 13, 14:03', '50m · qr'], ['Mon Jul 13, 08:30', '2h 0m · schedule']].map(([when, meta], i) => /*#__PURE__*/React.createElement(SoftCard, {
    key: i,
    style: {
      padding: '14px 20px',
      marginBottom: 6
    }
  }, /*#__PURE__*/React.createElement("div", {
    style: titleSm
  }, when), /*#__PURE__*/React.createElement("div", {
    style: {
      ...bodySm,
      marginTop: 2
    }
  }, meta))));
}
window.StatsScreen = StatsScreen;
})(); } catch (e) { __ds_ns.__errors.push({ path: "ui_kits/app/screens.jsx", error: String((e && e.message) || e) }); }

// ui_kits/app/vocab.js
try { (() => {
// Per-theme copy — verbatim subset of app/src/main/java/com/ascendy/app/ui/theme/Vocab.kt
window.ascendyVocab = {
  kawaii: {
    appTitle: 'ascendy ♡',
    back: 'back',
    statusReady: 'ready',
    statusFocusing: 'focusing',
    heroIdle: "tap your tag whenever you're ready ✨",
    heroActive: "you're focusing — tap your tag to come back 🌙",
    longPressHint: 'hold the little guy to start without a tag ♡',
    sectionSetup: 'setup',
    rowPair: ['🌸', 'pair an nfc tag'],
    rowList: ['✨', 'build your focus list'],
    rowPerms: ['🔒', 'permissions'],
    badgeTodo: 'todo',
    badgeOk: 'ok',
    setupAllDone: 'all set ♡',
    statsTitle: 'your stats ♡',
    streakFmt: '%d-day streak 🔥',
    pomodoroTitle: 'quick lock ⏱',
    pomodoro25: '25 min',
    emergencyTitle: 'emergency unlock',
    emergencyBody: 'one-time per session. for true emergencies only.',
    emergencyButton: 'use unlock',
    settingsTitle: 'settings',
    currentFmt: 'theme: %s',
    themes: {
      kawaii: ['kawaii ♡', 'soft pink, blush cheeks, soothing curves'],
      tough: ['tough ⛓', 'iron chains, hard edges, scowling mascot'],
      neutral: ['neutral', 'corporate, clean, no decoration']
    },
    badgeActive: 'active',
    badgeSelect: 'tap',
    sectionMore: 'more',
    rowStats: 'your stats 🌸',
    rowSchedules: 'scheduled focus ✨',
    rowPomodoro: 'quick lock ⏱',
    rowAbout: 'about ♡',
    goalTitle: 'daily focus goal ♡',
    goalBody: 'pick how many minutes you want to focus each day. your little guy cheers when you hit it.',
    safetyTitle: 'max session length',
    safetyBody: 'every session auto-ends after this (safety in case you lose your tag/qr).',
    lockdownTitle: 'lockdown mode 🔐',
    lockdownBody: "stops you wriggling out mid-session. ascendy can't be uninstalled, and you can't reach the settings pages that turn it off, until your session ends. your safety timer is still your way out ♡",
    footer: 'more themes coming soon — drop ideas via the github repo.',
    statsToday: 'today',
    statsWeek: 'this week',
    statsAllTime: 'all time',
    statsRecent: 'recent sessions',
    chartLabel: 'last 7 days',
    bestDay: 'best day this week',
    achievement: '7-day streak: your little guy got a headband 🌸',
    timerMinFmt: '%d min focused',
    goalFmt: '%d / %d min today',
    appsSitesFmt: '%d apps · %d sites blocked',
    streakBadgeFmt: '🔥 %d'
  },
  tough: {
    appTitle: 'ASCENDY ⛓',
    back: 'BACK',
    statusReady: 'READY',
    statusFocusing: 'LOCKED IN',
    heroIdle: 'TAP THE ANCHOR. GET TO WORK.',
    heroActive: 'LOCKED IN. TAP THE ANCHOR TO BREAK.',
    longPressHint: 'HOLD THE GUY TO LOCK IN WITHOUT AN ANCHOR.',
    sectionSetup: 'SETUP',
    rowPair: ['⛓', 'ANCHOR'],
    rowList: ['🔥', 'BLOCKLIST'],
    rowPerms: ['🛡', 'PERMISSIONS'],
    badgeTodo: 'TODO',
    badgeOk: 'DONE',
    setupAllDone: 'ALL SET.',
    statsTitle: 'STATS',
    streakFmt: '%d-DAY STREAK ⛓',
    pomodoroTitle: 'QUICK LOCK',
    pomodoro25: '25 MIN',
    emergencyTitle: 'BREAK GLASS',
    emergencyBody: "one use. session only. no reset. don't waste it.",
    emergencyButton: 'USE OVERRIDE',
    settingsTitle: 'SETTINGS',
    currentFmt: 'theme: %s',
    themes: {
      kawaii: ['kawaii ♡', 'soft pink, blush cheeks, soothing curves'],
      tough: ['tough ⛓', 'iron chains, hard edges, scowling mascot'],
      neutral: ['neutral', 'corporate, clean, no decoration']
    },
    badgeActive: 'ACTIVE',
    badgeSelect: 'TAP',
    sectionMore: 'MORE',
    rowStats: 'STATS',
    rowSchedules: 'SCHEDULES',
    rowPomodoro: 'QUICK LOCK',
    rowAbout: 'ABOUT',
    goalTitle: 'DAILY FOCUS GOAL',
    goalBody: 'set a minimum. hit it. no excuses.',
    safetyTitle: 'MAX SESSION LENGTH',
    safetyBody: 'every session auto-ends after this. fail-safe for a lost anchor.',
    lockdownTitle: 'LOCKDOWN 🔒',
    lockdownBody: "no escape hatches. ascendy can't be uninstalled and the settings pages that kill it are sealed until the session ends. the safety timer is your only exit. choose it on purpose.",
    footer: 'more themes coming soon. drop ideas via the github repo.',
    statsToday: 'TODAY',
    statsWeek: 'THIS WEEK',
    statsAllTime: 'ALL TIME',
    statsRecent: 'RECENT SESSIONS',
    chartLabel: 'LAST 7 DAYS',
    bestDay: 'BEST DAY',
    achievement: '7-DAY STREAK: HEADBAND EARNED.',
    timerMinFmt: '%d MIN LOCKED IN',
    goalFmt: '%d / %d MIN TODAY',
    appsSitesFmt: '%d APPS · %d SITES BLOCKED',
    streakBadgeFmt: '%d ⛓'
  },
  neutral: {
    appTitle: 'Ascendy',
    back: 'Back',
    statusReady: 'Idle',
    statusFocusing: 'Active',
    heroIdle: 'Tap your tag to begin a focus session.',
    heroActive: 'Focus session active. Tap your tag to end it.',
    longPressHint: 'Long-press the icon to start a session without a tag.',
    sectionSetup: 'Setup',
    rowPair: ['', 'Pair a tag'],
    rowList: ['', 'Block list'],
    rowPerms: ['', 'Permissions'],
    badgeTodo: 'Pending',
    badgeOk: 'Done',
    setupAllDone: 'Setup complete',
    statsTitle: 'Statistics',
    streakFmt: '%d-day streak',
    pomodoroTitle: 'Timed session',
    pomodoro25: '25 min',
    emergencyTitle: 'Emergency override',
    emergencyBody: 'Single use per session. Cannot be reset until the session ends.',
    emergencyButton: 'Use override',
    settingsTitle: 'Settings',
    currentFmt: 'Theme: %s',
    themes: {
      kawaii: ['Kawaii', 'Soft pink, expressive mascot, generous curves.'],
      tough: ['Tough', 'Iron chains, hard edges, scowling mascot.'],
      neutral: ['Neutral', 'Corporate, clean, no decoration.']
    },
    badgeActive: 'Active',
    badgeSelect: 'Select',
    sectionMore: 'More',
    rowStats: 'Statistics',
    rowSchedules: 'Scheduled sessions',
    rowPomodoro: 'Timed session',
    rowAbout: 'About',
    goalTitle: 'Daily focus goal',
    goalBody: 'Set a daily target for focused minutes. Progress is tracked on the home screen.',
    safetyTitle: 'Maximum session length',
    safetyBody: 'Every session auto-ends after this duration. Provides a fail-safe if you lose access to your tag or QR code.',
    lockdownTitle: 'Lockdown mode',
    lockdownBody: 'Prevents bypassing a session. While Lockdown is on, Ascendy cannot be uninstalled, and the Settings screens used to disable it are blocked for the duration of an active session.',
    footer: 'More themes can be added. Suggestions welcome via the GitHub repo.',
    statsToday: 'Today',
    statsWeek: 'This week',
    statsAllTime: 'All time',
    statsRecent: 'Recent sessions',
    chartLabel: 'Last 7 days',
    bestDay: 'Best day this week',
    achievement: '7-day streak achieved.',
    timerMinFmt: 'Focused for %d min',
    goalFmt: '%d / %d min today',
    appsSitesFmt: '%d apps · %d sites blocked',
    streakBadgeFmt: '%d-day'
  }
};
window.fmt = (s, ...args) => {
  let i = 0;
  return s.replace(/%d|%s/g, () => String(args[i++]));
};
})(); } catch (e) { __ds_ns.__errors.push({ path: "ui_kits/app/vocab.js", error: String((e && e.message) || e) }); }

__ds_ns.Mascot = __ds_scope.Mascot;

__ds_ns.Badge = __ds_scope.Badge;

__ds_ns.PageColumn = __ds_scope.PageColumn;

__ds_ns.SelectableChip = __ds_scope.SelectableChip;

__ds_ns.SoftCard = __ds_scope.SoftCard;

__ds_ns.ThemeScope = __ds_scope.ThemeScope;

__ds_ns.Button = __ds_scope.Button;

__ds_ns.Switch = __ds_scope.Switch;

__ds_ns.TextField = __ds_scope.TextField;

})();
