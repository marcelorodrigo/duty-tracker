---
number: 3304
title: Popper menu doesn't move when reference element position changes
category: Q&A
created: 2025-04-26
url: "https://github.com/floating-ui/floating-ui/discussions/3304"
upvotes: 1
comments: 1
answered: false
---

# Popper menu doesn't move when reference element position changes

I have a situation where the reference element moves while the menu is still open. However, the menu doesn't move along with reference element and stays at the initial place.

One solution that I have in mind is to use `ResizeObserver` and call the `update()` function whenever the callback passed to  `ResizeObserver` is called.

Does anyone know of any other better solution that can be used in this case?

---

## Top Comments

**@atomiks** [maintainer]:

This is already supported: https://floating-ui.com/docs/autoUpdate#layoutshift