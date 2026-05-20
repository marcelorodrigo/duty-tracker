---
number: 3269
title: useHover fails to properly handle mousemove/hover events within a closed Shadow DOM
type: other
state: open
created: 2025-04-03
url: "https://github.com/floating-ui/floating-ui/issues/3269"
reactions: 1
comments: 4
---

# useHover fails to properly handle mousemove/hover events within a closed Shadow DOM

The useHover hook does not trigger the floating element to open when the reference element is hovered over, if both elements are rendered inside a closed shadow DOM. Because the nodes are all inside of a closed shadowDOM, it cannot find the popover target and assumes we should close the popover

I suspect this is because internal utilities, like getDocument(element), resolve to the main document rather than the specific shadowRoot that contains the element and the event listeners. This likely leads to event listeners being attached to the document rather than a custom root e.g. shadowDOM root.

ie
```
// react/src/utils.ts
export function getDocument(node: Element | null) {
  return node?.ownerDocument || document;
}

...