---
number: 3407
title: `useDismiss` still closes floating elements when clicking content scrollbars (incomplete fix from #1820)
type: bug
state: open
created: 2025-10-16
url: "https://github.com/floating-ui/floating-ui/issues/3407"
reactions: 0
comments: 0
labels: "[bug, has workaround]"
---

# `useDismiss` still closes floating elements when clicking content scrollbars (incomplete fix from #1820)

## Bug Description

PR #1820 fixed scrollbar detection for page-level scrollbars, but content scrollbars within floating elements still incorrectly trigger `outsidePress` dismissal. This creates inconsistent behavior where some scrollbars work (page-level) while others don't (content-level).

## Reproduction

1. Create floating element with scrollable content (`overflow: auto`, content exceeds container height)
2. Click floating element's scrollbar to scroll
3. **Bug:** Element dismisses instead of scrolling

## Root Cause

Current fix only handles document scrollbars, not scrollbars within floating element content. Need to detect:
- Traditional scrollbars (take layout space)
- Overlay scrollbars (macOS style, no layout space)

## Proposed Solution

...