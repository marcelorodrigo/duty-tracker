---
number: 695
title: RTL support?
type: other
state: closed
created: 2018-10-08
url: "https://github.com/floating-ui/floating-ui/issues/695"
reactions: 11
comments: 6
---

# RTL support?

Does Popper.js have RTL support for `placement` and `offset`? I.e. if a popper was set with `placement="bottom-start"`, so it's placed like this:
```text
+--------+
| target |
+-------------+
|             |
|   popper    |
|             |
+-------------+
```

Would the same popper automatically look like this if displayed on an RTL page?
```text
     +--------+
     | target |
+-------------+
|             |
|   popper    |
|             |
+-------------+
```

---

## Top Comments

**@FezVrasta** [maintainer] (+2):

We may add it into V2, but V1 at the moment is so heavy that any additional feature is out of question 

**@FezVrasta** [maintainer]:

I think this should work

```js
{
  order: 0,
  enable: true,
  fn: data => {
    const hash = { end: 'start', start: 'end' };
    data.placement = placement.replace(/start|end/g, matched => hash[matched]);
	return data;
  }
}
```

**@skitterm** (+5):

Thanks @FezVrasta for the quick help. It would also be awesome if RTL support made it into Popper.js natively. Where support for RTL languages is becoming more important, it'd add another feather in your library's cap to have that  