---
number: 3251
title: Focused DOM elements that are removed from the DOM seem to break the useFocus interaction
category: Q&A
created: 2025-03-11
url: "https://github.com/floating-ui/floating-ui/discussions/3251"
upvotes: 1
comments: 0
answered: false
---

# Focused DOM elements that are removed from the DOM seem to break the useFocus interaction

Hi there,

I am creating this as a discussion and not as a bug since I am not able to reproduce it outside of my app, but my issue seems very similar to this one: https://github.com/floating-ui/floating-ui/issues/2903, except that it happens when using the useFocus interaction and not in the floating focus manager. I suspect the reason I can't reproduce it outside of my app is that its timing related.

### Description
I have a container div that contains a text input and the floating menu. The container div is the reference element. When the floating menu is open it renders one of our custom components a ListCategoriesCardDeck which renders a number of cards using the renderCard function. The problem is that when I click on the checkbox inside a card it receives the focus but sometime...