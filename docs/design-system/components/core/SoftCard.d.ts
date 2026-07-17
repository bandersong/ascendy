/** @startingPoint section="Ascendy" subtitle="SoftCard — the app's only card style" viewport="700x260" */
export interface SoftCardProps {
  /** 'surface' (default) or 'cloud' (soft secondary / primary-container) */
  tone?: 'surface' | 'cloud';
  /** if set, card is pressable and press-scales to 0.97 */
  onClick?: () => void;
  style?: React.CSSProperties;
  children?: React.ReactNode;
}
export declare function SoftCard(props: SoftCardProps): JSX.Element;
