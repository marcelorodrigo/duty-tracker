---
number: 3412
title: "[Tooltip] Accessibility issue : tooltip closes when hovering its content"
type: docs
state: open
created: 2025-11-25
url: "https://github.com/floating-ui/floating-ui/issues/3412"
reactions: 0
comments: 1
labels: "[docs]"
---

# [Tooltip] Accessibility issue : tooltip closes when hovering its content

**Describe the bug** A clear and concise description of what the bug is.

Citing WAI ARIA tooltip patterns :
> If the tooltip is invoked when a pointing cursor moves over the trigger element, then it remains open as long as the cursor is over the trigger or the tooltip.

**To Reproduce**

https://codesandbox.io/p/sandbox/cranky-river-ifx8s9?file=%2Fsrc%2FApp.tsx

Steps to reproduce the behavior:

1. Hover over the button
2. Tooltip appears
3. Try to hover over the tooltip content
4. Tooltip disappears

**Expected behavior** A clear and concise description of what you expected to
happen.

Tooltip should stay open while user hovers on trigger or on tooltip content.

...