---
number: 3067
title: "Positioning inside container-type: inline-size is off when using position: fixed on the popup"
type: bug
state: open
created: 2024-10-03
url: "https://github.com/floating-ui/floating-ui/issues/3067"
reactions: 9
comments: 9
labels: "[browser bug, has workaround]"
---

# Positioning inside container-type: inline-size is off when using position: fixed on the popup

**Describe the bug**

When using the `computePosition()` function in the following context the popup is placed off
(see also https://codesandbox.io/p/sandbox/funny-glade-jfr7kl):

- The button and popup element are located inside a div somewhere in the DOM tree with the CSS property: `container-type: inline-size`
- Using strategy `fixed`
- The popup element has CSS property `position: fixed`. This is because we want the popup to be visible and overflowing even in a small side container for example.

**To Reproduce**

See code sandbox: https://codesandbox.io/p/sandbox/funny-glade-jfr7kl

**Expected behavior**

The popup should be placed below the button.

**Screenshots**



**Additional context**

I have debugged the code and found the following code to be executed which might be 'a problem' but I also might be doing something weird. (Found in dom/src/utils/getRectRelativeToOffsetParent.ts)

https://github.com/floating-ui/floating-ui/blob/master/packages/dom/src/utils/getRectRelativeToOffsetParent.ts#L23
```ts
/**
 * Here we get the coordinator for the element and this actually already returns
 * the correct x and y coordinates for the popup placement.
 */
const rect = getBoundingClientRect(element, true, isFixed, offsetParent);
```

https://github.com/floating-ui/floating-ui/blob/master/packages/dom/src/utils/getRectRelativeToOffsetParent.ts#L36
...

---

## Top Comments

**@atomiks** [maintainer] (+1):

I'm kind of leaning toward leaving it stay broken, and even removing the check for Firefox and Safari too so it's consistently broken across all browsers. This might sound weird, but there are two reasons: 

1. Determining that >=129 version of Chromium is being used (including all browsers based on it, like Edge) might be hard. If Chromium forces the same versioning with the `userAgentData API` or `navigator.userAgent` then this point is not really valid though.

2. But consider Firefox and Safari. One day randoml...

**@IvandeWolf** (+3):

Alright, after reading the blog it comes down to this:

Prior to chrome 129, `container-type: inline-size` creates an implicit containing block for absolutely positioned children. So if you tell a child to place it `left: 0` it will be against the border of the container.

Apparently according to the Chromium team this is a design mistake so they 'fixed' it. But all other major browsers still follow the pre-Chrome 129 behavior.

### Workaround
What works for me is the to add `contain: layout;` to the container div to explicitly add a containing block.
I also updated the CodeSandbox to ...

**@atomiks** [maintainer]:

The `container-type` property creates a "containing block", at least in Chrome 128 and below. Either Chrome 129 has a regression for this, or the spec changed and it was updated. I'm not sure where to look to see if that's the case. On MDN, `container-type` was added last year but remains to this day.

Either way, it's going to be a pain to handle browser versions to get it to work reliably. Issues with containing blocks causing positioning issues comes up frequently because browsers randomly change how a block...