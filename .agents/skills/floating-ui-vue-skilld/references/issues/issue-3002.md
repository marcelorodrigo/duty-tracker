---
number: 3002
title: "[React-Native Android] : On switching between two spotlight targets, the floating dialog is initially rendered in the position of previous target"
type: other
state: open
created: 2024-08-13
url: "https://github.com/floating-ui/floating-ui/issues/3002"
reactions: 1
comments: 2
labels: "[has workaround]"
---

# [React-Native Android] : On switching between two spotlight targets, the floating dialog is initially rendered in the position of previous target

**Describe the bug** In Android, On switching between two spotlight targets, the floating dialog is initially rendered in the position of the previous target. 

**To Reproduce**

- @floating-ui/react-native: https://codesandbox.io/s/floating-ui-dom-template-utpx0u

Steps to reproduce the behavior:

1. Select the first target icon.
2. Select the second target icon.
3. The floating styles remain the same as before for three cycles, and then they re-render with the actual target of the second icon.

**Expected behavior** The floating dialog is rendered properly in the first render itself.

**Screenshots** 

https://github.com/user-attachments/assets/6a9224a9-2e42-469d-bd42-4d1deaadece0



**Context:*...