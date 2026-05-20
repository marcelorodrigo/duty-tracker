---
number: 617
title: Popper.js v2
type: other
state: closed
created: 2018-04-17
url: "https://github.com/floating-ui/floating-ui/issues/617"
reactions: 23
comments: 111
labels: "[TARGETS: core, PRIORITY: high, breaking change]"
---

# Popper.js v2

If anyone wants to contribute to the Popper.js v2 rewrite please do so sending PRs against the `next` branch.

It's a completely new codebase, I think the current codebase is too messy to continue to work on it, it started as a little proof of concept and ended up being a full fledged library, I think it deserves a complete rewrite.

The idea is to copy the good code from the old version and write new one to make the new code more stable.

I'd like to make the library even more performant by better leveraging to an internal cache the modifiers can use to get most of the values they need that have been already read from the DOM.

The only decisions I made so far are:

- prettier to format the code
- Flow to type check it
- New system to order modifiers [WIP] (discussion)

To do:

- CI
  - [x] add a built step (Rollup + Babel)
  - [x] add a CI setup
  - [x] add unit tests (Jest)
  - [x] add functional/integration cross-browser tests (Karma?)
- Core
  - [x] rewrite the positioning engine to work on all the supported cases (and more?)
  - [x] rewrite the modifiers API
- Modifiers
  - [x] computeStyles
  - [x] applyStyles
  - [x] arrow
  - [x] preventOverflow
  - [x] flip
  - [x] offset/shift (merge them together)
  - [ ] other modifiers will be added later, if anyone wants them please send a PR to implement them
- Miscellaneous
  - [x] Implement the changes labelled as `BREAKING CHANGE`

If you'd like to contribute please write here so others know who's working on what.

---

## Top Comments

**@FezVrasta** [maintainer] (+2):

Just to share a bit of updates on the status of the `next` branch:

Right now there aren't any modifiers included, if you open the `basic.html` example you can see I created a super simple `applyStyle` modifier to showcase the current status (the screenshot above).

We need to add support for all the logic that handles the possible different contexts a popper may live in (scrolling parent, overflow hidden etc).

I really want to reach the point where we'll have a rock solid core positioning logic, independent by any additional modifier. So that we can confidently write additional code (m...

**@FezVrasta** [maintainer] (+4):

Daily update, initial support for nested scroll parents:



**@FezVrasta** [maintainer]:

I don't think it's useful to return `0` in case the browser can't return the correct values. Better make it clear something is wrong and return `undefined` no?