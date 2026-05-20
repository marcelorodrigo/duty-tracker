---
number: 3242
title: Trying to position tooltip with bottom preference
category: Q&A
created: 2025-03-06
url: "https://github.com/floating-ui/floating-ui/discussions/3242"
upvotes: 1
comments: 1
answered: true
---

# Trying to position tooltip with bottom preference

Here's what I am doing (pretending that `bottom-start` is the default, but not the only possible placement):

```js
computePosition(buttonElement.current, tooltipElement.current, {
    strategy: "fixed",
    placement: "bottom-start",
    middleware: [
        autoPlacement({
            alignment: "bottom-start" as any,
            crossAxis: true,
        }),
        shift(),
        offset(7),
    ],
}).then(({x, y}) => {
    setTooltipX(x);
    setTooltipY(y);
});
```

The results aren't as I expect:

- ~~The resulting positions keep varying even without changes to viewport.~~ (For some reason, that has gone away.)
- The resulting position is sometimes inside/over the button, rather than below, or above, or at the side of the button.

How can I fix that?

---

## Accepted Answer

P.S. perhaps it didn't position properly because the tooltip was having a `none` display style before using floating-ui, having an empty size.