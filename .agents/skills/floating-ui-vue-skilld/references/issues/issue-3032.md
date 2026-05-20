---
number: 3032
title: Incorrect positioning when using CSS `zoom` property
type: bug
state: open
created: 2024-09-03
url: "https://github.com/floating-ui/floating-ui/issues/3032"
reactions: 9
comments: 11
labels: "[bug, has workaround]"
---

# Incorrect positioning when using CSS `zoom` property

Hello,

if the CSS-zoom is set on the body-element the location of the popup is off.

You can reproduce it with the DropdownMenu-example on the Floating-UI homepage (https://codesandbox.io/p/sandbox/admiring-lamport-5wt3yg)

Just set the CSS-zoom on the body like this:

```body.style.zoom = '1.5'```

And open the popup afterwards. The result looks like this:



Tested with Chrome Version 128.0.6613.113 (Official Build) (64-bit)

---

## Top Comments

**@atomiks** [maintainer] (+2):

Passing in the `body` to `getScale` works only if the window hasn't been scrolled, so it doesn't work that simply unfortunately. 

But it looks like `position: relative` on the zoomed element (in this case, `body`) works in general

**@atomiks** [maintainer]:

The currently supported CSS property is `transform: scale(...)`. It doesn't act identically to `zoom` (which affects layout, while `transform` doesn't), but in many cases it could be sufficient.

**@atomiks** [maintainer]:

>  I explored how the product would react to increasing the zoom from 1 to 1.15, and as you might guess, I encountered issues with positioning my tooltips and popovers as well... :(

This issue is only relevant for the **CSS `zoom` _property_**. If you zoom your browser via <kbd>cmd</kbd> or <kbd>ctrl</kbd> plus or minus (+/-), or via pinch-zooming, this issue doesn't exist (that has never been a problem). It's only this previously-deprecated `zoom` property that is an issue, which wasn't exactly common given `transform: scale()` existed, and Firefox had no support for it.

> I wanted to a...