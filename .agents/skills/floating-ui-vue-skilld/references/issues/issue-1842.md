---
number: 1842
title: "[DOM] Support for :top-layer content, e.g. <dialog> and [popup]"
type: bug
state: closed
created: 2022-08-06
url: "https://github.com/floating-ui/floating-ui/issues/1842"
reactions: 20
comments: 28
labels: "[bug, has workaround]"
---

# [DOM] Support for :top-layer content, e.g. <dialog> and [popup]

Content in a `<dialog>` that is opened with `showModal()` or `showPopUp()` via the `popup` attribute will not actually have "offsetParents" or "clippingAncestors". How do you think it is best for the library to support positioning that content?

Particularly, if a reference element and a floating element are both bound in an element with a matrix transform, a la https://codesandbox.io/s/floating-ui-dom-template-forked-es3dfl?file=/src/index.js, do you foresee adding extra checks for this reality, or surfacing a flag so a use can opt-out of ancestor math or something else.


---

## Top Comments

**@atomiks** [maintainer] (+2):

That looks surprisingly small and simple so it should be straightforward to support in the core

I'm confused why is this line necessary to make it work?

```css
div {
  transform: translateZ(0px);
}
```

**@Westbrook** (+3):

I'm testing out this fairly naive middleware, based on the `offset` middleware, to manage this context:
...

**@atomiks** [maintainer]:

@Westbrook are you up to make a PR with that CodeSandbox you made? Seems good