---
number: 690
title: Option to set a custom class name for poppers
type: feature
state: closed
created: 2018-09-25
url: "https://github.com/floating-ui/floating-ui/issues/690"
reactions: 11
comments: 2
labels: "[feature, PRIORITY: low]"
---

# Option to set a custom class name for poppers

It would be tremendously useful, as a way to style different poppers differently, to be able to specify an additional class for the popper markup. Right now, it's possible by specifying the `template` option but it's needlessly verbose, requiring the entire template to be specified again simply to add a class.

I'm happy to provide a PR with this feature if it is deemed that it should be part of the library.

---

## Top Comments

**@FezVrasta** [maintainer]:

Hi, I'm interrupting the development of the `tooltip.js` library to focus on the development of `@popperjs/core` (Popper.js v2).

Please consider switching to Tippy.js, which is a way better alternative to Tooltip.js and is well maintained. Tippy.js still uses Popper.js under the hood, so you will find all your favourite features.

I'll close this issue.