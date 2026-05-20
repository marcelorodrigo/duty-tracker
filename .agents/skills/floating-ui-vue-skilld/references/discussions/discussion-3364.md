---
number: 3364
title: aXe complains about focus guards
category: Q&A
created: 2025-07-17
url: "https://github.com/floating-ui/floating-ui/discussions/3364"
upvotes: 1
comments: 1
answered: false
---

# aXe complains about focus guards

aXe complains about the focus guards having `tabIndex=0` while being `aria-hidden=true`:

<img width="761" height="331" alt="Screenshot 2025-07-17 at 10 27 37" src="https://github.com/user-attachments/assets/7f20d2f0-d9ed-4a9c-8874-8a6e8b1e0090" />

Because of the new European Accessibility Act, we have to comply with WCAG and would like to fix that aXe complains about this. Do you have suggestions for a workaround? Ideally I just want to remove the `aria-hidden=true` from the element since it does not make a difference for screenreaders anyways.

---

## Top Comments

**@mihkeleidast**:

See:
- https://github.com/floating-ui/floating-ui/pull/2298
- https://github.com/floating-ui/floating-ui/issues/2462
- https://github.com/floating-ui/floating-ui/issues/2758
- https://github.com/floating-ui/floating-ui/issues/2823

This is the issue with automatic checkers, they do not understand the background on what the focus guards do.