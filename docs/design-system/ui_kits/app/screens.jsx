const DS2 = window.AscendyDesignSystem_01b7f9;
const settingsStyles = {
  row: { background: 'var(--surface)', border: '1px solid var(--mist)', borderRadius: 'var(--radius-lg)', boxShadow: 'var(--shadow-card)', padding: '18px 20px', cursor: 'pointer', marginBottom: 8 },
};
function BackHeader({ v, title, app }) {
  return <div style={{ display: 'flex', alignItems: 'center', gap: 4 }}>
    <span className="msr" role="button" aria-label={v.back} onClick={() => app.go('home')} style={{ cursor: 'pointer', padding: 10, color: 'var(--ink)' }}>arrow_back</span>
    <span style={{ fontSize: 'var(--type-headline-size)', fontWeight: 'var(--type-headline-weight)', letterSpacing: 'var(--type-headline-tracking)', textTransform: 'var(--type-heading-transform)', color: 'var(--ink)' }}>{title}</span>
  </div>;
}
window.BackHeader = BackHeader;

function SettingsScreen({ v, theme, app }) {
  const { SoftCard, Badge, SelectableChip, Switch: DSSwitch } = DS2;
  const titleSm = { fontSize: 'var(--type-title-sm-size)', fontWeight: 'var(--type-title-sm-weight)', textTransform: 'var(--type-heading-transform)', color: 'var(--ink)' };
  const bodySm = { fontSize: 'var(--type-body-sm-size)', lineHeight: 'var(--type-body-sm-leading)', color: 'var(--smoke)' };
  const sect = { fontSize: 'var(--type-title-size)', fontWeight: 'var(--type-title-weight)', textTransform: 'var(--type-heading-transform)', color: 'var(--smoke)', margin: '16px 0 8px' };
  const chipRow = (choices, cur, set) => <div style={{ display: 'flex', gap: 6, marginTop: 10 }}>
    {choices.map(m => <SelectableChip key={m} label={m % 60 === 0 ? `${m / 60}h` : `${m}m`} selected={cur === m} onClick={() => set(m)} style={{ flex: 1, padding: '10px 0' }} />)}
  </div>;
  return <div style={{ padding: '16px 20px 24px' }}>
    <BackHeader v={v} title={v.settingsTitle} app={app} />
    <div style={{ ...sect, marginTop: 8 }}>{fmt(v.currentFmt, v.themes[theme][0])}</div>
    {['kawaii', 'tough', 'neutral'].map(t => <div key={t} style={{ ...settingsStyles.row, padding: '14px 18px', display: 'flex', alignItems: 'center', gap: 14 }} onClick={() => app.setTheme(t)}>
      <img src={`../../assets/theme-icons/theme_icon_${t}.png`} alt="" style={{ width: 52, height: 52, borderRadius: 'var(--radius-md)', objectFit: 'cover' }} />
      <div style={{ flex: 1 }}>
        <div style={titleSm}>{v.themes[t][0]}</div>
        <div style={bodySm}>{v.themes[t][1]}</div>
      </div>
      <Badge label={theme === t ? v.badgeActive : v.badgeSelect} tone={theme === t ? 'sage' : 'mint'} />
    </div>)}
    <div style={sect}>{v.sectionMore}</div>
    {[v.rowStats, v.rowSchedules, v.rowPomodoro, v.rowAbout].map((label, i) => <div key={i} style={{ ...settingsStyles.row, ...titleSm }} onClick={i === 0 ? () => app.go('stats') : undefined}>{label}</div>)}
    <SoftCard style={{ marginTop: 12 }}>
      <div style={titleSm}>{v.goalTitle}</div>
      <div style={{ ...bodySm, marginTop: 4 }}>{v.goalBody}</div>
      {chipRow([30, 60, 120], app.goal, app.setGoal)}
      {chipRow([180, 240, 360], app.goal, app.setGoal)}
    </SoftCard>
    <SoftCard style={{ marginTop: 8 }}>
      <div style={titleSm}>{v.safetyTitle}</div>
      <div style={{ ...bodySm, marginTop: 4 }}>{v.safetyBody}</div>
      {chipRow([60, 120, 240], app.safety, app.setSafety)}
      {chipRow([480, 720, 1440], app.safety, app.setSafety)}
    </SoftCard>
    <SoftCard style={{ marginTop: 8 }}>
      <div style={{ display: 'flex', alignItems: 'center', gap: 12 }}>
        <div style={{ flex: 1 }}>
          <div style={titleSm}>{v.lockdownTitle}</div>
          <div style={{ ...bodySm, marginTop: 4 }}>{v.lockdownBody}</div>
        </div>
        <DSSwitch checked={app.lockdown} onChange={app.setLockdown} />
      </div>
    </SoftCard>
    <SoftCard tone="cloud" style={{ marginTop: 24 }}>
      <div style={{ fontSize: 'var(--type-body-md-size)', color: 'var(--smoke)' }}>{v.footer}</div>
    </SoftCard>
  </div>;
}
window.SettingsScreen = SettingsScreen;

