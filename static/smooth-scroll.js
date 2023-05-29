
// SMOOTH AUTO SCROLL
//  cf  https://www.youtube.com/watch?v=b0tnynJtm18

(function () {

    var autoScroll = {}
    var scrollY = 0
    var distance = 50
    var speed = 1
    var lastPos = -1

    function within (x, y) {
       return (Math.abs(x - y) < distance);
    }
    
    function scrollTo(el, off){
	var scrollPos = window.pageYOffset
	var targetPos = document.getElementById(el).offsetTop + off
	if(scrollPos < targetPos){
            scrollDown(el, off)
	}
        else {
            scrollUp(el, off)
	}
    }
    
    function scrollDown(el, off){
	var currentY = window.pageYOffset
	var targetY = document.getElementById(el).offsetTop + off
	var animator = setTimeout(function() {
            scrollDown(el, off)
        }, speed)
	if (!within(currentY,targetY) && lastPos != currentY) {
	    lastPos = currentY
            scrollY = currentY + distance
            window.scroll(0, scrollY)
	}
        else {
	    lastPos = -1
            clearTimeout(animator)
            window.scroll(0, targetY)
	}  
    }
    
    function scrollUp(el, off){
	var currentY = window.pageYOffset
	var targetY = document.getElementById(el).offsetTop + off
	var animator = setTimeout(function() {
            scrollUp(el, off)
        }, speed)
	if (!within(currentY, targetY) && lastPos != currentY) {
	    lastPos = currentY
            scrollY = currentY - distance
            window.scroll(0, scrollY)
	}
        else {
	    lastPos = -1
            clearTimeout(animator)
            window.scroll(0, targetY)
	}  
    }
    
    autoScroll.scrollTo = scrollTo;
    autoScroll.scrollUp = scrollUp;
    autoScroll.scrollDown = scrollDown;

    // automatically setup by connecting to all 'nav .smooth-scroll' elements
    // reading from href 
    window.addEventListener("load", function() {
        // offset is given by the position of the 'main' tag
        var off = document.querySelector("main").offsetTop
        var elts = document.querySelectorAll("nav .smooth-scroll")
        for (var i = 0; i < elts.length; i++) {
            var href = elts[i].getAttribute("href").slice(1)
            elts[i].onclick = (function(href) {
                return function() {
                    scrollTo(href, -off)
                    return false;
                }
            })(href)
        }
    })

    this.smoothScroll = autoScroll;
})()
