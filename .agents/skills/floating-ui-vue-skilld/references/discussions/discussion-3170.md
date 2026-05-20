---
number: 3170
title: does arrow() in virtual element should pass object insteadof element ?
category: Q&A
created: 2024-12-19
url: "https://github.com/floating-ui/floating-ui/discussions/3170"
upvotes: 1
comments: 2
answered: false
---

# does arrow() in virtual element should pass object insteadof element ?

```jsx
middlware: [arrow({element:{
width:  // 
}})]
```
Is there a demo available to implement arrow in virtual elements?

---

## Top Comments

**@FezVrasta** [maintainer]:

You can use a virtual reference object. Just implement what's needed by the type (getBoundingClientRect and a few more properties)

**@vaynevayne**:

Can you teach me how to do it? I tried svg dom, floating-ui/recact Arrow, but it still failed. I checked the source code of ReactArrow, and it seemed very complicated. @FezVrasta 

...