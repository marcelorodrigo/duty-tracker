---
number: 3379
title: Floating UI in older browsers
category: Q&A
created: 2025-08-06
url: "https://github.com/floating-ui/floating-ui/discussions/3379"
upvotes: 1
comments: 1
answered: false
---

# Floating UI in older browsers

Hi,

thanks for all the wonderful floating libraries 

We develop an offline-first edtech platform that runs on older devices and is deployed in remote locations, so we need to maintain quite old browsers in our browserlist.

For a number of reasons, there's a tangible need to upgrade our popover-like elements. I really hope we could start using vanilla `@floating-ui/dom` - seems to be perfect for our use-cases.

Our frontend is Vue 2 and we polyfill with `corejs`. To get at least some initial understanding, I transpiled a file with few random imports and functions from Floating UI with Babel on our browserlist, and the polyfills that were applied look fine to us. I also tr...

---

## Top Comments

**@FezVrasta** [maintainer]:

Hi, unfortunately even if you managed to make the code run it would not behave correctly on old browsers.

I think your best bet is to use older popper.js releases that still supported IE