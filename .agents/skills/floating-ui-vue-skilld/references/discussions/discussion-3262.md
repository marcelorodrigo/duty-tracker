---
number: 3262
title: "[React] Issue: Nested hover popovers close prematurely during mouse transition"
category: Q&A
created: 2025-03-27
url: "https://github.com/floating-ui/floating-ui/discussions/3262"
upvotes: 3
comments: 1
answered: false
---

# [React] Issue: Nested hover popovers close prematurely during mouse transition

**Summary:**  
We're implementing a nested popover system using Floating UI with `useHover` behavior, and encountering persistent issues where **moving the mouse from a parent popover to a child popover causes both to close prematurely**, even with `safePolygon` configured. This makes it nearly impossible to interact with nested hover-based menus or tooltips.

---

###  **Expected Behavior**
When hovering over a parent popover and transitioning to a child popover (e.g., submenu or popover inside the parent), the parent should remain open until the user fully leaves the interactive area (both parent and child).

---

###  **Actual Behavior**
- The parent popover closes before the cursor reaches the child popover.
- The child opens briefly, then dismisses instantly.
- This beh...

---

## Top Comments

**@Seanathon**:

###  Addendum: Attempted `useHover({ enabled })` Workaround (didn’t work)

Following this suggestion by @atomiks, I attempted to resolve the nested hover popover dismissal issue by **disabling the `useHover` hook on the parent popover while a child popover is active**.

####  Implementation
I added logic like the following to conditionally disable hover tracking:

```tsx
const hover = useHover(context, {
  enabled: triggerOnHover && !isControlled && !isChildPopoverOpen,
  handleClose: safePolygon({
    blockPointerEvents: true,
    buffer: 400,
    requireIntent: false,
  }),
  delay: { open: 100, close: 1200 },
})
```...