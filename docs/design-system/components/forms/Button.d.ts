export interface ButtonProps {
  children?: React.ReactNode;
  /** 'filled' (Petal pill, default), 'text' (TextButton), 'outlined' */
  variant?: 'filled' | 'text' | 'outlined';
  disabled?: boolean;
  onClick?: () => void;
  style?: React.CSSProperties;
}
export declare function Button(props: ButtonProps): JSX.Element;
