---
number: 3394
title: "FloatingFocusManager applies :focus-visible pseudo class to reference element when the floating element is closed"
type: bug
state: open
created: 2025-09-15
url: "https://github.com/floating-ui/floating-ui/issues/3394"
reactions: 1
comments: 2
labels: "[bug]"
---

# FloatingFocusManager applies :focus-visible pseudo class to reference element when the floating element is closed

**Describe the bug**

When a Dropdown Menu is closed (for example, after an option is selected via mouse pointer event), the `FloatingFocusManager` returns the focus back to the reference element. This is expected, however, we are finding that in some cases, the `:focus-visible` pseduo-class is being applied to the reference element when the focus is returned. Ideally, the `:focus-visible` pseduo-class should only be applied when the dropdown menu has been closed using a keyboard event!

**To Reproduce**

<!--
Please use one of the following CodeSandboxes to reproduce your issue and make sure not to include any external libraries:

...