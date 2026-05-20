---
number: 3329
title: Voice Control numbers not shown on Floating UI Combobox in iOS Safari
type: bug
state: open
created: 2025-05-27
url: "https://github.com/floating-ui/floating-ui/issues/3329"
reactions: 0
comments: 0
labels: "[browser bug]"
---

# Voice Control numbers not shown on Floating UI Combobox in iOS Safari

We encountered an accessibility issue where iOS Voice Control does not show numbers for the combobox dropdown, while all other platforms work perfectly. After investigating, I discovered that adding `display: contents` to the `FloatingPortal` (or `[data-floating-ui-portal] `element) seems to resolve the problem. It’s possible that other users may experience the same issue.


It is possible to reproduce with your own combobox example: https://codesandbox.io/p/sandbox/fragrant-water-bsuirj


Steps to reproduce the behavior:

...