---
number: 3176
title: How to use topLayer with @floating-ui/vue
category: Q&A
created: 2024-12-27
url: "https://github.com/floating-ui/floating-ui/discussions/3176"
upvotes: 1
comments: 1
answered: false
---

# How to use topLayer with @floating-ui/vue

I'm using @floating-ui/vue (1.1.5) to create a tooltip component.

I'm using it with a button within a native dialog element, and the tooltip is shown behind the dialog. I've tried to use the platform object as I've seen in this PR https://github.com/floating-ui/floating-ui/pull/2747

However the vue composable does not have the `platform` attribute.

Do you know how I could solve this?

Thanks!

---

## Top Comments

**@atomiks** [maintainer]:

The tooltip either also needs to be a top-layer element, or be mounted inside the dialog element itself so it's also top-layer.