---
number: 3411
title: Why target is detaching when it's fixed?
category: Q&A
created: 2025-11-24
url: "https://github.com/floating-ui/floating-ui/discussions/3411"
upvotes: 1
comments: 0
answered: false
---

# Why target is detaching when it's fixed?

I have a fixed sidebar on the left side of the page.

And within that sidebar, I have a menu with `overflow: hidden` and `position: relative`. This creates a clipping context, so I need to use the `fixed` strategy.

But when I scroll the page, the target element becomes detached from the reference.

Why?

See this example:

https://codepen.io/dzlmsxld-the-reactor/pen/raepmwE


https://github.com/user-attachments/assets/9654eaf2-4295-4585-ae2b-24b0e7c93a0e

