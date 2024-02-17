<script>
  document.title = 'Homework 2 - WebDev SP24'
</script>

# Homework 2: Javascript


## Due Date: Sunday, February 25, 2024 (23h59)


- This homework is to be done in teams of two. You're welcome to
discuss with other students, but all submitted work must be original
and your own. If you use a solution from another source you must cite
it &mdash; this includes when that source is someone else helping you.

- Here's a zip file [`homework2.zip`](homework2.zip) containing starter files.

- **Please do not post your solutions on a public website or a public repository like GitHub.**


## Electronic Submission Instructions

- Zip all required files into a file `homework2.zip`.

- Make sure one of the files is a `README` text file listing the members of your team, as well as any remarks you want to make about your code.

- When you're ready to submit, log into our [class chat](https://chat.rpucella.net)
and click **Submit File** in the profile menu (your initials in the upper right corner).


* * * 

In this homework (and the upcoming ones) we are going to build a game of Memory — you know the kind,
where you have a certain number of cards that are hidden, each cards appearing in pairs, and you
reveal cards two at a time: when they match you keep them revealed, and when they don't match you
hide them back. You keep going until you have all cards revealed. The aim is to finish in the fewest
number of moves or in the shortest time. 

In this homework, we're going to get some practice understanding the basic structure of the game and
how we can implement the basic functionality in raw Javascript.


## Question 0: Warm up

First, install the latest version of [Node.js](https://nodejs.org/) on your system. (We're going to need it during the
course anyway.) The latest version is v20, and that'll be just fine for us.

