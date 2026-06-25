#!/usr/bin/env bash
#
# Design-system guardrail — keeps the UI on the spacing grid.
#
# Spacing must come from the Space scale (VSpace / HSpace / Space.*), never a
# freehand .dp literal. See docs/UI_MASTERPIECE.md §1. This flags only the
# UNAMBIGUOUS spacing constructs, so it has no false positives on sizes:
#   - Spacer(Modifier.height/width/size(N.dp))   -> use VSpace/HSpace(Space.*)
#   - Arrangement.spacedBy(N.dp) / spacedBy(N.dp) -> use spacedBy(Space.*)
#
# Sizes (icon .size(20.dp), chart .height(120.dp), BorderStroke widths) stay raw
# and are intentionally NOT matched.
#
# Run locally: bash tools/check-design-tokens.sh
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
DIRS=(
  "$ROOT/app/src/main/java/com/ascendy/app/ui/screens"
  "$ROOT/app/src/main/java/com/ascendy/app/ui/components"
)

PATTERN='Spacer\(Modifier\.(height|width|size)\([0-9]+\.dp\)|spacedBy\([0-9]+\.dp\)'

hits="$(grep -REn "$PATTERN" "${DIRS[@]}" --include='*.kt' || true)"

if [ -n "$hits" ]; then
  echo "❌ Design-token guardrail failed: raw spacing .dp in UI code."
  echo "   Use VSpace/HSpace/Space.* instead (docs/UI_MASTERPIECE.md §1)."
  echo
  echo "$hits"
  exit 1
fi

echo "✅ Design tokens: no raw spacing .dp in screens/ or components/."
