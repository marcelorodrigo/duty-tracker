---
number: 3260
title: Prepare new major versions
type: other
state: closed
created: 2025-03-23
url: "https://github.com/floating-ui/floating-ui/issues/3260"
reactions: 6
comments: 7
labels: "[breaking change]"
---

# Prepare new major versions

## `@floating-ui/vue` v2

Drop Vue 2 support as it reached EoL awhile ago

## `@floating-ui/react` v1

Properly stabilize this package and refine APIs. I've kept it mostly backwards-compatible for 2 years now but it's showing some limitations.

It can be lighter/more tree-shakeable, and the APIs can be less confusing. Remove deprecations and tech debt.

## `@floating-ui/utils` v1

This could be refined, stabilized and properly documented

## `@floating-ui/core,dom,react-dom,react-native`

With CSS anchoring becoming more widespread in the future, it doesn't make sense to change anything/release a new major for these. The APIs have some minor issues, but these can be worked around externally easily (such as supporting logical sides without the confusing `placement` string)

---

## Top Comments

**@atomiks** [maintainer] (+4):

Hey @kiran-bhalerao no timeline atm but before the end of the year, so there's no need to wait. 

I'm trying to keep the changes small (prop/config-related) that will be easy to migrate to. Most of the changes will be to improve default behavior (such as #3138), performance/bundle size, etc, rather than fundamental API changes.

**@atomiks** [maintainer] (+1):

v2 is no longer planned

**@atomiks** [maintainer]:

Base UI uses `@floating-ui/react-dom` directly, but it forked `@floating-ui/react` and they've diverged significantly