export interface TextFieldProps {
  /** floating label notched into the top border */
  label?: string;
  value?: string;
  onChange?: (value: string) => void;
  placeholder?: string;
  type?: string;
  /** background color behind the notched label — match the parent card (default var(--surface)) */
  surface?: string;
  style?: React.CSSProperties;
}
export declare function TextField(props: TextFieldProps): JSX.Element;