function StatsScreen({ v, theme, app }) {
  const { SoftCard } = DS2;
  const buckets = [35, 80, 20, 65, 120, 45, 90], best = 4, days = ['S', 'M', 'T', 'W', 'T', 'F', 'S'];
  const max = 120;
  const titleSm = { fontSize: 'var(--type-title-sm-size)', fontWeight: 'var(--type-title-sm-weight)', textTransform: 'var(--type-heading-transform)', color: 'var(--ink)' };
  const bodySm = { fontSize: 'var(--type-body-sm-size)', color: 'var(--smoke)' };
  const sect = { fontSize: 'var(--type-title-size)', fontWeight: 'var(--type-title-weight)', textTransform: 'var(--type-heading-transform)', color: 'var(--smoke)', margin: '20px 0 8px' };
  const fmtMin = m => m >= 60 ? `${Math.floor(m / 60)}h ${m % 60}m` : `${m}m`;
  return <div style={{ padding: '16px 20px 24px' }}>
    <BackHeader v={v} title={v.statsTitle} app={app} />
    <div style={{ height: 8 }}></div>
    <SoftCard tone="cloud" style={{ textAlign: 'center' }}>
      <div style={{ fontSize: 20, fontWeight: 'var(--type-headline-weight)', textTransform: 'var(--type-heading-transform)', color: 'var(--ink)' }}>{fmt(v.streakFmt, app.streak)}</div>
      <div style={{ ...bodySm, marginTop: 4 }}>{v.achievement}</div>
    </SoftCard>
    <div style={{ display: 'flex', gap: 8, marginTop: 12 }}>
      {[[v.statsToday, fmtMin(app.todayMin)], [v.statsWeek, '7h 35m'], [v.statsAllTime, '31h 10m']].map(([l, val], i) => <SoftCard key={i} style={{ flex: 1, textAlign: 'center', padding: 14 }}>
        <div style={titleSm}>{val}</div>
        <div style={{ ...bodySm, marginTop: 2 }}>{l}</div>
      </SoftCard>)}
    </div>
    <div style={sect}>{v.chartLabel}</div>
    <SoftCard>
      <div style={{ display: 'flex', alignItems: 'flex-end', height: 110, gap: 6 }}>
        {buckets.map((b, i) => <div key={i} style={{ flex: 1, display: 'flex', justifyContent: 'center' }}>
          <div style={{ width: '55%', maxWidth: 44, height: Math.max(2, b / max * 100), background: i === best ? 'var(--lilac)' : 'var(--petal)', borderRadius: theme === 'tough' ? 3 : 10 }}></div>
        </div>)}
      </div>
      <div style={{ display: 'flex', marginTop: 6 }}>
        {days.map((d, i) => <div key={i} style={{ flex: 1, textAlign: 'center', fontSize: 'var(--type-body-sm-size)', color: i === best ? 'var(--ink)' : 'var(--smoke)' }}>{d}</div>)}
      </div>
      <div style={{ ...bodySm, marginTop: 6 }}>{v.bestDay}: T · 2h 0m</div>
    </SoftCard>
    <div style={sect}>{v.statsRecent}</div>
    {[['Tue Jul 14, 09:12', '1h 45m · nfc'], ['Mon Jul 13, 14:03', '50m · qr'], ['Mon Jul 13, 08:30', '2h 0m · schedule']].map(([when, meta], i) => <SoftCard key={i} style={{ padding: '14px 20px', marginBottom: 6 }}>
      <div style={titleSm}>{when}</div>
      <div style={{ ...bodySm, marginTop: 2 }}>{meta}</div>
    </SoftCard>)}
  </div>;
}
window.StatsScreen = StatsScreen;
