---
number: 3307
title: Struggling with flip middleware after migrating from popper v2 to floating-ui
category: Q&A
created: 2025-04-29
url: "https://github.com/floating-ui/floating-ui/discussions/3307"
upvotes: 0
comments: 2
answered: false
---

# Struggling with flip middleware after migrating from popper v2 to floating-ui

Hello,

I am working on a design system and trying to move my dropdown menu (uses react-select) from popper v2 to floating-ui and facing some challenges.

Earlier, the popper automatically used to respect the footer shown and was positioned using `top-start` position since there was not enough space for it to open in downwards direction.

<img width="580" alt="image" src="https://github.com/user-attachments/assets/e3241241-bbb5-4a33-8e62-5d29911f652f" />

But after switching to `floating-ui`, it seems like the dropdown is checking for available space in the clipping container/parent. Since the parent has enough space for it to open, it opens in downwards direction. However, I want it to behave like earlier. 

What must I do to make it work like earlier?

As of now I am doing
```
const { floatingStyles } = useFloating({
    placement: 'bottom-start',
    middleware: [ flip({ padding: 10, fallbackPlacements: [ 'top-start' ] }) ],
    elements: {
      reference: containerRef,
      floating: menuRef
    },
    whileElementsMounted: autoUpdate
  });
```...

---

## Top Comments

**@karanmidha**:

There was a local css issue. The root parent had `overflow: hidden` which was causing the issue.

**@karanmidha**:

@atomiks Any idea how to go about this problem?