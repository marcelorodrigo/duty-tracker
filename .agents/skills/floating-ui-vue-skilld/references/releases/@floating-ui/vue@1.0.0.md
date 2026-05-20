---
tag: @floating-ui/vue@1.0.0
version: 1.0.0
published: 2023-04-30
---

# @floating-ui/vue@1.0.0

## Breaking Changes

- Default `x` and `y` coordinates to `0` instead of `null` (#2300)

  `isPositioned` lets you know if the floating element has been positioned.

## New Features

- feat: `floatingStyles` object (#2300)

  Pre-configured positioning styles for the majority of cases:
  
  ```js
  const {floatingStyles} = useFloating(reference, floating);
  ```
  
  ```vue
  <div ref="floating" :style="floatingStyles" />
  ```