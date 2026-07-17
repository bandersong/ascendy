TextField — Material-3 outlined input (tag nicknames, list names, friction-tax sentence). Label notches into the border; Petal 2px ring on focus.

```jsx
<TextField label="give it a name (e.g. kitchen)" value={name} onChange={setName} />
```

Set `surface` to the parent card's fill (e.g. `var(--cloud)`) so the label notch blends.
