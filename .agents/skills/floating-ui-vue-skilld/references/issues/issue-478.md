---
number: 478
title: How to use Popper.js in Jest / JSDOM?
type: question
state: closed
created: 2017-11-01
url: "https://github.com/floating-ui/floating-ui/issues/478"
reactions: 20
comments: 42
labels: "[# QUESTION , FAQ]"
---

# How to use Popper.js in Jest / JSDOM?



### CodePen demo



https://codepen.io/pen?template=wGqJEz

### Steps to reproduce the problem

1.  Run UI tests with jsdom
2.  Get exception `TypeError: document.createRange is not a function`

### What is the expected behavior?

Not to get an exception

### What went wrong?

Obviously this is not popper's fault, but maybe it can be fixed here.

### Any other comments?

Thanks for the awesome library, I hope this doesn't come across as disrespectful. The jsdom issue gives the feeling that this won't implemented anytime soon. I would like to know more about why `createRange` is used and can we replace it with something else or fall back to another solution if the browser doesn't support it.

Browsers newer than IE8 already support this so I can't find another use case other than testing with `jsdom`.

---

## Top Comments

**@brentertz** (+13):

Thanks @FezVrasta The following works for me.

```js
jest.mock(
  'popper.js',
  () =>
    class Popper {
      static placements = [
        'auto',
        'auto-end',
        'auto-start',
        'bottom',
        'bottom-end',
        'bottom-start',
        'left',
        'left-end',
        'left-start',
        'right',
        'right-end',
        'right-start',
        'top',
        'top-end',
        'top-start'
      ];

      constructor() {
        return {
          destroy: () => {},
          scheduleUpdate: () => {}
        };
      }
    }
);
```...

**@brentertz** (+6):

@frontsideair I am dealing with the exact same thing.  If you come up with a solution, please share, and I'll do the same.

**@FezVrasta** [maintainer]:

I decided to use createRange because it's a convenient way to know the order of two given DOM elements in the DOM.

Previously I used a very complex and unreliable custom code that didn't perform so well.

I may suggest you to simply polyfill `createRange` since anyway Popper.js is not going to actually work inside JSDOM.