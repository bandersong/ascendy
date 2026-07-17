/** @startingPoint section="Ascendy" subtitle="Theme wrapper — kawaii / tough / neutral, light or dark" viewport="700x400" */
export interface ThemeScopeProps {
  /** 'kawaii' | 'tough' | 'neutral' */
  theme?: 'kawaii' | 'tough' | 'neutral';
  dark?: boolean;
  /** adds 20px inner padding */
  padded?: boolean;
  style?: React.CSSProperties;
  children?: React.ReactNode;
}
export declare function ThemeScope(props: ThemeScopeProps): JSX.Element;
