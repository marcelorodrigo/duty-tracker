---
number: 3150
title: "[React 19] cleanup function support in `useMergeRefs` hook"
type: other
state: closed
created: 2024-12-05
url: "https://github.com/floating-ui/floating-ui/issues/3150"
reactions: 4
comments: 0
---

# [React 19] cleanup function support in `useMergeRefs` hook

React 19 is now stable.

One of the changes in react 19 was that ref callbacks can now return a cleanup function[^1]. This does not work with the `useMergeRefs` hook yet.

Here is an example implementation that does support them: https://github.com/vercel/next.js/blob/canary/packages/next/src/client/use-merged-ref.ts

[^1]: see https://react.dev/blog/2024/12/05/react-19#cleanup-functions-for-refs