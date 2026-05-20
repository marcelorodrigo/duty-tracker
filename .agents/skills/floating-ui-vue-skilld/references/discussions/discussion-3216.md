---
number: 3216
title: `inline` positioning off when selection range clicked
category: Q&A
created: 2025-02-04
url: "https://github.com/floating-ui/floating-ui/discussions/3216"
upvotes: 2
comments: 2
answered: false
---

# `inline` positioning off when selection range clicked

I've been getting into floating lately its been great. Thanks for an awesome library!

I'm now trying to build a floating text editor menu using the `inline` middleware. I'm running into a weird issue: when _drag selecting_ the float is positioned as expected. However when _click selecting_ its not placed `offset` to the top `placement` as expected, but rather to the bottom right of the range's center:

Screencast From 2025-02-04 10-44-52.webm

I am using an event handler to track `selectionchange` so it may have to do with that. I work with Svelte and I wrote a minimal reproduction REPL for your convenience. I commented t...

---

## Top Comments

**@jayclees**:

@ixxie I fixed this by not toggling `display: none` on the toolbar when making a selection. Instead I just move the toolbar way off screen when not being displayed and reset the position in  `computePosition().then(...)` callback.

**@jayclees**:

Getting same issue. I can't figure out what the pattern is either, it's very inconsistent.

@ixxie did you ever figure this out?