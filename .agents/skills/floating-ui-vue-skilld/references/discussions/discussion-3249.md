---
number: 3249
title: Correct usage of safePolygon with useHover
category: Q&A
created: 2025-03-11
url: "https://github.com/floating-ui/floating-ui/discussions/3249"
upvotes: 1
comments: 1
answered: true
---

# Correct usage of safePolygon with useHover

I want user to comfortably move to floating element (without it getting close) and should be able to press the button in it. My code sample is in below sandbox. I am using `safePolygon` as prescribed for such use-case, but it is not working in following sandbox.

> A “safe” polygon is one that a pointer is safe to traverse as it moves off the reference element and toward the floating element after hovering it. If the pointer moves outside of this safe area, the floating element closes.

https://codesandbox.io/p/sandbox/late-https-lu3833

I may be missing something very basic, Appreciate any idea/help. thanks.

My code is - 

...

---

## Accepted Answer

It seem, to make the desired behavior work, it is mandatory to pass `blockPointerEvents: true` to `safePolygon` 
from the code here - 
https://github.com/floating-ui/floating-ui/blob/b9ad108d9b0257fe1d7d25a3b187fd5189062b9a/website/lib/components/Home/DropdownMenu.js#L89

Now it is working as I desired for me. Thanks.