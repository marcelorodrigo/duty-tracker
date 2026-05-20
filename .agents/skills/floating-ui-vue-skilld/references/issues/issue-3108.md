---
number: 3108
title: "Issue with boundary[] in hide middleware"
type: bug
state: open
created: 2024-11-06
url: "https://github.com/floating-ui/floating-ui/issues/3108"
reactions: 0
comments: 4
labels: "[bug, has workaround]"
---

# Issue with boundary[] in hide middleware

When we place a modal inside a drawer and then place a panel inside the drawer, the floating UI position becomes incorrect.

All use popover attribute and uses top-layer.

To overcome this , we used the solution suggested in https://github.com/floating-ui/floating-ui/issues/2858

As per the suggestion we used empty boundary (boundary:[]) for both shift and hide middlewares. It is working fine , but we are facing an issue in hide middleware.

We are hiding the floating element using
        contentElement.style.display = middlewareData.hide?.referenceHidden ? 'none' : 'block';
in compute Position
        
But, since boundary is empty, `middlewareData.hide?.referenceHidden` returns true only when the trigger element goes out of viewport. But, we need to hide the floating element ...