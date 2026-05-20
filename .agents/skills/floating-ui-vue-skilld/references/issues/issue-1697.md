---
number: 1697
title: "[React] Singletons (shared floating elements)"
type: feature
state: closed
created: 2022-05-12
url: "https://github.com/floating-ui/floating-ui/issues/1697"
reactions: 8
comments: 4
labels: "[feature]"
---

# [React] Singletons (shared floating elements)

Useful for:

- Layout-transitioned navigation mega-menus (like Stripe's)
- Smooth transitions in general (like Chrome's tab hovercard)

---

## Top Comments

**@atomiks** [maintainer] (+1):

Since singleton behavior can be faked using FLIP trickery + `FloatingDelayGroup`, only a small amount of code is necessary and you don't need `framer-motion`: https://codesandbox.io/s/friendly-dirac-mxm823?file=/src/App.tsx 

Downside of the above compared to `framer-motion` is that you don't get the transition of the dimensions, but I will _probably_ make a hook for layout transitions using plain CSS, like we have the `useTransition{X}` hooks.

Going to close this issue since singletons aren't planned anymore. 

**@atomiks** [maintainer] (+1):

There's a way to achieve transitions that look like a singleton without actually being one, using Framer Motion's layout animations. Since the size of the tooltip changes too, it looks great:

https://codesandbox.io/s/floating-ui-framer-motion-layout-transitions-skorzk

Given this is possible, I wonder what the main use case would be for singletons otherwise?

**@Nantris**:

Great example @atomiks - thanks for sharing. We ended up using `react-transition-group` so achieve something similar. It's a little clunky, but I agree that it undercuts the need to support singletons. Perhaps there's some other case that hasn't occurred to anyone yet but I don't see it right now.