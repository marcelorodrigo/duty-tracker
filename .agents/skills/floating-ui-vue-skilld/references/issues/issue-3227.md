---
number: 3227
title: Keyboard navigation in Select / Virtual Select
type: docs
state: open
created: 2025-02-16
url: "https://github.com/floating-ui/floating-ui/issues/3227"
reactions: 0
comments: 0
labels: "[docs]"
---

# Keyboard navigation in Select / Virtual Select

Keyboard navigation seems to not work as expected when compared to a native select. The following 3 problems occur:

### Problem A
When scrolling down manually, then hovering over an item, using the keyboard up / down arrows will not cycle through the tems. Instead it will just move the scrollbar.

**Steps to reproduce the behavior:**  
1. Go to  Virtualized Select 
2. Open the select.
3. Move the scrollbar to roughly 50%.
4. Hover over an item in the last.
5. Use the arrow up or down key.

**Expected behavior**
When hovering over an item after scrolling down, I expect using the arrow/up keys to cycle through the options rather than just moving the scrollbar.

...