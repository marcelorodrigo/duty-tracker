---
number: 2904
title: "[React-native] Android does not adjust based on reference's y-cordinate"
type: bug
state: open
created: 2024-05-16
url: "https://github.com/floating-ui/floating-ui/issues/2904"
reactions: 2
comments: 6
labels: "[browser bug, has workaround]"
---

# [React-native] Android does not adjust based on reference's y-cordinate

**Describe the bug**
With react-native android, floating element does not correctly adjust for height of views before the reference view. Even with `collapsable={false}`, as pointed out in the docs.

**To Reproduce**

https://snack.expo.dev/@mookiies/floating-scollview Snack takes basic example from docs, and adds a 100 tall view as first child.

Compare Android vs iOS/web behavior

**Expected behavior** 
Behavior matches on all platforms, and Android floating view is updated to adjust based on reference's layout height. 

<img width="200" alt="Screenshot 2024-05-16 at 11 41 13 AM" src="https://github.com/floating-ui/floating-ui/assets/22333355/246771e7-7444-4f73-ba10-7a09c524713e">
<img width="200" alt="Screenshot 2024-05-16 at 11 40 53 AM" src="https://github.com/floating-ui...