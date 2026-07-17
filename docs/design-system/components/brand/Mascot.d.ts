export interface MascotProps {
  /** which theme's mascot art */
  variant?: 'kawaii' | 'tough' | 'neutral';
  /** true = focusing/locked art, false = idle */
  locked?: boolean;
  /** px, hero size in the app is 176 */
  size?: number;
  /** ambient ±8px bob loop (2400ms) */
  bob?: boolean;
  style?: React.CSSProperties;
}
export declare function Mascot(props: MascotProps): JSX.Element;
