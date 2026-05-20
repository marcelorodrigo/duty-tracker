---
number: 3208
title: prevent popover from opening
category: Q&A
created: 2025-01-29
url: "https://github.com/floating-ui/floating-ui/discussions/3208"
upvotes: 1
comments: 0
answered: false
---

# prevent popover from opening

Is there any way to prevent a floating element from opening in runtime?
I was thinking something along the lines of `e.preventDefault()` in the `onClick`/`onMouseDown`/`onPointerDown` that is passed to the trigger with `getReferenceProps` but it doesn't seem to work

```
<input
      {...getReferenceProps({
        onClick: (e) => e.preventDefault(),
      })}
/>
```

(BTW, I'm using @floating-ui/react v0.26.25)