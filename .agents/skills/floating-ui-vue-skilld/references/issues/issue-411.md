---
number: 411
title: Do you want a lighter Popper.js version?
type: question
state: closed
created: 2017-08-31
url: "https://github.com/floating-ui/floating-ui/issues/411"
reactions: 34
comments: 4
labels: "[# QUESTION , # META ]"
---

# Do you want a lighter Popper.js version?

So, I want to understand if you guys are okay with the current library size (~7KB) or you want a lighter version.

The idea would be to release a popper.slim.js or something similar, with less built-in modifiers (so, less features) or with alternative modifiers that do less stuff.

-  if you'd like a lighter Popper.js
-  if you are fine with the current size or you prefer me to focus on other priorities first (bugs, features)

---

## Top Comments

**@FezVrasta** [maintainer]:

Hi @mechanicious, I already tried to cover all the edge cases I could think of.

If you find uncovered cases feel free to open a issue or directly send a PR

**@FezVrasta** [maintainer]:

I just found out that enabling an option in the minifier I managed to reduce the minified size by 3KB and the compressed size by .3KB (out of the initial 6.3).

A little win at least 

**@mechanicious**:

> ...focus on other priorities first (bugs, features)

I would suggest to first increase coverage for edge-cases etc.