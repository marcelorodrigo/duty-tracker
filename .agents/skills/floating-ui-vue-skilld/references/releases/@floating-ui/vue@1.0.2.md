---
tag: @floating-ui/vue@1.0.2
version: 1.0.2
published: 2023-07-19
---

# @floating-ui/vue@1.0.2

## Bug Fixes

- fix: widen type for `ArrowOptions.element` (#2450)

  Allows `MaybeElement<Element>` instead of `MaybeElement<HTMLElement>` to improve interaction with Vue's Function Refs

- fix: avoid `arrow` re-export collision (#2450)