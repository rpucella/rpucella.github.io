<script>
  document.title = 'Homework 3 - Web Dev SP24'
</script>

# Homework 3: MVC


## Due Date: Wednesday, March 13, 2024 (23h59)


- This homework is to be done in teams of two. You're welcome to
discuss with other students, but all submitted work must be original
and your own. If you use a solution from another source you must cite
it &mdash; this includes when that source is someone else helping you.

- Here's a zip file [`homework3.zip`](homework3.zip) containing starter files.

- **Please do not post your solutions on a public website or a public repository like GitHub.**


## Electronic Submission Instructions

- Zip all required files into a file `homework3.zip`.

- Make sure one of the files is a `README` text file listing the members of your team, as well as any remarks you want to make about your code.

- When you're ready to submit, log into our [class chat](https://chat.rpucella.net)
and click **Submit File** in the profile menu (your initials in the upper right corner).


* * * 

Back to our Memory game.

We're going to re-implement the game we created in the last homework, using the MVC techniques we
learned in class.

Please use the structure I used for the picture viewer example from class. It'll make everybody's life easier. I have put some very light structure already in the `homework3.html` and `homework3.js` files I provided you. 


## Question 1: The Game

### (A)

Create a model class `Model` for the Memory game.

I suggest you have an array of cards objects (each card object a simple "dictionary") as
the state of the model, where each card object holds:

- the card type: an integer from 0, 1, 2, 3, 4, 5.
- the card state: hidden, chosen, or revealed

Recall that a card is hidden when it is flipped so you cannot see its content, revealed when it is
showing its content and has already been matched with another card, and chosen when it is currently
showing and has been chosen as a card to compare against another card.

You can also put additional state in the model if it makes your life easier.

The one action of interest is choosing a card. Choosing a card should have different effects
depending on whether the card is the first card chosen or the second card chosen:

- choosing a card that is already chosen or revealed should have no effect
- when there is no card already chosen, choosing a card should change the state of that card to chosen.
- where is already a card chosen, choosing a card should change the state of that card to chosen,
  and then the two chosen cards are compared. If they are the same card type, then their states are
  both changed to revealed. If they are not the same card type, then after 2 seconds, the two cards
  should have their state set back to hidden.

You only need one type of notification for now: notifying interested views/controllers when a
card has changed its state.

You will need a way to initialize the model with a random board. As in last homework assume a 4 x 3
board of 12 cards (therefore, 6 card types).

### (B)

Create a view class `BoardView` for the board, that displays the cards according to their type and
state in the model. Have it respond to the "card changed state" notification by updating that card's
representation on the screen appropriately. 

For the content of the cards, use the same SVG representation for cards that you used in Homework 2
Q3, with a light gray `#eeeeee` background:

- 0 = red circle
- 1 = blue circle
- 2 = green circle
- 3 = red square
- 4 = blue square
- 5 = green square

Use a picture that you like as the "back of the card" when a card is
hidden, for variety's sake. Change the cursor to be a pointer when hovering over a card that is hidden.

(You can also create a view `CardView` that you associate with each card, alongside the card
controller below. There's advantages and disadvantage to that approach as opposed to doing all the
card view stuff in `BoardView`. Feel free to explore the design space.)

Make sure you can distinguish a card that is chosen from a card that is revealed. You can use the same kind of highlight (dark grey `#666666` border) as you did on the last homework.


### (C)


Create a controller `CardController` that you can associate with a
specific card, and whose job is ust to respond to clicks on that card
and invoke the action "choose card" on the model. 

When `BoardView` initializes, it should assign a controller to each card on the board.


<center style="padding: 20px;">
<video width="640" controls>
  <source src="question1.mp4" type="video/mp4">
Your browser does not support the video tag.
</video>
</center>


## Question 2: Round counter

Modify the model and its actions to maintain a new piece of state, the **current round**. The game starts at round 1, and whenever the player chooses two cards (and the game either reveals them or hides them back), the round counter increases by one.

Add a new view `RoundView` that shows the current round of the game in some way. You will need a new notification channel to notify views that the current round has changed.



<center style="padding: 20px;">
<video width="640" controls>
  <source src="question2.mp4" type="video/mp4">
Your browser does not support the video tag.
</video>
</center>


## Question 3: Starting a new game

Just like in homework 2, add a congratulatory end dialog with a "play again" button when the player finishes the game.

You will need a new piece of state in the model, whether the game is **finished** or not. The game being finished should be determined in the choosing a card action: when the last pair of matches is revealed, the state should be flipped to finished. There should be a notification channel for the game being finished.

When the game is **finished**, then choosing a card should have no effect.

Add a new action to the model to start a new game, which should create a new random board with the cards set to hidden, and clear the "game finished" flag. The biggest challenge here is how to get `BoardView` to show the new cards. Depending on how you implemented `BoardView`, you may need to refactor some things. It is fine if you need to add a additional channel to notify `BoardView` that the board has changed. I'm curious to see how you solve that problem.

Create a view `FinishedView` that subscribes to the "game finished" notification and that shows a dialog box with a "Play again" button when notified of the game being finished. In response to clicking on the  "Play again" button, the "start new game" action should be invoked. Make sure the dialog box disappears when you start a new game, and that the rounds start back at 1. 

Set things up so that the initial game setup simply uses the "start new game" action.

And while we're at it, add a button "Start new game" associated with a
`NewGameController` that lets you give up on your current game and
sets up a new board when you click on the button, again by invoking
the "start new game" action.


<center style="padding: 20px;">
<video width="640" controls>
  <source src="question3.mp4" type="video/mp4">
Your browser does not support the video tag.
</video>
</center>


