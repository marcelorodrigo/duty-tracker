---
number: 3369
title: Incorrect availableWidth in size middleware
category: Q&A
created: 2025-07-24
url: "https://github.com/floating-ui/floating-ui/discussions/3369"
upvotes: 1
comments: 1
answered: false
---

# Incorrect availableWidth in size middleware

Hello! Please tell me why in one case availableWidth is calculated correctly when scaling the screen (when the trigger is on the left), but in another case it is not

Usage:

```
  const { context, refs, middlewareData, floatingStyles } = useFloating({
    open: showTooltip,
    strategy: position,
    middleware: [
      offset(13),
      flip({
        fallbackPlacements: placementDecorator([placement, ...(preferPositions || [])]),
      }),
      size({
        apply({ availableWidth, elements }) {
          elements.floating.style.width = `${availableWidth}px`;
        },
      }),
      hide(),
    ],
    placement,
    onOpenChange: setShowTooltip,
    whileElementsMounted: (reference, floating, update) =>
      autoUpdate(reference, floating, update, {
        layoutShift: false,
      }),
  });
```...

---

## Top Comments

**@atomiks** [maintainer]:

When it doesn't fit on `top-end`, `flip()` is setting it to `top` (center alignment). In order to preserve the center alignment, the size is quite small on the x-axis. You're missing `shift()` in the middleware array; that would allow it to be larger since it doesn't need to preserve the `center` alignment. 

The reason it works on the bottom is because of the ordering of the placements most likely, not 100% certain without a code repro