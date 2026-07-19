---
title: Alignment For Box Layout Component
date: 2026-07-18
reading: Nikolski (by Nicolas Dickner)
prevTitle: A Box Layout Component For Desktop Apps Design
prevUrl: ../../2026/06-box-layout
---

Last week I discussed one way to implement desktop-style user interfaces in a web context using a small [box layout widget](/blog/posts/2026/06-box-layout) that, given a height and width, wraps around other widgets and lays them out either horizontally or vertically, in a nested fashion. Very much like flexbox, except optimized to fit within an given height and width where the size of nested widgets is allocated based on an algorithm that calculated a uniform height and width for all nested widget, subject to optional minimum and maximum specified height and width for all nested widgets. 

Historically, this is the kind of layout that goes back to classical UI widget library for desktop applications, such as the [Tcl/Tk `pack` geometry manager](https://wiki.tcl-lang.org/page/pack).

I implemented that box layout widget for web pages through a set of CSS classes `box-layout`, `box-hz`, and `box-vt` and an associated Javascript file to run the the algorithm to allocate nested widgets heights and widths when the browser window resizes.

One thing I forgot to put in is _alignment_. When laying out out a sequence of widgets horizontally, they can be lined up at their top edge or bottom edge, or they can be centered along their horizontal axis. Similarly, when laying out a sequence of widgets vertically, they can be lined up their left edge or right edge, or they can be centered along their vertical axis. Thankfully, that's easily remedied. Basically, the alignment can be a parameter of the horizontal and vertical layout annotations.

In the CSS-based implementation, this amounts to adding classes to control the alignment: `box-align-top`, `box-align-bottom`, and `box-align-center` for horizontal layouts, and `box-align-left`, `box-align-right`, and the same `box-align-center` for vertical layouts. These classes are used in conjunction with the `box-hz` and `box-vt` classes.

My implementation relies on flexbox for the underlying layout management, so these new classes just translate to a specific `align-items` property. Of course, for a different implementation, these would need to be handled differently.

Here's the revised CSS file: [box-layout.css](./box-layout.css). The Javascript support file remains unchanged: [box-layout.js](./box-layout.js)

Here is an example that shows the various alignments in action:

<center><img src="./example.png" width="75%"></center>

The code to produce it is straightforward, if repetitive:

    <div class="box-layout"  style="height: 600px; width: 600px; background-color: #cccccc;">
         
         <div class="box-hz" style="border: 1px solid #ffffff;" data-height="200">
           <div class="box-vt" style="border: 1px solid #ffffff;" data-width="200">
             <div class="box-vt box-align-left">
               <div class="box-hz box-align-top" data-width="100">
                 <div style="background-color: #333333;" data-height="50" data-width="50"></div>
                 <div style="background-color: #888888;" data-height="150" data-width="50"></div>
               </div>
             </div>
           </div>
           <div class="box-vt" style="border: 1px solid #ffffff;" data-width="200">
             <div class="box-vt box-align-center" data-width="200">
               <div class="box-hz box-align-top" data-width="100">
                 <div style="background-color: #333333;" data-height="50" data-width="50"></div>
                 <div style="background-color: #888888;" data-height="150" data-width="50"></div>
               </div>
             </div>
           </div>
           <div class="box-vt" style="border: 1px solid #ffffff;" data-width="200">
             <div class="box-vt box-align-right" data-width="200">
               <div class="box-hz box-align-top" data-width="100">
                 <div style="background-color: #333333;" data-height="50" data-width="50"></div>
                 <div style="background-color: #888888;" data-height="150" data-width="50"></div>
               </div>
             </div>
           </div>
         </div>

         <div class="box-hz" style="border: 1px solid #ffffff;" data-height="200">
           <div class="box-vt" style="border: 1px solid #ffffff;" data-width="200">
             <div class="box-vt box-align-left">
               <div class="box-hz box-align-center" data-width="100">
                 <div style="background-color: #333333;" data-height="50" data-width="50"></div>
                 <div style="background-color: #888888;" data-height="150" data-width="50"></div>
               </div>
             </div>
           </div>
           <div class="box-vt" style="border: 1px solid #ffffff;" data-width="200">
             <div class="box-vt box-align-center" data-width="200">
               <div class="box-hz box-align-center" data-width="100">
                 <div style="background-color: #333333;" data-height="50" data-width="50"></div>
                 <div style="background-color: #888888;" data-height="150" data-width="50"></div>
               </div>
             </div>
           </div>
           <div class="box-vt" style="border: 1px solid #ffffff;" data-width="200">
             <div class="box-vt box-align-right" data-width="200">
               <div class="box-hz box-align-center" data-width="100">
                 <div style="background-color: #333333;" data-height="50" data-width="50"></div>
                 <div style="background-color: #888888;" data-height="150" data-width="50"></div>
               </div>
             </div>
           </div>
         </div>

         <div class="box-hz" style="border: 1px solid #ffffff;" data-height="200">
           <div class="box-vt" style="border: 1px solid #ffffff;" data-width="200">
             <div class="box-vt box-align-left">
               <div class="box-hz box-align-bottom" data-width="100">
                 <div style="background-color: #333333;" data-height="50" data-width="50"></div>
                 <div style="background-color: #888888;" data-height="150" data-width="50"></div>
               </div>
             </div>
           </div>
           <div class="box-vt" style="border: 1px solid #ffffff;" data-width="200">
             <div class="box-vt box-align-center" data-width="200">
               <div class="box-hz box-align-bottom" data-width="100">
                 <div style="background-color: #333333;" data-height="50" data-width="50"></div>
                 <div style="background-color: #888888;" data-height="150" data-width="50"></div>
               </div>
             </div>
           </div>
           <div class="box-vt" style="border: 1px solid #ffffff;" data-width="200">
             <div class="box-vt box-align-right" data-width="200">
               <div class="box-hz box-align-bottom" data-width="100">
                 <div style="background-color: #333333;" data-height="50" data-width="50"></div>
                 <div style="background-color: #888888;" data-height="150" data-width="50"></div>
               </div>
             </div>
           </div>
         </div>
         
       </div>

Here's the [full file](./example.html).
