---
number: 3243
title: How can I position Floating UI relative to one element but attach props and event handlers to another?
category: Q&A
created: 2025-03-07
url: "https://github.com/floating-ui/floating-ui/discussions/3243"
upvotes: 3
comments: 1
answered: false
---

# How can I position Floating UI relative to one element but attach props and event handlers to another?

Hello! I love the library and am actively implementing it within a design system at Zillow. However, I've hit an issue with a custom Combobox implementation.

My trigger area contains multiple elements, including an input element that should receive focus and handle events, but I want to position the floating element relative to the entire trigger container instead.

```tsx
const Combobox = (props) => {
  // all the useFloating logic as a separate hook, etc
  return (
    <>
      {/* I want to use `triggerRef` as the position reference for floating ref only */}
      <div ref={triggerRef}>
        <div>
          <button>Multi-select chip</button>
          {/* I want to use input as the reference for event handlers, focus management, etc */}
          <input ref={inputRef} />
        </div>
        <button>Clear</button>
        <span>Chevron icon</span>
      </div>
      {/* floating ref stays as is */}
      <div ref={floatingRef} />
    </>
  );
};
```...

---

## Top Comments

**@kris-ellery**:

Ok, I missed the part in docs that covers it. https://floating-ui.com/docs/react#changing-the-positioning-reference-while-retaining-events