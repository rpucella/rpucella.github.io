<script>
  document.title = 'Notes on React'
</script>

# 
# Notes on React


[React](https://react.dev/) is a framework for developing frontends in
Javascript with an emphasis on composing together reusable components.

A component is a unit of UI consisting of HTML (and CSS) with some associated behavior.

My goal today is to introduce React in a way that is compatible with how we worked with the
MVC architecture in our last two classes. This is purely for pedagogical reasons. This is not how
React is used in practice, and we're going to see how to use it "properly" next class.

(Why this detour? Because I want to explain a little bit what is going on under the hood. React these days
is streamlined to the point where it is easy to use, but also pretty mysterious.)

First, let's start with the HTML code:

    <!DOCTYPE html>
    <html>
      <meta charset="utf-8">
      <head>
        <title>React Test</title>
	
        <style>
          .counter {
              width: 240px;
              height: 240px;
              display: flex;
              flex-direction: column;
              align-items: center;
              padding: 24px;
              gap: 24px;
              border: 1px solid #333333;
              box-sizing: border-box;
          }
	  
          .counter .header {
              font-weight: bold;
          }
	  
          .count {
              font-weight: bold;
              font-size: 80px;
          }
        </style>
	
        <!-- Load react libraries -->
        <script src="https://unpkg.com/react@18/umd/react.development.js" crossorigin></script>
        <script src="https://unpkg.com/react-dom@18/umd/react-dom.development.js" crossorigin></script>
        <script src="./test.js"></script>
	
      </head>
    
      <body>
        <div id="app">
        </div>
      </body>
    </html>

Nothing particularly special going on here. There are some CSS classes that we're going to use in the code later.
Note that we are loading the two React libraries: `react`, and `react-dom`. Those make the React framework available
to us through the global variables `React` and `ReactDOM`. Also note that the body of the page is a single div element
with ID `app`.

File `test.js` contains the actual application. In a way similar to
our MVC code, we assign a function to the window `load` event to
initialize the app.

    const e = React.createElement
    
    window.onload = () => {
      const domContainer = document.getElementById('app')
      const root = ReactDOM.createRoot(domContainer)
      root.render(e(Test, {}))
    }

This boilerplate basically tells React to add code to the `app` div.

The code is added through function `React.createElement` (abbreviated `e` in the rest of the code for convenience)
which is basically a souped up version of `document.createElement`:

- Its first argument is either a tag (like `document.createElement`) or a React component (which we'll cover below). We'll focus on tags for now.
- The second arguent is an object containing the attributes of the element to be created.
- Additional arguments represent the body of the element being created, and can be text, or other elements created via `React.createElement`

For example,

    e('h1', {}, 'Hello React!')

would create an HTML element

    <h1>Hello React!</h1>

while

    e('img', {src: 'dog.png'})

would create an HTML element

    <img src="dog.png">

Similarly,

    e('div', {},
      e('h1', {}, 'Hello React!'),
      e('img', {src: 'dog.png'}))

would create the element

    <div>
      <h1>Hello React!</h1>
      <img src="dog.png">
    </div>

Pretty straightforward. At this point, `React.createElement` is just a more expressive
version of `document.createElement`. 

`React.createElement` can also be used to create React component. To a first approximation,
a React component is a *function* that returns some HTML code created via `React.createElement`.

For example, this is simple component that shows a title and a paragraph of text:

    function Test() {
       return e('div', {},
                e('h1', {}, 'React Test'),
		e('p', {}, 'Hello - this is your first React component'))
    }

To use this component (often referred to as _instantiating the
component_) you simply put the function as the first argument to
`React.createElement`, as in `e(Test, {})`.  This will create the
element that's obtained by calling the function.

You can also parameterize the component, so that you can instantiate it with different values. The function defining the component simply takes those parameters via an object, and you can pass those parameters via the second argument to `React.createElement`. For example:

    function Test({name}) {
       return e('div', {},
                e('h1', {}, 'React Test'),
		e('p', {}, `Hello ${name} - this is your first React component`))
    }

(this uses something called [destructuring](https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Operators/Destructuring_assignment) to extract the elements of an object argument passed to the function.)

and now you can instantiate this component with:

    e(Test, {name: 'Riccardo'})

or

    e(Test, {name: 'Alexandra'})

to produce either:

    <div>
      <h1>React Test</h1>
      <p>Hello Riccardo - this is your first React component</p>
    </div>

or

    <div>
      <h1>React Test</h1>
      <p>Hello Alexandra - this is your first React component</p>
    </div>

Of course, the elements created by `Test` can themselves use other components.

None of the above is particularly exciting, or particularly
special. What makes React component interesting is that they can have
_state_, and modifying that state will force a _recreation_ of the
component in the document by basically re-running the function that defines the component automatically.

Consider the following component that defines a counter with a button
that when you click increases the count in the counter, and using a
another component `Count` internally to display that actual count:

    function Counter() {
      const [count, setCount] = React.useState(0)
      const handleClick = () => {
        setCount(count + 1)
      }
      return e('div', {className: 'counter'},
               e('div', {className: 'header'}, 'Counter'),
               e('button', {onClick: handleClick}, 'Increment'),
               e(Count, {value: count}))
    }

    function Count({value}) {
      return e('div', {}, value)
    }

The first line of the component is:

    const [count, setCount] = React.useState(0)

is special and somewhat magical. The first time the component is
instantiated, this line records within React a state variable
associated with the component initialized at value 0. It then returns
the current value of that state to variable `count`, and also a
function that you can use to _update_ the value of that special state.

When you call `setCount` to update the state, React will notice, and
re-run the function to re-create the component. When a function
defining a component is re-run to recreate the component, it doesn't
re-create the state. It knows that there is a state that was created
the first time the function was run, and simply uses the _new_ value
of the state that you updated by calling `setCount`. In other words,
the special state you create with `useState` _survives_ each
invocation of the component function. (This is a version of what's
called a [static variable](https://en.wikipedia.org/wiki/Static_variable).)

In the above counter example, you see that we create a React state to
hold the current count (initially 0), and then return some HTML that
shows a button and the current count. When you click the button, you
invoke the `handleClick` event handler, which uses `setCount` to
increment the count and update the React state. _That's_ when React
knows you've changed the state, and it internally clears out the
component's HTML from the document and recreates it by re-invoking the
function, which will use the new count as its current count. Here's the behavior:

<video width="320px" controls>
  <source src="counter.mp4" type="video/mp4">
Your browser does not support the video tag.
</video>

Here is the full code:

    const e = React.createElement
    
    window.onload = () => {
      const domContainer = document.getElementById('app')
      const root = ReactDOM.createRoot(domContainer)
      root.render(e(Test, {}))
    }

    function Test() {
       return e('div', {},
                e('h1', {}, 'React Test'),
                e('Counter', {}))
    }

    function Counter() {
      const [count, setCount] = React.useState(0)
      const handleClick = () => {
        setCount(count + 1)
      }
      return e('div', {className: 'counter'},
               e('div', {className: 'header'}, 'Counter'),
               e('button', {onClick: handleClick}, 'Increment'),
               e(Count, {value: count}))
    }
    
    function Count({value}) {
      return e('div', {}, value)
    }

