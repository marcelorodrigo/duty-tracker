---
number: 2955
title: Incorrect positioning when inside a Web Component slot, with a container that has a backdrop-filter
type: bug
state: closed
created: 2024-06-28
url: "https://github.com/floating-ui/floating-ui/issues/2955"
reactions: 7
comments: 10
labels: "[bug, docs, has workaround]"
---

# Incorrect positioning when inside a Web Component slot, with a container that has a backdrop-filter

**Describe the bug** 

When a tooltip is rendered within the slot of a web component, within a container that has a `backdrop-filter`, the tooltip ends up being positioned incorrectly. I've observed quite a variance in just _how far_ off the tooltip is, from just a few pixels to being completely off the page - depending on the rest of the context, but I've provided a simple reproduction below

**To Reproduce**

Here is a codesandbox that shows the issue (has a dependency on Lit): https://codesandbox.io/p/sandbox/floating-ui-dom-template-forked-xjn3yc?file=%2Fsrc%2Findex.js%3A8%2C16

**Screenshots** 

### Incorrectly positioned:



### Correctly positioned:








**Context:**

- OS: [e.g. iOS]: MacOS Sonoma 14.5
- Browser [e.g. chrome, safari]: Chrome and Firefox. ~Safari does not seem to be affected~. Safari is affected as well, when using `-webkit-backdrop-filter`
- Version [e.g. 22]: Chrome Version 126.0.6478.127 (Official Build) (arm64), Firefox 127.0.2 (64-bit)



---

## Top Comments

**@atomiks** [maintainer] (+1):

Containing block detection is already handled robustly by `isContainingBlock`, but @jcfranco said some tests failed when changing to use that. 

Did you keep the `!== 'static'` position check? I don't see why this would cause failures:

```js
if (style.position !== 'static' || isContainingBlock(ancestor)) {
  return ancestor;
}
```

@danwenzel it's labeled with `has workaround` because the `getOffsetParent` platform method has a `polyfill` hook that allows you...

**@atomiks** [maintainer] (+1):

I believe it's impossible to traverse through closed shadow roots (feel free to correct) so there's no method that will work to find the correct offset parent. If that's the case, the only workaround is to manually supply the offset parent via that platform method hook, by returning the `backdrop-filter` element directly, since it can't be detected automatically

**@atomiks** [maintainer]:

I think this issue may be the Shadow DOM bug.

It doesn't seem like the `composed-offset-position` (`offsetParent`) polyfill works in this case as it doesn't detect containing blocks correctly

cc: @jcfranco 