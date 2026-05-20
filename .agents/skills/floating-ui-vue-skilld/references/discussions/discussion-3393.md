---
number: 3393
title: Making the floating menu match the size of something else than the reference
category: Q&A
created: 2025-09-09
url: "https://github.com/floating-ui/floating-ui/discussions/3393"
upvotes: 1
comments: 0
answered: false
---

# Making the floating menu match the size of something else than the reference

I have a phone number input, with a country select embedded inside of it.

<img width="831" height="718" alt="Screenshot 2025-09-09 at 22 49 41" src="https://github.com/user-attachments/assets/fe054ddf-9aec-4479-8941-eed33dac7d93" />

I would like the select controller button to show/hide the menu upon click, but the menu to match the width of the select.

1. I have tried passing the input the `ref`, and spreading `getReferenceProps()` onto the controller button, and it works great (screenshot) except when I try to click the controller button to close the menu, it closes and opens again.

2. If I pass both the `ref` and `getReferenceProps` to the controller button, then I can close the menu but its size matches the controller button's.

3. If I pass both the `ref` and `getReferen...