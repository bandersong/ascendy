export interface BadgeProps {
  label: string;
  /** palette fill: sage (done/success), petal (todo/strict), mint (streak), lilac (focusing), cloud, smoke */
  tone?: 'sage' | 'petal' | 'mint' | 'lilac' | 'cloud' | 'smoke';
  style?: React.CSSProperties;
}
export declare function Badge(props: BadgeProps): JSX.Element;
