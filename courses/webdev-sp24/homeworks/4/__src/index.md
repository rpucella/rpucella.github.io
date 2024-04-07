<script>
  document.title = 'Homework 4 - Web Dev SP24'
</script>

# Homework 4: React


## Due Date: Wednesday, April 3, 2024 (23h59)


- This homework is to be done in teams of two. You're welcome to
discuss with other students, but all submitted work must be original
and your own. If you use a solution from another source you must cite
it &mdash; this includes when that source is someone else helping you.

- Here's a zip file [`homework4.zip`](homework4.zip) containing starter files.

- **Please do not post your solutions on a public website or a public repository like GitHub.**


## Electronic Submission Instructions

- Zip all required files into a file `homework4.zip`. Do NOT include the `node_modules/` or the `build/` folders, because they get way too large, and I can reconstruct them on my end anyway.

- Make sure you edit the `README.md` file to list the members of your team, as well as any remarks you
  want to make about your code.

- When you're ready to submit, log into our [class chat](https://chat.rpucella.net)
and click **Submit File** in the profile menu (your initials in the upper right corner).

***

We're going to re-implement and extend the Memory Game we've been working on for the past few
homework. This time, it's going to be in React.

I've already created the structure of the application in the provided files, using
Create React App as we saw in class.

The first thing you need to do is install the latest version of [Node.js](https://nodejs.org/) on
your system. Once you have Node.js installed, open a command line and go to the folder `homework4/`
included in the zipped `homework4.zip`. There, run `npm install` — that should install all the
packages needed by the application. 

To start the server in "development" mode, type `npm start`.  That should start the server and open
your game in your default browser. Any changes to the source files while the development server is
running will reflect themselves in the browser page. (You may need to hit refresh to restart the
game from scratch.)


***

## Question 1: Once More With Feeling

This one is straightward: redo Homework 3, except in React this time. This includes the round
counter, the button to start a new game, and the dialog box telling you that you have
completed the game and giving you the option to replay a new one.

Restructure the UI so that there is a panel on the left side of the screen (suggested width: 320px)
with the title of the game, the current round count, and the button to restart the game. The right
side of the screen is the game board, showing a board of 3 rows and 4 columns of cards. As usual,
each card is either hidden (you see the back of the card), or revealed (you see the content of the
card). A card is chosen if it is temporarily revealed until two cards are chosen in which case if
they match they become revealed permanently, and if they do not match they go back to being hidden
after a certain amount of time.

I already created a starting `App` component for you, although it does not do much but put some
placeholder text on the screen. I suggest you create a component for every piece of UI that is
needed. In my code, I have a component `Panel` for the left-hand panel, a component `Board` for the
game board, a component `Card` to represent an individual card (rendering either the back of the
card with its image if the card is hidden, or a shape corresponding to the card type if the card is
showing), a `EndDialog` component for the end-of-game dialog box, etc. You should feel
free to define all the components you want and need.

As far as state, the easiest is probably to reproduce the state you put in the model in Homework 3,
assuming you were happy with it. In my code, I used two React state variable: the content of the
game board as an array of card objects, each with the card type and the card state (hidden,
revealed, chosen), and the current game round. You can pass that state down into the components that
need them.

Remember that if you update the card state within the board array by changing the card object in
place, then you'll need to re-create a new array when you call the state update function in order
for React to "notice" that there's been a state change.

Here is my implementation. You do not have to do exactly the same, but it should play roughly the same:

<center style="padding: 20px;">
<video width="800" controls>
  <source src="question1.mp4" type="video/mp4">
Your browser does not support the video tag.
</video>
</center>


***

## Question 2: Variety is the Spice of Life

One advantage of programming our game in React is that it reasonably straightforward to extend it
by simply creating new components and plugging them into the right spots. We're going to show this
by modifying the game to allow for multiple game board sizes.

In this modified version, the first thing that happens when you start a new game is that
you're presented with a choice of size of game board: either 3 rows by 4 columns, 4 rows by 6
columns, or 5 rows by 6 columns. After you've selected a board size, a board (of that size) should
be randomly created, and the game should start.

The easiest way to get that to work is to allow the React state holding the board to be null. When the board variable is null, you can take it as a flag that a new game is starting, and you show the board
size selection. When the player selects a board size, you create a new board, set the React state to
that board, and you can then show the board. To start a new game, you simply set the board state
back to `null`, and the code should then automatically show the board size selector again.

You will need to choose enough shapes and colors so that you can cover the 5 x 6 board size. In my
code, I allow for up to 3 shapes (circle, rectangle, diamond), and up to 5 colors (red, blue, green,
black, orange).

Here is my implementation. You do not have to do exactly the same, but it should play roughly the same:

<center style="padding: 20px;">
<video width="800" controls>
  <source src="question2.mp4" type="video/mp4">
Your browser does not support the video tag.
</video>
</center>



***

## Question 3: A Sprinkle of Salt


In Homework 3, we had only one card style — whatever image you decided to use for the back of the card. In this question, we are going to offer the player a choice of which style of card to use. To keep things simple, you are going to find two images to use when a card is hidden, and let the player switch whenever they want.

Add the card style selector in the left panel below the "start new game" button. Show both card styles, and make the card style currently selected look normal while making the other card style look faded (say, with opacity 0.5). If you can't make that work, use highlights. 

When you click on the card style that is not currently selected, it becomes the new card style, and
any card in play that is hidden should show the new back image.

You will need a new piece of state to record which image is the current card style.

Here is my implementation. You do not have to do exactly the same, but it should play roughly the same:

<center style="padding: 20px;">
<video width="800" controls>
  <source src="question3.mp4" type="video/mp4">
Your browser does not support the video tag.
</video>
</center>

***

## Question 4: Deploying to GitHub Pages

The purpose of this question is to show that if you have a frontend-only web app (that is, one that does not require a web application server), it is easy to deploy it to the public via [GitHub Pages](https://docs.github.com/en/pages).

Here are the steps, assuming you have a GitHub account. These instructions are adapted from [here](https://docs.github.com/en/pages/getting-started-with-github-pages/creating-a-github-pages-site)

- Create a fresh GitHub _public_ project to host your web app. I called mine [`gh-frontend-test`](https://github.com/rpucella/gh-frontend-test). I like to initialize mine with at least a `README.md` file.

- Go to **Settings** tab for your project, and then to the **Pages** option. There, select **Deploy from a branch** as your source, and below select branch **main** and folder **/ (root)**:

<center>
<img width="600" src="source.png">
</center>

- Clone your project locally

- Go into your frontend code for homework 4 above. Edit your `package.json` file to add a field `"homepage"` with associated value `"."` into it. It doesn't matter where, as long as it is at top level:

        "homepage": ".",
	
<center>
<img width="600" src="package.png">
</center>

- Run `npm run build` to build your frontend into the `build/` folder.

- At this point you should try to run `python3 -m http.server -d build` and connect your browser to `localhost:8000/index.html` to make sure your frontend works.

- Copy the content of the `build/` folder into your new project. This is what your project folder should look like (roughly):

<center>
<img width="600" src="root.png">
</center>

- Commit the changes, and push to your remote branch on GitHub.

- At this point, the website should be publishing. Go to the **Actions** tab for your project, and click on the deployment that should be ongoing. Once the deployment is done, everything should turn green, and you will have a link to your published site, of the form `https://<username>.github.io/<projectname>`:

<center>
<img width="600" src="build.png">
</center>

[Here's mine](https://rpucella.github.io/gh-frontend-test).