Second, download the file [h2_node.js](./h2_node.js). This is a standalone file not needed for the
rest of the homework, but I'll ask you to submit it within your `homework2.zip` bundle before the
deadline. (For simplicity, I've also added it to the `homework2.zip` bundle above.)

You can edit `h2_node.js` in whatever text editor you like. You can start Node.js by running `node`
from the command line (depending on your machine) and from there load `h2_node.js` using:

    > .load h2_node.js
    
This assumes you ran `node` from the same folder as `h2_node.js` — you will have to give a full path to
`h2_node.js` otherwise.


### (A)

We are going to use integers `0`, `1`, `2`, ... to represent the various card types. A game board
will have two of each card type.

Write a function `createBoard` that takes a number `N` representing the number of card types to
hold in the board, and returns a random board of 2 `N` cards (represented as an array) where each
array position contains a card type. Recall that there must be two occurrences of each card type in
the array.

You'll probably want to use the built-in function [`Math.random`](https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Math/random).

A sample run on my machine in the Node.js shell:

    > createBoard(2)
    [ 0, 0, 1, 1 ]

    > createBoard(2)
    [ 1, 0, 0, 1 ]

    > createBoard(2)
    [ 1, 1, 0, 0 ]

    > createBoard(5)
    [
      2, 3, 1, 0, 4,
      4, 3, 2, 1, 0
    ]

    > createBoard(5)
    [
      0, 3, 4, 1, 0,
      1, 2, 4, 3, 2
    ]

    > createBoard(5)
    [
      3, 2, 0, 1, 1,
      2, 3, 4, 0, 4
    ]

    > createBoard(5)
    [
      0, 0, 4, 3, 1,
      3, 2, 2, 4, 1
    ]

    > createBoard(10)
    [
      0, 2, 4, 9, 3, 8, 6,
      7, 7, 4, 3, 9, 0, 1,
      5, 6, 8, 2, 5, 1
    ]

### (B) 

Let's implement a simple command-line version of the game. This version of the game is pretty
straightforward: it's a loop until all cards are revealed.

Accordingly, write a function `run` that takes a number `N` of card types as arugment and that
implements the following algorithm:

- create a random board with `N` card types (using `createBoard` above)
- loop until all cards on the board are revealed:
    - print the board (only showing the revealed cards)
    - ask for a position using `input()`
    - check that the position is valid and the card there is not revealed
    - show the card at the position
    - ask for another position using `input()`
    - check that the position is valid and the card there is not revealed and the position is
      different from the first position
    - show the card at the position
    - if the cards are the same, congratulate the player and keep the cards revealed on the board
    - if the cards are not the same, keep them "not revealed"

I suggest you represent your board as an array of objects, each object representing a card via two
fields:

    { 
        card: integer,
        revealed: boolean
    }

where `card` is an integer between `0` and `N-1` (the card type) and `revealed` is true when the
card is revealed and false otherwise.

To help you out, I included a function `input()` in `h2_node.js` that lets you query the user for
an input, roughly like Python's [`input` function](https://docs.python.org/3/library/functions.html#input). You invoke it by giving it a prompt, like so `input('First position: ')`. The result is a
string, so you'll have to convert it to an integer.

It's a pretty trivial game to play since you see all your past interactions on the screen :)

A sample run on my machine in the Node.js shell:

    > run(4)

    ---------------------------------
    | - | - | - | - | - | - | - | - |
    ---------------------------------
    First choice: 0
      ==> 2
    Second choice: 1
      ==> 1

    NO MATCH!

    ---------------------------------
    | - | - | - | - | - | - | - | - |
    ---------------------------------
    First choice: 2
      ==> 1
    Second choice: 3
      ==> 2

    NO MATCH!

    ---------------------------------
    | - | - | - | - | - | - | - | - |
    ---------------------------------
    First choice: 4
      ==> 3
    Second choice: 5
      ==> 0

    NO MATCH!

    ---------------------------------
    | - | - | - | - | - | - | - | - |
    ---------------------------------
    First choice: 6
      ==> 0
    Second choice: 7
      ==> 3

    NO MATCH!

    ---------------------------------
    | - | - | - | - | - | - | - | - |
    ---------------------------------
    First choice: 8
    Improper choice - try again

    ---------------------------------
    | - | - | - | - | - | - | - | - |
    ---------------------------------
    First choice: 5
      ==> 0
    Second choice: 6
      ==> 0

    MATCH!

    ---------------------------------
    | - | - | - | - | - | 0 | 0 | - |
    ---------------------------------
    First choice: 4
      ==> 3
    Second choice: 7
      ==> 3

    MATCH!

    ---------------------------------
    | - | - | - | - | 3 | 0 | 0 | 3 |
    ---------------------------------
    First choice: 0
      ==> 2
    Second choice: 3
      ==> 2

    MATCH!

    ---------------------------------
    | 2 | - | - | 2 | 3 | 0 | 0 | 3 |
    ---------------------------------
    First choice: 1
      ==> 1
    Second choice: 2
      ==> 1

    MATCH!


***


## Question 1: Browser game

The `homework2.zip` file contains two files, `homework2.html` and `homework2.js`, and has been setup so that the `homework2.html` file loads the `homework2.js` file as a script. When you load `homework2.html` in your browser (either by dragging the file to it or using "Open File..." in your browser menu:

<center style="padding:20px;">
<img style="width: 300px; padding: 10px; border: 1px solid #cccccc;" src="openfile.png">
</center>

If you open the browser console, you should see the string **Initializing** showing there.

I've set up the `homework2.js` file to run function `init()` when the `homework2.html` page loads
and the DOM is available. Your task in this homework is to set up the game board in the `init()`
function, and to create the appropriate event handlers to actually play the game to
completion. 

There is a sample video below showing my version of the game. Your doesn't have to look exactly like
that. I care about functionality, mostly.

### (A)

For this question, you should create a random board and instantiate a 4 x 3 grid of cards. Choose a
reasonable ratio of about 1.5 for the height of the cards versus their width. Mine are 196px
by 128px.

To create the random board you can use the same function you implemented in Question 0(A). Just copy
it over to `homework2.js`. Since we have 12 cards, you need to create the board with 6 card types.

For reasons that will become clearer next time, don't use the array you get back as the object you
consult during the game to determine the card type for each card in the grid.  Instead, I'm going to
ask that **you store the state of the game (including the card type associated with each card in
the grid) _within the card elements_ themselves**. You can use a [`data`
attribute](https://developer.mozilla.org/en-US/docs/Learn/HTML/Howto/Use_data_attributes. )  for
doing that. I called mine `data-card`. You can set and retrieve the value of that attribute for any given
card as usual by using the `setAttribute()` and `getAtttibute()` methods of the card element.

Since you'll want the card type to also be shown when you reveal the card, you'll also include that
the card type as text on the screen. In Question 2 below, we'll modify cards so that they show
shapes instead of numbers.

Use `#eeeeee` as background color for the card.

<center style="padding:20px;">
<img style="width: 800px; padding: 10px; border: 1px solid #cccccc;" src="question1a.png">
</center>



### (B) 

Let's add the ability to hide/reveal a card. Every should have two states: 

- hidden
- revealed

When a card is revealed, it shows the content like we do in (A) above.

When a card is hidden, it shows a simple `#666666` background without any content.

How do we represent the state of a card? Again, we're just going to store the state of the card
within the card element itself. The easiest is to simply use a class for revealed cards, and a class
for hidden cards. (That'll be useful to style the two types of card differently anyway.) To check
the state of a card, you can simply check for the presence or absence of a specific class. To set
the state, you add/remove the appropriate classes.

Initialize all cards so that they're all hidden at the beginning of the game.

Style the cards so that they get highlighted with color `#333333` when you hover over them and they
are hidden. There is no highlight for revealed cards.

To test, you can use the console to manually add/remove appropriate classes of some cards, or do
that programmatically.

<center style="padding:20px;">
<img style="width: 800px; padding: 10px; border: 1px solid #cccccc;" src="question1b.png">
</center>



### (C) 

Let's add some flipping functionality.

Add a click handler on cards to that when they are hidden they get revealed, and when they are
revealed, they get hidden. Do so by changing the state of the card, since eventually we'll want to
be able to query the state of a card after you've manipulated it.

Change the cursor when you hover over a card to turn it into the cursor that shows when you can
click on something. See the [`cursor`](https://developer.mozilla.org/en-US/docs/Web/CSS/cursor) CSS property.


### (D)

In Part (C), we allowed the user to flip any card over willy nilly. In this question, we're going
to adjust the click event handler to control what you can flip and when so that we implement the
actual rules of Memory.

To distinguih a card that we've revealed as part of one of our two guesses per round from the cards that have been revealed because we've matched in the part, we're going to introduce a new state for cards:

- chosen

When a card is chosen, it appears just like a revealed card by showing the content. One difference
is that a chosen card has a dark border `#666666`, the same color as a hidden card.

Now implement the rules of the game:

- clicking on a revealed or chosen card does nothing
- when you click on a hidden card and there are no chosen card, the card you clicked on becomes a chosen card
- when you click on a hidden card and there is one chosen card, the card you clicked on becomes a chosen card, and:
    - if the two chosen cards have the same card type, then both chosen card switch to the revealed state
    - if the two chosen cards do not have the same card type, then after 2 seconds, the cards switch back to the hidden state

Javascript built-in function [`setTimeout`](https://developer.mozilla.org/en-US/docs/Web/API/setTimeout) is your friend here. Read about it and use it to do the
"wait 2 seconds and do stuff".

Some things to keep in mind:

- Make sure the cursor only changes on hover when the card is hidden (since clicking on a revealed or chosen card does nothing).
- Depending on how you style your elements, you may see the cards jump or shift when a card is chosen and you add a border. Investiate the `box-sizing` property.
- Make sure that while you are waiting 2 seconds before hiding two unmatched chosen cards, you cannot in fact flip any other hidden card.



### (E)


Right now, when you've revealed all cards, you're done, and you can no longer do anything. Let's do
something more interesting when the game is over.

When all cards are revealed, put up a message box on the screen that says "Congratulations" and has a button
"Play Again". Pressing that button should remove the message box and reset the grid to another random board
and a different instance of the game should start.

- When do you check for the game being ended? A natural spot to do so is in the click event handler right after you've revealed the last two cards of the grid.
- How do you put up a message box? The easiest is to show a `<div>` that was previously hidden, that gets positioned in the center of the screen using `position` and setting `left`, `right`, `top`, and `bottom` appropriately, and using the `z-index` property to show the box "on top of" everything else on the screen. To remove the message box, just hide it by setting its `display` property to `none`.

Here's a sample run of my version of the game:


<center style="padding: 20px;">
<video width="640" controls>
  <source src="question1.mp4" type="video/mp4">
Your browser does not support the video tag.
</video>
</center>




***

## Question 2: Nicer cards


The game above is playable. But we should not feel restricted to use the numbers 0–5 as the content of each card. We could use images, or anything, really. 

Let's do shapes for simplicity. Here's the shapes that I'd like you to use for each card type:

- 0 = red circle
- 1 = blue circle
- 2 = green circle
- 3 = red square
- 4 = blue square
- 5 = green square

How do we show a red circle (say) in an HTML element? The easiest way is to use an SVG (scalable vector graphic). Start by [reading a bit on SVGs](https://developer.mozilla.org/en-US/docs/Web/SVG/Tutorial). In particular, you can create a circle with the `<circle>` SVG tag, and a square with a `<rect>` SVG tag.

One very important thing to keep in mind is that if you create an SVG element in Javascript — either an `<svg>` element proper or a `<circle>` or `<rect>` element — you need to use method `createElementNS()` and pass the appropriate _namespace_ as a first argument. The details of why this is needed are pretty opaque. In practice, it just means that to create an SVG element, you have to use something like:

    const svgElt = document.createElementNS('http://www.w3.org/2000/svg', 'svg')
    
You can start [reading this tutorial](https://www.motiontricks.com/creating-dynamic-svg-elements-with-javascript/) which while it goes much deeper than what we'll be doing, does give you the right setup at the start.

Try to get it to work. If you cannot, you can always go with the backup plan of using static PNG images for the various shapes instead. I'll let you figure out how to set that up.

Here's a sample run of my version of the game:

<center style="padding: 20px;">
<video width="640" controls>
  <source src="question2.mp4" type="video/mp4">
Your browser does not support the video tag.
</video>
</center>


