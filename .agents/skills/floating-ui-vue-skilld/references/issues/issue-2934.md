---
number: 2934
title: "incorrect positioning with fixed ancestors + shadow DOM + parent overflow: hidden + `shift`/`autoPlacement` modifiers"
type: bug
state: open
created: 2024-06-14
url: "https://github.com/floating-ui/floating-ui/issues/2934"
reactions: 2
comments: 1
labels: "[bug, has workaround]"
---

# incorrect positioning with fixed ancestors + shadow DOM + parent overflow: hidden + `shift`/`autoPlacement` modifiers

**Describe the bug**

This is an isolated repro case for a positioning issue that stemmed from testing https://github.com/floating-ui/floating-ui/pull/2291#issuecomment-1513729875.

**To Reproduce**



Steps to reproduce the behavior:

1. go to https://codepen.io/jcfranco/pen/LYoOJGe?editors=1010
2. hover over tooltip buttons (`hover over me`)
3. notice placement is incorrect for the `overflow: hidden` + `fixed` strategy use case
4. by contrast, other cases using...