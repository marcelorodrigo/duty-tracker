---
number: 3405
title: "`refs.setFloating` triggering React Compiler lint error \"Cannot access refs during render\""
category: React
created: 2025-10-12
url: "https://github.com/floating-ui/floating-ui/discussions/3405"
upvotes: 2
comments: 2
answered: true
---

# `refs.setFloating` triggering React Compiler lint error "Cannot access refs during render"

The docs on `useFloating` recommend setting `ref` to `refs.setFloating` like this:

```tsx
<div ref={refs.setFloating}/>
```

This usage triggers a lint error "Cannot access refs during render" from the new rule `react-hooks/refs` that was added for React Compiler.

I wonder, is this a false-positive or is it a bug in this library?

---

## Accepted Answer

**@atomiks** [maintainer]:

It's a false positive in the lint rule
`refs` is a plain object and `setFloating` is a state setter (used as a callback ref). No refs are being read in render.

Seems like there's a bug with how refs are inferred: https://github.com/facebook/react/issues/34775