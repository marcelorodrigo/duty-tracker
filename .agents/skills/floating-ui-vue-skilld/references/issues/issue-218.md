---
number: 218
title: Breaking changes in minor releases
type: other
state: closed
created: 2017-04-18
url: "https://github.com/floating-ui/floating-ui/issues/218"
reactions: 19
comments: 15
---

# Breaking changes in minor releases

Please. Please. PLEASE, don't make breaking changes in minor releases.

You called out that you weren't following semver in the release notes and did it any way.

When you don't follow semver down stream people get affected. Your change completely broke our release pipelines while we tracked down what changed over the weekend and we've now had to pin our versions back whilst we find a work around.

Semver exists for a reason, please follow it properly in the future. :(

---

## Top Comments

**@FezVrasta** [maintainer]:

You should use a lockfile or shrinkwrap for extra safety. I'm sorry it caused problems to you but it was a needed change that had to be made and I didn't want to bump a major version because almost nobody used the feature I removed. 

Sorry again for the problems caused by it, I'll be more careful in future.

**@FezVrasta** [maintainer]:

Default yarn lockfile will actually lock your dependencies to the exact version specified in the lockfile

**@FezVrasta** [maintainer]:

I can assure you that your error isn't related to a breaking change