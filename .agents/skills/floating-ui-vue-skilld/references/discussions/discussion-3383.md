---
number: 3383
title: What are the `onOpenChange` trigger conditions when using `ArrowRight` and `ArrowLeft` keys on nested floating lists?
category: Q&A
created: 2025-08-13
url: "https://github.com/floating-ui/floating-ui/discussions/3383"
upvotes: 1
comments: 1
answered: true
---

# What are the `onOpenChange` trigger conditions when using `ArrowRight` and `ArrowLeft` keys on nested floating lists?

Hello!

I'm implementing a menu with nested submenus, similar to the `<DropdownMenu>` example.

I don't use the exact same setup, and it's been a very difficult implementation so far, but I managed to get something working — except for one thing. I can't get the `ArrowRight` key to trigger an `onOpenChange` event. The `ArrowLeft` key works fine though, and I successfully get a "list-navigation" `reason` when I use it, but the `ArrowRight` key is just ignored.

I tried tweaking the `<DropdownMenu>` example, but I can't understand what the conditions are for the `ArrowRight` key to work. I guess it's just handled by the `useListNavigation` + tree structure, but what could be the other factors allowing the key to be handled?
...

---

## Accepted Answer

Alright, I think I found the problem.
It turns out that for everything to work correctly, click or hover interactions need to be enabled.
I was controlling the open/close state with regular click and hover events, so I had disabled them.