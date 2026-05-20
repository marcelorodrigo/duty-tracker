---
number: 674
title: Document minimum CSS needed to style arrows
type: docs
state: closed
created: 2018-08-24
url: "https://github.com/floating-ui/floating-ui/issues/674"
reactions: 29
comments: 11
labels: "[feature, docs]"
---

# Document minimum CSS needed to style arrows

### CodePen demo

### Steps to reproduce the problem

1.  Install Tooltip.js
2.  Look for docs on how to add styled arrows

### What is the expected behavior?

It would help to have some CSS docs linked from either Tooltip.js, or the README, or somewhere, indicating what minimum styling is needed for tooltips with styled arrows.

### What went wrong?

Documentation of this sort does not appear to be available anywhere.

### Any other comments?

:beers:


---

## Top Comments

**@FezVrasta** [maintainer] (+3):

I find it awesome this issue has stayed open for 9 months and nobody sent a PR to add this piece of documentation.

**@kmgdevelopment** (+3):

Agree. Took me a while to realize you have to manually apply `position: absolute` as well as top/left/right/bottom alignment for the arrow positioning to work correctly. 

**@kmgdevelopment** (+7):

For me, the expectation upon installation is for the tooltip styling to be applied out of the box. Basically a default style to get up-and-running with the plugin quickly. So when I went through the installation instructions I was confused why I wasn't seeing an arrow, and there's nowhere in the documentation that explains you have to apply your own CSS rules yourself in order for the arrow to display. 

So if you're not going to apply default styles, it would be helpful to add some content to the docs that explains you need to add your own, and maybe an example CSS code snippet on how to do...