const DS = window.AscendyDesignSystem_01b7f9;
const { SoftCard, Badge, SelectableChip, Mascot, Switch: DSSwitch } = DS;
const Icon = ({ name, onClick, label }) => <span className="msr" role={onClick ? 'button' : undefined} aria-label={label} onClick={onClick} style={{ cursor: onClick ? 'pointer' : undefined, padding: onClick ? 10 : 0, color: 'var(--ink)' }}>{name}</span>;
const H = ({ children, style }) => <div style={{ fontSize: 'var(--type-headline-size)', fontWeight: 'var(--type-headline-weight)', letterSpacing: 'var(--type-headline-tracking)', lineHeight: 'var(--type-headline-leading)', textTransform: 'var(--type-heading-transform)', color: 'var(--ink)', ...style }}>{children}</div>;
const SectionLabel = ({ children }) => <div style={{ fontSize: 'var(--type-title-size)', fontWeight: 'var(--type-title-weight)', letterSpacing: 'var(--type-title-tracking)', textTransform: 'var(--type-heading-transform)', color: 'var(--smoke)', margin: '20px 0 8px' }}>{children}</div>;
const TitleSm = { fontSize: 'var(--type-title-sm-size)', fontWeight: 'var(--type-title-sm-weight)', textTransform: 'var(--type-heading-transform)', color: 'var(--ink)' };
const BodySm = { fontSize: 'var(--type-body-sm-size)', lineHeight: 'var(--type-body-sm-leading)', color: 'var(--smoke)' };

function HomeScreen({ v, theme, app }) {
  const { active, minutes, streak, goal, todayMin, setupDone } = app;
  return <div style={{ padding: '16px 20px 24px' }}>
    <div style={{ display: 'flex', alignItems: 'center' }}>
      <H style={{ flex: 1 }}>{v.appTitle}</H>
      <Icon name="qr_code_scanner" label="scan qr" onClick={() => {}} />
      <Icon name="settings" label="settings" onClick={() => app.go('settings')} />
    </div>
    <div style={{ height: 16 }}></div>
    <SoftCard tone="cloud" style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 8 }}>
      <div onClick={app.toggleSession} style={{ cursor: 'pointer' }} title="long-press in the real app">
        <Mascot variant={theme} locked={active} size={176} />
      </div>
      <div style={{ display: 'flex', gap: 6 }}>
        <Badge label={active ? v.statusFocusing : v.statusReady} tone={active ? 'lilac' : 'sage'} />
        {streak > 0 && <Badge label={fmt(v.streakBadgeFmt, streak)} tone="mint" />}
      </div>
      <div style={BodySm}>{todayMin >= goal ? fmt(v.goalFmt, todayMin, goal) : fmt(v.goalFmt, todayMin, goal)}</div>
      {active ? <div style={{ textAlign: 'center' }}>
        <H>{fmt(v.timerMinFmt, minutes)}</H>
        <div style={{ ...BodySm, marginTop: 4 }}>{v.heroActive}</div>
        <div style={{ ...BodySm, marginTop: 6 }}>{fmt(v.appsSitesFmt, 7, 3)}</div>
      </div> : <div style={{ textAlign: 'center' }}>
        <div style={TitleSm}>{v.heroIdle}</div>
        {!setupDone && <div style={{ ...BodySm, marginTop: 4 }}>{v.longPressHint}</div>}
      </div>}
    </SoftCard>
    {setupDone ? <div style={{ marginTop: 20 }}>
      <SoftCard tone="cloud" style={{ display: 'flex', alignItems: 'center', gap: 8, padding: '14px 20px' }}>
        <span className="msr" style={{ color: 'var(--sage)', fontSize: 20 }}>check</span>
        <span style={TitleSm}>{v.setupAllDone}</span>
      </SoftCard>
    </div> : <div>
      <SectionLabel>{v.sectionSetup}</SectionLabel>
      <SoftCard style={{ padding: '4px 16px' }}>
        {[[v.rowPair, true, '2'], [v.rowList, true, '3'], [v.rowPerms, false, v.badgeTodo]].map(([row, done, badge], i) => <div key={i}>
          {i > 0 && <div style={{ height: 1, background: 'var(--mist)' }}></div>}
          <div style={{ display: 'flex', alignItems: 'center', gap: 10, padding: '14px 4px', cursor: 'pointer' }}>
            {done ? <span className="msr" style={{ color: 'var(--sage)', fontSize: 20 }}>check</span> : (row[0] ? <span style={{ fontSize: 18 }}>{row[0]}</span> : null)}
            <span style={{ ...TitleSm, flex: 1 }}>{row[1]}</span>
            <Badge label={done ? badge : v.badgeTodo} tone={done ? 'sage' : 'petal'} />
          </div>
        </div>)}
      </SoftCard>
    </div>}
    <div style={{ display: 'flex', gap: 8, marginTop: 16 }}>
      <SoftCard onClick={() => app.go('stats')} style={{ flex: 1, padding: 16 }}>
        <div style={TitleSm}>{v.statsTitle}</div>
        <div style={{ ...BodySm, marginTop: 4 }}>{fmt(v.streakFmt, streak)}</div>
      </SoftCard>
      <SoftCard style={{ flex: 1, padding: 16, opacity: active ? 'var(--disabled-alpha)' : 1 }}>
        <div style={TitleSm}>{v.pomodoroTitle}</div>
        <div style={{ ...BodySm, marginTop: 4 }}>{v.pomodoro25}</div>
      </SoftCard>
    </div>
    {active && <SoftCard tone="cloud" style={{ marginTop: 16 }}>
      <div style={TitleSm}>{v.emergencyTitle}</div>
      <div style={{ ...BodySm, marginTop: 4 }}>{v.emergencyBody}</div>
      <div style={{ marginTop: 8, color: 'var(--petal)', fontSize: 'var(--type-label-size)', fontWeight: 600, textTransform: 'var(--type-heading-transform)', cursor: 'pointer' }} onClick={app.toggleSession}>{v.emergencyButton}</div>
    </SoftCard>}
  </div>;
}
window.HomeScreen = HomeScreen;
