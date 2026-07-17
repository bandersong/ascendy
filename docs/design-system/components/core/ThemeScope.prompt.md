Theme wrapper — apply one of Ascendy's six theme scopes (kawaii/tough/neutral × light/dark) to a subtree; every other component reads tokens from the nearest scope.

```jsx
<ThemeScope theme="tough" dark padded>
  <SoftCard>…</SoftCard>
</ThemeScope>
```

Default is kawaii light. Sets `data-theme`, Cream background, Ink text, and the font stack.
