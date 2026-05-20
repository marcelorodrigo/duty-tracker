---
number: 300
title: "Add \"click outside\" option to hide tooltip"
type: feature
state: closed
created: 2017-06-06
url: "https://github.com/floating-ui/floating-ui/issues/300"
reactions: 13
comments: 36
labels: "[feature, PRIORITY: low]"
---

# Add "click outside" option to hide tooltip

### Suggestion: 

I've made a plugin for Tether (back in the day) for tooltips and I pass an option (Boolean) 
for tooltips (only ones which are opening on "click/tap/manual" event) and it listens
to clicking outside of the tooltip area and then closes them.

I guess it will look like this:

````
var tooltip = new Tooltip(referenceElement, {
    title: "Hey there",
    trigger: "click",
    options: {
       clickOutside : true
   }
});
````

Here's a snippet of my code which will give some idea to what I did (modified for simplicity purposes):

````
if( checkClickOutside ){
    timer = setTimeout(function(){
        // bind a "click outside" event on the document itself
        // (this event MUST be removed when the tooltip is
        // removed to prevent it from being created again and
        // not to pollute the document with events)
        $(document.body).off('click.tooltip_outside')
                        .on('click.tooltip_outside', onClickOutside.bind(that));
    }, 50);
}

function onClickOutside(e){
    if( !$(e.target).closest('.ttip').length ){
        $(document).trigger('clickOutside', [e.target]);
        $(document.body).off('click.tooltip_outside');
        this.hide();
    }
}
````

I've been using Tether for YEARS and only today I've found out about your lib. great work!
I was always sad how Tether got so shitty and abandoned... this seems so much better in terms of code.
Tether's source was so horrible..OMG.. super bad.

---------

BTW - would be nice if the tooltip example page would show an example where some options are passed

---

## Top Comments

**@moyarich** (+3):

This is how it is done for bootstrap popovers:
http://jsfiddle.net/moyarich/adzsp0L6/

```javascript
$(function () {
    var element = '.example';
    $(element).popover({
    });

    $('body').on('click', function (e) {
				
        //Use each to hide Popovers with the same class
         $(element).each(function(index, elm) {
    				hidePopover(elm, e);
        }); 
    });

     // hide any open popovers when anywhere else in the body is clicked
    var hidePopover = function(element, e){
      if (!$(element).is(e.target) && $(element).has(e.target).length === 0 && $('.popover').has(e.target).length === 0){
        $(element).popover('hide');
      }
    }
});
```...

**@FezVrasta** [maintainer]:

`.dipose()`

**@yairEO** (+4):

I don't use bootstrap.. so I don't know

Even if they don't have this option, I think this is a must-have feature because it's very common UX request to have a tooltip disappear when clicking outside and if it's not included in bootstrap, it shouldn't stop you from adding a few extra stuff, if they are important enough, no?

wouldn't you say this is an important feature?
