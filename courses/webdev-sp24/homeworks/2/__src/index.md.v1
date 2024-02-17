<script>
  document.title = 'Homework 2 - WebDev SP24'
</script>

# Homework 2: Javascript


## Due Date: Sunday, February 25, 2024 (23h59)


- This homework is to be done in teams of two. You're welcome to
discuss with other students, but all submitted work must be original
and your own. If you use a solution from another source you must cite
it &mdash; this includes when that source is someone else helping you.

- Here's a zip file `homework2.zip` (coming soon) containing starter files.

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
rest of the homework, but I'll ask you to submit it within your homework2.zip bundle before the
deadline.

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

## (B) 

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


## Question 1 and beyond

Coming soon
