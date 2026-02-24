
---

title: Canvas-Based Web Applications
date: 2024-05-25
reading: On Writers and Writing (by John Gardner)

---

I just finished giving a course on [web development](https://rpucella.net/courses/webdev-sp24) this
spring and one topic I never got to was canvas-based web applications. We just ran out of
time. It's a bit of shame because one of my current projects may well benefit from a canvas-based UI,
and teaching the topic would have been a great forcing function for me to understand the subtleties involved. 

To give some perhaps well-known background, the HTML [`canvas`
element](https://developer.mozilla.org/en-US/docs/Web/HTML/Element/canvas) is an element into which
you can draw directly using a Javascript programmatic interface, the [Canvas
API](https://developer.mozilla.org/en-US/docs/Web/API/Canvas_API). It is direct (the term *immediate* is also used) in the sense that it does not rely on the DOM for rendering. If you issue an instruction to draw a line or a rectangle to a canvas element, the line or rectangle gets drawn immediately.  Here's an example:

    <canvas width="100" height="100">
    </canvas>
    
    <script>
      const canvas = document.querySelector("canvas")
      const ctx = canvas.getContext("2d")
      ctx.fillStyle = "red"
      ctx.fillRect(20, 20, 60, 60)
      ctx.fillStyle = "black"
      ctx.beginPath()
      ctx.arc(50, 50, 20, 0, 2 * Math.PI)
      ctx.fill()
    </script>

<img src="./rect-circle.png" width="100%">

To draw to a `canvas` element, you first get a *context* to draw into that element (think of it as a
*way* to draw into the element), and then you issue drawing instructions which take effect
immediately. While modern versions of canvas support a number of contexts such as
[WebGL](https://developer.mozilla.org/en-US/docs/Web/API/WebGL_API), I'll focus on the default 2D
drawing context supported by all browsers.

Canvas tends to be used whenever what needs to be shown in the browser is not easily achievable
visually using straight HTML elements, or to support applications such as games where having a way
to draw directly to the screen is important for efficiency. I won't have much to say about games
here, even though they are arguably the most common use of canvas these days.

An example of the kind of application that benefits from canvas over straight HTML elements is
[Google
Docs](https://workspaceupdates.googleblog.com/2021/05/Google-Docs-Canvas-Based-Rendering-Update.html),
which basically exposes a large canvas element into which the document being edited is "drawn" and
with which the user interacts. One downside of canvas is that you cannot take advantage of the
browser's HTML rendering engine, and instead have to do all rendering manually. There are libraries
to help systematize the drawing of complex objects and controls, such as 
[EaselJS](https://createjs.com/easeljs).

Another application using canvas is [Figma](https://www.figma.com/). Figma is a design tool for
create user interfaces. At its core, it exposes a canvas element where the user creates their design
by adding and manipulating shapes and text and other visual components. One thing that makes Figma
interesting from my perspective is that the design area is basically an infinite virtual "sheet":
the canvas element shows only a view into the whole virtual sheet, and you can zoom in and out and
move around that virtual sheet at will, adding visual components wherever you wish.

That's what I want to explore here: how to support an infinite virtual sheet that can hold objects
at arbitrary locations, and the ability to move around and zoom in and out across that virtual
sheet though a canvas element. It turns out to be pretty simple, and really an exercise in coordinate system
transformations.

We'll keep things simple. The virtual sheet will hold small red blocks, and we will implement a
basic interface where you drop a new block on the virtual sheet by clicking a location, with
navigation buttons for manipulating the portion of the sheet we see in the canvas element. We'll
implement it in raw Javascript. to show that no framework tricks are required. Moving this code to a
framework like React is an easy exercise. Wrapping it into a [Web
Component](https://developer.mozilla.org/en-US/docs/Web/API/Web_components) is perhaps a bit more
interesting, and I may come back to that in a future post.

Let's start with the UI:

<img src="./sample-ui.png" width="100%">
    
    <body>
  
      <p>Click on the canvas to create a red rectangle. 
         Use buttons to move around the canvas and zoom in and out.</p>
  
      <canvas id="canvas">
      </canvas>
  
      <div>
        <button id="btn-up">Up</button>
        <button id="btn-down">Down</button>
        <button id="btn-left">Left</button>
        <button id="btn-right">Right</button>
        <button id="btn-in">In</button>
        <button id="btn-out">Out</button>
        <span id="pos-x">X 0</span>
        <span id="pos-y">Y 0</span>
        <span id="zoom">Zoom 100%</span>
      </div>
  
    </body>
    
Completely straightforward: a single canvas element that will show part of the underlying virtual
sheet, and six buttons to navigate the virtual sheet by moving it up, down, left, and right, or
zooming in and out. I've kept the styling to a minimum, where the only thing
of interest that I've left out above is the canvas size:

    canvas {
      border: 2px solid #333333;
      margin: 8px;
      width: 80vw;
      height: 80vh;
    }

The code itself conceptually splits in three parts: the data structure for the content of the
virtual sheet, the code to render that content on the canvas, and the various handlers associated
with the buttons to update the view into the virtual sheet. Let's look at them in order.

The data structure to store the blocks on the virtual sheet is dead simple: it's a list of objects,
each object representing a red block and recording the position of the center of the block on the
virtual sheet. I assume a coordinate system on the virtual sheet which I will call the *virtual
coordinate system*. Virtual coordinates describe the position of a block within the sheet.

The task of taking what's on the virtual sheet and exposing a portion of it on the canvas is through
a function `render`. It takes the content of the virtual sheet as well a *view object* that
describes the virtual coordinates of the upper left corner of the canvas (fields `startx` and
`starty`) as well as the zoom factor (field `zoom`), where a zoom factor of *N* means that 1 unit of
virtual coordinates takes *N* units on the canvas.

In order to render an area of the virtual sheet into the canvas, we need to figure out where every block on the
virtual sheet appears on the canvas using information stored in the view. To do this, we need to
convert a virtual position *(vx, vy)* into a *canvas position* *(cx, cy)* on the canvas, where canvas
coordinates *(0, 0)* is the upper left corner of the canvas and *(w, h)* is the lower right corner
of the canvas, with *w* and *h* the dimensions of the canvas on the browser screen.

A bit of math tells us that if the virtual coordinate of the upper left corner of the canvas is
*(vx0, vy0)* then virtual point *(vx, vy)* is at canvas position *(z (vx - vx0), z (vy - vy0))*
where *z* is the zoom factor. In code:

    function toCanvas(view, {x, y}) {
      // Convert a virtual x, y to physical position within the canvas.
      const nX = (x - view.startx) * view.zoom
      const nY = (y - view.starty) * view.zoom
      return {x: nX, y: nY}
    }

It will be useful later to transform canvas coordinates back to virtual coordinates as well, which
is simply the reverse mapping, where *(cx, cy)* map to virtual point  *((cx / z) + vx0, (cy / z) + vy0)*:

    function fromCanvas(view, {x, y}) {
      // Convert an x, y WITHIN the canvas to virtual coordinates.
      const nX = (x / view.zoom) + view.startx
      const nY = (y / view.zoom) + view.starty
      return {x: nX, y: nY}
    }

Equipped with these functions, we can easily write a `render` function to draw every block in the
list of blocks onto the canvas — first clear the canvas, then loop over the blocks, drawing
each on the canvas given the virtual coordinate at the center of the canvas and the zoom factor:

    function render(content, view) {
      const canvas = document.getElementById('canvas')
      const ctx = canvas.getContext('2d')
      ctx.clearRect(0, 0, canvas.width, canvas.height)
      const size = 30 * view.zoom
      ctx.fillStyle = 'red'
      for (const r of content) {
        p = toCanvas(view, r)
        ctx.fillRect(p.x - size / 2, p.y - size / 2, size, size)
      }
    }

Each block has virtual width 20. Note that this was a feature of the canvas element that
anything drawn outside of the area of the canvas (any canvas coordinate less than *(0, 0)* or more
than *(w, h)*) is not visible. When we have too many objects, we may benefit from first determining if
an object is visible before drawing it, but for the purpose of this simple experiment, this is good
enough.

To complete the code, we merely have to add the handlers associated with the various events that
interest us: clicking on the canvas to add a new block, and clicking on the buttons to modify the
view into the virtual sheet. Most of this is purely mechanical at this point. First, we need global
variables to hold the content of the virtual sheet as well as the view shown through the canvas,
because the handlers will need to modify both.

    const CONTENT = []

    const VIEW = {
      startx: 0,
      starty: 0,
      zoom: 1
    }

In a real system, of course, this information would be kept in the state management layer, or
encapsulated in an object of the system.

We set up the handlers when the page loads:

    window.addEventListener('load', run)
    
    function run() {
      const canvas = document.getElementById('canvas')
      canvas.width = canvas.clientWidth
      canvas.height = canvas.clientHeight
      canvas.addEventListener('click', canvasClickHandler)
      document.getElementById('btn-up').addEventListener('click', btnUpHandler)
      document.getElementById('btn-down').addEventListener('click', btnDownHandler)
      document.getElementById('btn-left').addEventListener('click', btnLeftHandler)
      document.getElementById('btn-right').addEventListener('click', btnRightHandler)
      document.getElementById('btn-in').addEventListener('click', btnInHandler)
      document.getElementById('btn-out').addEventListener('click', btnOutHandler)
    }

We associated an event handler with the canvas so that we can add a new block to the virtual sheet
by clicking on the canvas. In that event handler, we get the coordinates of the click *within* the
canvas element (basically a canvas position), convert it to a virtual position on the sheet given
the current view using function `fromCanvas()` defined above, and add the block to the content list.
We call `render` to update the canvas after the change:

    function getCanvasOffset() {
      const canvas = document.getElementById('canvas')
      return [canvas.offsetLeft, canvas.offsetTop]
    }

    function canvasClickHandler(evt) {
      const [eX, eY] = getCanvasOffset()
      const vp = fromCanvas(VIEW, {x: evt.clientX - eX, y: evt.clientY - eY})
      CONTENT.push(vp)
      render(CONTENT, VIEW)
    }

The buttons to move simply change the virtual coordinates of the upper left corner of the canvas,
rendering the canvas after each update to reflect the new view. We move the virtual sheet by 50
*canvas units* to make it easier to navigate large virtual sheets:

    function btnUpHandler(evt) {
      VIEW.starty = VIEW.starty + (50 / VIEW.zoom)
      render(CONTENT, VIEW)
    }
    
    function btnDownHandler(evt) {
      VIEW.starty = VIEW.starty - (50 / VIEW.zoom)
      render(CONTENT, VIEW)
    }
    
    function btnLeftHandler(evt) {
      VIEW.startx = VIEW.startx + (50 / VIEW.zoom)
      render(CONTENT, VIEW)
    }
    
    function btnRightHandler(evt) {
      VIEW.startx = VIEW.startx - (50 / VIEW.zoom)
      render(CONTENT, VIEW)
    }

The buttons to zoom in and out are perhaps a touch more interesting, because while they basically
just increase or decrease the zoom factor, we would like to give the effect of zooming into or out
of the *center* of the canvas. This requires finding the virtual coordinates of the center of the
canvas (given the current view), then recalculating the new virtual coordinates of the upper left
corner of the canvas after the zoom factor change:

    function getCanvasSize() {
      const canvas = document.getElementById('canvas')
      return [canvas.width, canvas.height]
    }
    
    function center(view, {x, y}) {
      // return start x, y that puts virtual x, y in the center of the canvas.
      const [width, height] = getCanvasSize()
      const start_x = x - (width / 2) / view.zoom
      const start_y = y - (height / 2) / view.zoom
      return {x: start_x, y: start_y}
    }
    
    function btnInHandler(evt) {
      const [width, height] = getCanvasSize()
      const old_c = fromCanvas(VIEW, {x: width / 2, y: height / 2})
      VIEW.zoom = VIEW.zoom * 1.25
      const new_start = center(VIEW, old_c)
      VIEW.startx = new_start.x
      VIEW.starty = new_start.y
      render(CONTENT, VIEW)
    }
    
    function btnOutHandler(evt) {
      const [width, height] = getCanvasSize()
      const old_c = fromCanvas(VIEW, {x: width / 2, y: height / 2})
      VIEW.zoom = VIEW.zoom / 1.25
      const new_start = center(VIEW, old_c)
      VIEW.startx = new_start.x
      VIEW.starty = new_start.y
      render(CONTENT, VIEW)
    }
    
And that's it! [You can try the result](./canvas.html): add blocks, move about the virtual sheet,
zoom in and out.  This is the essential Figma navigation functionality. The Javascript code itself is in
file [`canvas.js`](./canvas.js). 

Obvious next steps for generalization include:

- support different shape in the virtual sheet rather than red blocks: lines, rectangles, circles,
  text boxes, etc.
- connect canvas movement to mouse event: click and drag on the canvas to move it, use the scroll
  button to zoom in and out, etc.
- save and load the list of objects on the virtual sheet

More interesting is the ability to select an object on the virtual sheet and manipulate it: move it,
change it, or delete it. This would require a slightly more involved data structure to represent
those objects so that it's easier to determine if there is an object on the virtual sheet covering a
specific point, and if so which one. Structures like [R-trees](https://en.wikipedia.org/wiki/R-tree)
have been developed for that purpose.

I may come back to this later once I make some progress on the project that sent me down this rabbit
hole in the first place: a web app that lets me create and manipulate virtual index cards on an
infinite board. Stay tuned.

