<script>
  document.title = 'Homework 1 - FOCS FA23'
</script>

# Homework 1: HTML and CSS


## Due Date: Sunday, February 11, 2024 (23h59)


- This homework is to be done in teams of two. You're welcome to
discuss with other students, but all submitted work must be original
and your own. If you use a solution from another source you must cite
it &mdash; this includes when that source is someone else helping you.

- Here's a zip file [homework1.zip](./homework1.zip) containing an HTML document and associated images used as a starting point. Once you unzip the file, use the "Open File" functionality of your browser to open the `theft.html` file (or just drag it to your browser).

- **Please do not post your solutions on a public website or a public repository like GitHub.**


## Electronic Submission Instructions

- Zip all required files into a file `homework1.zip`.

- Make sure one of the files is a `README` text file listing the members of your team, as well as any remarks you want to make about your code.

- When you're ready to submit, log into our <a href="https://chat.rpucella.net">class chat</a>
and click <b>Submit File</b> in the profile menu (your initials in the upper right corner).


* * * 


## Question 1: Basic Styling

The file `theft.html` in the .zip file is a very basic HTML documenet holding a simplified version of the Wikipedia page on the 1990 Isabelle Stewart Gardner Museum heist back in 1990, right here in Boston. 

Your task: make it pretty, in a way that replicates roughly the formatting of Wikipedia proper. You do not have to make it pixel perfect. 

- Add a margin of 48px around the whole document

- Make the font size for the whole document larger than the browser default. (Make it bigger by 30% or something close.)

- All text should be sans-serif, except for headers at level 1 and 2.

- All text should be spaced so that there is half a line's height between each line. (Hint: look up the `line-height` property.)

- Headers should be separated by 48px from the text above it.

- Headers at level 1 and level 2 (`h1` and `h2` tags) should have a line underneath them that goes
  all the way to the right of the screen.

I clearly can't give you a link to a page that shows you what I want. Instead, here's a [PDF](./theft.pdf) printout of way I want the page to look. 

The PDF also contains the result of the questions below.


* * * 


## Question 2: Links

Read up on the [anchor element](https://developer.mozilla.org/en-US/docs/Web/HTML/Element/a).

- Add links to the three list items in the _External links_ section of the document. Make them links to the following URLs: `https://www.gardnermuseum.org/about/theft`, `https://www.fbi.gov/news/stories/5-million-reward-offered-for-return-of-stolen-gardner-museum-artwork`, and `https://www.wbur.org/podcasts/lastseen`, respectively.

- Make the items in the _Table of contents_ list that's after the first paragraphs internal links to the various sections of the document. (Hint: see _Linking to an element on the same page_ on the Anchor element documentation.)

- Style the table of content so that it has no bullets on each list item, and so that the entire table of content has a border and such that the background has color `f8f9fa`. (See the [PDF](./theft.pdf).)

- Style all links so that they do not have an underline, except when you're hovering over them. (Hint: See the [`:hover` pseudo-class](https://developer.mozilla.org/en-US/docs/Web/CSS/Pseudo-classes).)

<center style="padding:20px;"><img style="width: 400px; padding: 10px; border: 1px solid #cccccc;" src="sample-toc.png"></center>


* * * 


## Question 3: Tables

Read up on [HTML tables](https://developer.mozilla.org/en-US/docs/Learn/HTML/Tables/Basics).

- Replace the preformatted section in the document containing the table of stolen pieces by a _bona fide_ HTML table.

- Style the table with an overall border and each cell itself bordered. Put a background color to the table, with color `eaecf0` as the background color  for the header, and `f8f9fa` as the background color for the rest of the table.

- Center the table in the middle of the page

- Make sure the column names are left aligned. (Some browsers will center them.)

<center style="padding:20px;"><img style="width: 400px; padding: 10px; border: 1px solid #cccccc;" src="sample-table.png"></center>


* * * 


## Question 4: Floating Images

Read up on the [`float` property](https://developer.mozilla.org/en-US/docs/Web/CSS/float).

- Float all three images in the text to the right of the document, and make sure they're all a reasonable consistent size.

- Put a centered caption under each image. The caption to use is in a comment underneath each image in the source of the document. (Hint: see the [figure element](https://developer.mozilla.org/en-US/docs/Web/HTML/Element/figure) for one way to do that.)

- Style the image so that there is a border at 4px distance from the image and caption. 

<center style="padding:20px;"><img style="width: 400px; padding: 10px; border: 1px solid #cccccc;" src="sample-float.png"></center>


* * * 


## Question 5: Flexbox

Read up on [Flexbox](https://developer.mozilla.org/en-US/docs/Learn/CSS/CSS_layout/Flexbox) and keep the summary [Flexbox cheatsheet](https://css-tricks.com/snippets/css/a-guide-to-flexbox/) close. 
    
- Use flexbox to place the two sections _References_ and _External links_ in two columns at the end of the document. 

- Make sure there is some gutter space between the two columns. Use your judgment to make it look pleasant.

As we saw in class, to use flexbox effectively, you need the ability to create an element whose sole purpose is to "group" other elements so that those elements are considered a single element. (Each column in a flexbox columnar layout needs to be its own element.) The best way to do that is to use a [`div` element](https://developer.mozilla.org/en-US/docs/Web/HTML/Element/div). Divs are flexbox's drinking buddies.

<center style="padding:20px;"><img style="width: 800px; padding: 10px; border: 1px solid #cccccc;" src="sample-flex.png"></center>


* * * 

## Question 6: Grid

Read up on [Grid](https://developer.mozilla.org/en-US/docs/Web/CSS/grid). Here's a handy [Grid cheatsheet](https://css-tricks.com/snippets/css/complete-guide-grid/).

- Find six pictures of the stolen paintings. 

- Create a new section at the end of the document entitled _Additional pictures_, and make sure that you add the section to the table of contents.

- In that section, create a grid of three pictures over two rows (using `display: grid`) where each picture takes a third of the screen's width, and is centered within its grid cell both horizontally and vertically. 

- Put a border around each picture at some distance (I used 16px) from the picture proper.

- Separate the pictures from each other in a reasonable way (I used 72px distance).

Here's a partial snapshot of what I have:

<center style="padding:20px;"><img style="width: 800px; padding: 10px; border: 1px solid #cccccc;" src="sample-grid.png"></center>


**(Somewhat challenging)**

- Make it so that when you hover over one of the picture, it blows up to 1.5 times its size for as long as you hover over it, returning to its original size once you stop hovering over it.

- Moreover, and this is the slightly challenging bit: I want the blown-up picture to remain within the screen frame, not overflowing to the left or to the right outside the frame. Thus, the pictures in the left column should blow up more towards the right, the pictures in the middle should just blow up and the pictures on the right should blow up towards the left. (Hint: look at the `position` property.) You also probably will not be able to do it using a single uniform class over the whole grid.

No JavaScript. Here's a video of what happens when I hover over the grid:

<center style="padding: 20px;">
<video width="640" controls>
  <source src="sample-zoom.mp4" type="video/mp4">
Your browser does not support the video tag.
</video>
</center>

