const { ThemeScope } = window.AscendyDesignSystem_01b7f9;
function AscendyApp() {
  const saved = (() => { try { return JSON.parse(localStorage.getItem('ascendy-uikit') || '{}'); } catch (e) { return {}; } })();
  const [theme, setThemeState] = React.useState(saved.theme || 'kawaii');
  const [dark, setDark] = React.useState(!!saved.dark);
  const [screen, setScreen] = React.useState('home');
  const [active, setActive] = React.useState(false);
  const [minutes, setMinutes] = React.useState(0);
  const [goal, setGoal] = React.useState(60);
  const [safety, setSafety] = React.useState(480);
  const [lockdown, setLockdown] = React.useState(false);
  React.useEffect(() => { localStorage.setItem('ascendy-uikit', JSON.stringify({ theme, dark })); }, [theme, dark]);
  React.useEffect(() => {
    if (!active) return;
    const t = setInterval(() => setMinutes(m => m + 1), 4000); // sped-up demo clock
    return () => clearInterval(t);
  }, [active]);
  const v = window.ascendyVocab[theme];
  const app = {
    go: setScreen, setTheme: setThemeState, goal, setGoal, safety, setSafety, lockdown, setLockdown,
    active, minutes: Math.max(1, minutes), streak: 12, todayMin: 42, setupDone: false,
    toggleSession: () => { setActive(a => !a); setMinutes(0); },
  };
  const Screen = screen === 'settings' ? SettingsScreen : screen === 'stats' ? StatsScreen : HomeScreen;
  return <div style={{ display: 'flex', justifyContent: 'center' }}>
    <div style={{ width: 412, position: 'relative' }}>
      <ThemeScope theme={theme} dark={dark} style={{ minHeight: '100vh' }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', padding: '8px 20px 0', fontSize: 12, fontWeight: 500, color: 'var(--smoke)', fontFamily: 'var(--font-sans)' }}>
          <span>9:41</span><span className="msr" style={{ fontSize: 14 }}>signal_cellular_alt</span>
        </div>
        <div data-screen-label={screen}><Screen v={v} theme={theme} app={app} /></div>
      </ThemeScope>
      <button onClick={() => setDark(d => !d)} title="preview dark mode" style={{ position: 'fixed', bottom: 12, right: 12, width: 36, height: 36, borderRadius: 18, border: '1px solid #ccc', background: '#fff', cursor: 'pointer', fontSize: 16, zIndex: 9 }}>{dark ? '☀' : '☾'}</button>
    </div>
  </div>;
}
ReactDOM.createRoot(document.getElementById('root')).render(<AscendyApp />);
