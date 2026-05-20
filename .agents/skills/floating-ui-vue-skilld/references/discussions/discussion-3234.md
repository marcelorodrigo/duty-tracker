---
number: 3234
title: Too Many Poppers Slowing Down Browser (Angular) - how to resolve?
category: Q&A
created: 2025-02-25
url: "https://github.com/floating-ui/floating-ui/discussions/3234"
upvotes: 1
comments: 2
answered: false
---

# Too Many Poppers Slowing Down Browser (Angular) - how to resolve?

I have a container component with overflow: auto on it and inside of that is my child component, which acts like a dropdown and the list (which is display:none) that appears is my popper. When I have a few on the page, there is no issue, but however when there's a significant amount, when the user scrolls the container, the container slows down to an unusable crawl.

If I deliberate disable the ability to listen to events by adding this to my modifiers on the popper:

`{
     name: 'eventListeners',
     options: {
        scroll: false,
	resize: false
     }
}`

The slowdown goes away completely. However, a @HostListener("window:scroll") [or document:scroll] doesn't replace that missing eventlistener. How are the poppers able to read the scroll of the container even though it ...

---

## Top Comments

**@atomiks** [maintainer]:

Make sure that the popper instance is destroyed or the listeners are inactive while they are not open/mounted to the DOM/screen.

**@propkitty**:

How do you turn the listeners inactive in order to do this? I can't seem to
find anything in the documentation that says how to change modifiers after
createPopper(). Thank you!

On Tue, Feb 25, 2025 at 9:05 PM atomiks ***@***.***> wrote:

> Make sure that the popper instance is destroyed or the listeners are
> inactive while they are not open/mounted to the DOM/screen.
>
> —
> Reply to this email directly, view it on GitHub
> <https://github.com/floating-ui/floating-ui/discussions/3234#discussioncomment-12319936>,
> or unsubscribe
> <https://github.com/notifications/unsubscribe-a...