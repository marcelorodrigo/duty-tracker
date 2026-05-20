---
number: 1361
title: Recent change to package.json/exports causes an issue in @restart/ui which breaks other downstream projects
type: bug
state: closed
created: 2021-09-02
url: "https://github.com/floating-ui/floating-ui/issues/1361"
reactions: 10
comments: 8
labels: "[bug, PRIORITY: high]"
---

# Recent change to package.json/exports causes an issue in @restart/ui which breaks other downstream projects



the change (https://github.com/popperjs/popper-core/pull/1342) seems to break this usage in some way.

### What is the expected behavior?

The enums should have been found by webpacks with alternate imports.

When using imports like this https://github.com/react-restart/ui/blob/main/src/popper.ts#L9 should work

### What went wrong?

```
ModuleNotFoundError: Module not found: Error: Package path ./lib/enums is not exported from package
.../Library/Caches/Bit/capsules/2b25412ad92805e9573583d4b25e6a7e05d89fd5/node_modules/@restart/ui/node_modules/
@popperjs/core (see exports field in .../Library/Caches/Bit/capsules/2b25412ad92805e9573583d4b25e6a7e05d89fd5/node_modules/@restart/ui/node_modules/
@popperjs/core/package.json)
```

### Any other comments?




---

## Top Comments

**@FezVrasta** [maintainer] (+7):

I released a new version that reverts this, sorry for the trouble

**@crokodz** (+2):

yep same here, need to npm remove @popperjs/core and npm install @popperjs/core@2.9.3 in my pipeline just to get it working

**@evanleck**:

I'm seeing something similar when using esbuild to bundle modifiers directly.

```
 > app/assets/scripts/tooltip.js:2:47: error: Could not resolve "@popperjs/core/lib/modifiers" (mark it as external to exclude it from the bundle)
     2 │ import { arrow, offset, preventOverflow } from '@popperjs/core/lib/modifiers'
       ╵                                                ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
   node_modules/@popperjs/core/package.json:29:13: note: The path "./lib/modifiers" is not exported by package "@popperjs/core"
    29 │   "exports": {
```