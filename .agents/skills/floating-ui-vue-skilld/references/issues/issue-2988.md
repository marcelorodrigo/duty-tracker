---
number: 2988
title: React FloatingFocusManager does not perserve tab order with FloatingPortal
type: other
state: open
created: 2024-07-23
url: "https://github.com/floating-ui/floating-ui/issues/2988"
reactions: 2
comments: 4
labels: "[has workaround]"
---

# React FloatingFocusManager does not perserve tab order with FloatingPortal

**Describe the bug** 
Two bugs actually:

**1. With `guards={true}` Focus gets trapped and cycles between the Floating Element buttons and the last focusable element on the page. It feels odd, but maybe it's expected behavior.**

https://github.com/user-attachments/assets/cd384f6e-4978-4f09-93a6-2bb8fac19064


**2. With `guards={false}` FloatingPortal's preserveTabOrder does not appear to be honored**

https://github.com/user-attachments/assets/1726747c-fa3c-4c5a-a47d-fa0de887b301



Steps to reproduce the behavior:
Broken in 0.26.0
https://codesandbox.io/p/sandbox/broken-new-version-dnl2hw?file=%2Fsrc%2FApp.tsx%3A49%2C26

Working in 0.23.0
Using 0.23.0 cause it's in the Dialog example and just happened to be working.
Note: This does seem to also break when `guards={fal...