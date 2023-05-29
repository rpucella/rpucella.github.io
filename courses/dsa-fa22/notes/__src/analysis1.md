<script>
  document.title = 'Analysis of Algorithms'
</script>


# Analysis of Algorithms (1)

An **algorithm** is a computational procedure that solves a well-defined computational problem, by
taking a set of values (input) and producting a set of values (output).

A **problem** is defined by specfying the desired input/output relationship.

EXAMPLE: the sorting problem.  
Input: a sequence of integers <math><mrow><mo form="prefix" stretchy="false">⟨</mo><msub><mi>a</mi><mn>1</mn></msub><mo separator="true">,</mo><msub><mi>a</mi><mn>2</mn></msub><mo separator="true">,</mo><mo>…</mo><mo separator="true">,</mo><msub><mi>a</mi><mi>n</mi></msub><mo form="postfix" stretchy="false">⟩</mo></mrow></math>
of length <math><mi>n</mi></math>  
Output: a permutation <math><mrow><mo form="prefix" stretchy="false">⟨</mo><msub><mi>a</mi><msub><mi>i</mi><mn>1</mn></msub></msub><mo separator="true">,</mo><msub><mi>a</mi><msub><mi>i</mi><mn>2</mn></msub></msub><mo separator="true">,</mo><mo>…</mo><mo separator="true">,</mo><msub><mi>a</mi><msub><mi>i</mi><mi>n</mi></msub></msub><mo form="postfix" stretchy="false">⟩</mo></mrow></math> of the input sequence with <math><mrow><msub><mi>a</mi><msub><mi>i</mi><mn>1</mn></msub></msub><mo>≤</mo><msub><mi>a</mi><msub><mi>i</mi><mn>2</mn></msub></msub><mo>≤</mo><mo>⋯</mo><mo>≤</mo><msub><mi>a</mi><msub><mi>i</mi><mi>n</mi></msub></msub></mrow></math>

An **instance** of a problem is a specific input to the problem. For example, <math><mrow><mo form="prefix" stretchy="false">⟨</mo><mn>12</mn><mo separator="true">,</mo><mn>3</mn><mo separator="true">,</mo><mn>2</mn><mo separator="true">,</mo><mn>10</mn><mo separator="true">,</mo><mn>8</mn><mo form="postfix" stretchy="false">⟩</mo></mrow></math>
is an instance of the sorting problem.

An algorithm is **correct** if it computes the desired output for every input, as per the problem
specification. More on correctness next time.


## Pseudo-Code

We are going to write our algorithms down in pseudo code, which you can think of as an informal programming language. The idea here is that pseudo code is for humans to read, while still being close enough to a programming language that it should be clear how to implement an algorithm written in pseudo code. Moreover, the pseudo code is chosen to make runtime analysis easier by exposing the primitive operations of the underlying computational model. (See below.)

The pseudo code we will use captures the essence of imperative programming, in which code is a sequence of instructions, and each instructions is:

- an assignment to a variable or an array location
- a for loop between two bounds
- a while loop with a condition expression
- a conditional if with a condition expression and two blocks of instructions to execute when the condition is true and when it is false false
- calls to helper functions

The types of value include booleans, strings, integers, as well as arrays of the same. An array is a contiguous area of memory, and you can access array elements in constant time by indexing. 

The pseudo code we will use 1-indexes arrays, because that's what the textbook uses. (Mathematicians like to 1-index sequences, and algorithms are studied mostly by theoretical computer scientists, which are a class of mathematicians.)


## Sorting: Bubble Sort

Here is Bubble Sort, a slightly simpler variant than the one presented last
time. Basically, instead of repeatedly looping over the whole array
flipping every entry that's out of order, we can simply loop over
smaller and smaller sections of the array, and at each iteration
"bubble down" the smallest value to the left, slowly growing the sorted array
on the left side of the array.

    BUBBLE-SORT(A = <a_1, ..., a_n>) =           
    for i <- 1 to n-1
       for j <- n-1 to i
          if A[j] > A[j+1]
             temp = A[j]
             A[j] = A[j+1]
             A[j+1] = temp
	  
Selection Sort is an improved variant of Bubble Sort whereby instead of "bubbling down" the smallest
values, the smallest value is searched for in the remainder of the array at every step. (You'll work
on Selection Sort on homework 2.)


## Sorting: Insertion Sort

Bubble Sort is based on the intuition that we can sort by basically
"bubbling down" the smallest values to the left, accumulating the
sorted sequence element by element. 

A different intuition is to scan from left to right, and to simply
take the next element in the array and insert into the sorted array of
all elements we have seen until then. This sorted array can be grown
in the left part of the current array, just like for Bubble Sort.

    INSERTION-SORT(A = <a_1, ..., a_n>) =
    for i = 2 to n
       item = A[i]
       j = i-1
       while j > 0 and A[j] > item
          A[j+1] = A[j]
          j = j-1
       A[j+1] = item


## Running Time of Loop Algorithms

The running time of an algorithm on a given input is a measure of how long the
algorithm takes to "run" on that given input. Since an algorithm is
not code and not actually run on an actual machine, we need a notion
of time that's independent of a machine. It is common to count the
number of "primitive operations" that an algorithm takes, based on a
*computational model* that defines what operations are considered
primitives.

The model we use is the **RAM (random-access machine) model**. It is a
model that reflects the standard Von Neumann computer architecture
consisting of a CPU executing sequences of instructions that can
perform basic arithmetic as well as read and write to memory in time
independent of location.

Since we cannot give running time for all possible inputs, we focus on
running time for the *worst case* input of a given size. This strikes
a good balance between having running time that can practically be
estimated and still be useful. (Alternatives to worst-case analysis is
average-case analysis, but that requires an understanding of what is a
typical input, or more generally requires a probability distribution
over inputs, the hold of which is quite difficult to get.)

Our pseudo-code was chosen to match the primitive operations of the
computational model. We can simply count the number of times any given
line of pseudo-code is executed on the worst-case input of any given
size. If the line occurs in the middle of a loop, that line is
executed however many times the loop says it should be executed. Be
mindful when loops are nested. Also, note that a for loop from <math><mn>1</mn></math> to <math><mi>n</mi></math>
(say) is executed <math><mrow><mi>n</mi><mo>+</mo><mn>1</mn></mrow></math> times: once for 1, 2, 3, 4, ..., <math><mi>n</mi></math>, and then
once more to get to <math><mrow><mi>n</mi><mo>+</mo><mn>1</mn></mrow></math> and break out of the loop. (That's the
typical implementation of for loops - it turns out it doesn't impact
the asymptotic running time, but it's good to keep in mind when
looking at the analyses from the textbook.)

Let's calculate the number of times lines are executed for the above
two algorithms.

    BUBBLE-SORT(A = <a_1, ..., a_n>) =           
    for i <- 1 to n-1                        #  n
       for j <- n-1 to i                     #  Sum{i=1}{n-1}(n - i + 1)
          if A[j] > A[j+1]                   #  Sum{i=1}{n-1}(n - i)
             temp = A[j]                     #  Sum{i=1}{n-1}(n - i)
             A[j] = A[j+1]                   #  Sum{i=1}{n-1}(n - i)
             A[j+1] = temp                   #  Sum{i=1}{n-1}(n - i)

where I'm using notation `Sum{i=a}{b}(x)` for <math><mrow><msubsup><mo movablelimits="false">∑</mo><mrow><mi>i</mi><mo>=</mo><mi>a</mi></mrow><mi>b</mi></msubsup><mi>x</mi></mrow></math>.

Note that I'm using the fact that in the worst-case input, all of `A[j] > A[j+1]` will come out as
true (i.e., the array is decreasing from left to right). If we let <math><mrow><msub><mi>T</mi><mi>B</mi></msub><mo form="prefix" stretchy="false">(</mo><mi>n</mi><mo form="postfix" stretchy="false">)</mo></mrow></math> be the running time of
the algorithm on a worst-case input array of length <math><mi>n</mi></math>, then summing the column above on the right we get:

<math display="block"><mrow><msub><mi>T</mi><mi>B</mi></msub><mo form="prefix" stretchy="false">(</mo><mi>n</mi><mo form="postfix" stretchy="false">)</mo><mo>=</mo><mi>n</mi><mo>+</mo><mrow><munderover><mo movablelimits="false">∑</mo><mrow><mi>i</mi><mo>=</mo><mn>1</mn></mrow><mrow><mi>n</mi><mo>−</mo><mn>1</mn></mrow></munderover></mrow><mo form="prefix" stretchy="false">(</mo><mi>n</mi><mo>−</mo><mi>i</mi><mo>+</mo><mn>1</mn><mo form="postfix" stretchy="false">)</mo><mo>+</mo><mn>4</mn><mo form="prefix" stretchy="false">(</mo><mrow><munderover><mo movablelimits="false">∑</mo><mrow><mi>i</mi><mo>=</mo><mn>1</mn></mrow><mrow><mi>n</mi><mo>−</mo><mn>1</mn></mrow></munderover></mrow><mo form="prefix" stretchy="false">(</mo><mi>n</mi><mo>−</mo><mi>i</mi><mo form="postfix" stretchy="false">)</mo><mo form="postfix" stretchy="false">)</mo></mrow></math>

We can use some standard equalities to simplify the summations:

<math display="block"><mtable displaystyle="true" rowspacing="0.25em" columnalign="right left" columnspacing="0em" class="tml-array tml-gather"><mtr><mtd style="text-align:-webkit-right;"><mrow></mrow></mtd><mtd style="text-align:-webkit-left;"><mrow><mrow><munderover><mo movablelimits="false">∑</mo><mrow><mi>i</mi><mo>=</mo><mi>a</mi></mrow><mi>b</mi></munderover></mrow><mn>1</mn><mo>=</mo><mi>b</mi><mo>−</mo><mi>a</mi><mo>+</mo><mn>1</mn></mrow></mtd></mtr><mtr><mtd style="text-align:-webkit-right;"><mrow></mrow></mtd><mtd style="text-align:-webkit-left;"><mrow><mrow><munderover><mo movablelimits="false">∑</mo><mrow><mi>i</mi><mo>=</mo><mn>1</mn></mrow><mi>k</mi></munderover></mrow><mi>i</mi><mo>=</mo><mfrac><mrow><mi>k</mi><mo form="prefix" stretchy="false">(</mo><mi>k</mi><mo>+</mo><mn>1</mn><mo form="postfix" stretchy="false">)</mo></mrow><mn>2</mn></mfrac></mrow></mtd></mtr><mtr><mtd style="text-align:-webkit-right;"><mrow></mrow></mtd><mtd style="text-align:-webkit-left;"><mrow><mrow><munderover><mo movablelimits="false">∑</mo><mrow><mi>i</mi><mo>=</mo><mi>a</mi></mrow><mi>b</mi></munderover></mrow><mo form="prefix" stretchy="false">(</mo><mi>x</mi><mo>+</mo><mi>y</mi><mo form="postfix" stretchy="false">)</mo><mo>=</mo><mo form="prefix" stretchy="false">(</mo><mrow><munderover><mo movablelimits="false">∑</mo><mrow><mi>i</mi><mo>=</mo><mi>a</mi></mrow><mi>b</mi></munderover></mrow><mi>x</mi><mo form="postfix" stretchy="false">)</mo><mo>+</mo><mo form="prefix" stretchy="false">(</mo><mrow><munderover><mo movablelimits="false">∑</mo><mrow><mi>i</mi><mo>=</mo><mi>a</mi></mrow><mi>b</mi></munderover></mrow><mi>y</mi><mo form="postfix" stretchy="false">)</mo></mrow></mtd></mtr><mtr><mtd style="text-align:-webkit-right;"><mrow></mrow></mtd><mtd style="text-align:-webkit-left;"><mrow><mrow><munderover><mo movablelimits="false">∑</mo><mrow><mi>i</mi><mo>=</mo><mi>a</mi></mrow><mi>b</mi></munderover></mrow><mo form="prefix" stretchy="false">(</mo><mi>c</mi><mi>x</mi><mo form="postfix" stretchy="false">)</mo><mo>=</mo><mi>c</mi><mo form="prefix" stretchy="false">(</mo><mrow><munderover><mo movablelimits="false">∑</mo><mrow><mi>i</mi><mo>=</mo><mi>a</mi></mrow><mi>b</mi></munderover></mrow><mi>x</mi><mo form="postfix" stretchy="false">)</mo><mtext> </mtext><mtext> </mtext><mtext> </mtext><mrow><mtext>w</mtext><mtext>h</mtext><mtext>e</mtext><mtext>n</mtext><mtext> </mtext><mi>c</mi><mtext> </mtext><mtext>d</mtext><mtext>o</mtext><mtext>e</mtext><mtext>s</mtext><mtext> </mtext><mtext>n</mtext><mtext>o</mtext><mtext>t</mtext><mtext> </mtext><mtext>r</mtext><mtext>e</mtext><mtext>f</mtext><mtext>e</mtext><mtext>r</mtext><mtext> </mtext><mtext>t</mtext><mtext>o</mtext><mtext> </mtext><mi>i</mi></mrow></mrow></mtd></mtr></mtable></math>

and we can derive:

<math display="block"><mrow><msub><mi>T</mi><mi>B</mi></msub><mo form="prefix" stretchy="false">(</mo><mi>n</mi><mo form="postfix" stretchy="false">)</mo><mo>=</mo><mfrac><mn>5</mn><mn>2</mn></mfrac><msup><mi>n</mi><mn>2</mn></msup><mo>−</mo><mfrac><mn>1</mn><mn>2</mn></mfrac><mi>n</mi><mo>−</mo><mn>1</mn></mrow></math>

Let's remember that.

    INSERTION-SORT(A = <a_1, ..., a_n>) =
    for i = 2 to n                         #  n
       item = A[i]                         #  n-1
       j = i-1                             #  n-1
       while j > 0 and A[j] > item         #  Sum{i=2}{n}(i)
          A[j+1] = A[j]                    #  Sum{i=2}{n}(i - 1)
          j = j-1                          #  Sum{i=2}{n}(i - 1)
       A[j+1] = item                       #  n-1

Here, I'm using the fact that in the worst-case input, the while loop will decrease `j` all the way
down to `0` (again, when the input array is decreasing from left to right). If we let <math><mrow><msub><mi>T</mi><mi>I</mi></msub><mo form="prefix" stretchy="false">(</mo><mi>n</mi><mo form="postfix" stretchy="false">)</mo></mrow></math> be
the running time of the algorithm on a worst-case input array of length <math><mi>n</mi></math>, then

<math display="block"><mrow><msub><mi>T</mi><mi>I</mi></msub><mo form="prefix" stretchy="false">(</mo><mi>n</mi><mo form="postfix" stretchy="false">)</mo><mo>=</mo><mi>n</mi><mo>+</mo><mn>3</mn><mo form="prefix" stretchy="false">(</mo><mi>n</mi><mo>−</mo><mn>1</mn><mo form="postfix" stretchy="false">)</mo><mo>+</mo><mrow><munderover><mo movablelimits="false">∑</mo><mrow><mi>i</mi><mo>=</mo><mn>2</mn></mrow><mi>n</mi></munderover></mrow><mi>i</mi><mo>+</mo><mn>2</mn><mo form="prefix" stretchy="false">(</mo><mrow><munderover><mo movablelimits="false">∑</mo><mrow><mi>i</mi><mo>=</mo><mn>2</mn></mrow><mi>n</mi></munderover></mrow><mo form="prefix" stretchy="false">(</mo><mi>i</mi><mo>−</mo><mn>1</mn><mo form="postfix" stretchy="false">)</mo><mo form="postfix" stretchy="false">)</mo></mrow></math>

which simplifies to

<math display="block"><mrow><msub><mi>T</mi><mi>I</mi></msub><mo form="prefix" stretchy="false">(</mo><mi>n</mi><mo form="postfix" stretchy="false">)</mo><mo>=</mo><mfrac><mn>3</mn><mn>2</mn></mfrac><msup><mi>n</mi><mn>2</mn></msup><mo>+</mo><mfrac><mn>7</mn><mn>2</mn></mfrac><mi>n</mi><mo>−</mo><mn>4</mn></mrow></math>


## Asymptotic Running Time

When <math><mi>n</mi></math> (the size of the array to be sorted) gets large, the running time of Bubble Sort and Insertion Sort is dominated by the term n^2, and generally, the rate at which Bubble Sort and Insertion Sort slow down on larger inputs is similar. We can make that formal by talking about *aysymptotic* running time, which lets us focus on the general "shape" of the running time curve.

There are many ways of talking about the shape of a curve.

<math display="block"><mrow><mrow><mi mathvariant="normal">Θ</mi></mrow><mo form="prefix" stretchy="false">(</mo><mi>g</mi><mo form="postfix" stretchy="false">)</mo><mo>=</mo><mo form="prefix" stretchy="false">{</mo><mi>f</mi><mo lspace="0.22em" rspace="0.22em" stretchy="false">|</mo><mtext>there exists</mtext><mtext> </mtext><msub><mi>c</mi><mn>1</mn></msub><mo>&gt;</mo><mn>0</mn><mo separator="true">,</mo><msub><mi>c</mi><mn>2</mn></msub><mo>&gt;</mo><mn>0</mn><mo separator="true">,</mo><msub><mi>n</mi><mn>0</mn></msub><mo>&gt;</mo><mn>0</mn><mtext> </mtext><mtext>such that</mtext><mtext> </mtext><mn>0</mn><mo>≤</mo><msub><mi>c</mi><mn>1</mn></msub><mi>g</mi><mo form="prefix" stretchy="false">(</mo><mi>n</mi><mo form="postfix" stretchy="false">)</mo><mo>≤</mo><mi>f</mi><mo form="prefix" stretchy="false">(</mo><mi>n</mi><mo form="postfix" stretchy="false">)</mo><mo>≤</mo><msub><mi>c</mi><mn>2</mn></msub><mi>g</mi><mo form="prefix" stretchy="false">(</mo><mi>n</mi><mo form="postfix" stretchy="false">)</mo><mtext> </mtext><mtext>for all</mtext><mtext> </mtext><mi>n</mi><mo>≥</mo><msub><mi>n</mi><mn>0</mn></msub><mo form="postfix" stretchy="false">}</mo></mrow></math>

Intuitively, function <math><mi>f</mi></math> is sandwiched between <math><mrow><msub><mi>c</mi><mn>1</mn></msub><mi>g</mi></mrow></math> and <math><mrow><msub><mi>c</mi><mn>2</mn></msub><mi>g</mi></mrow></math> for all <math><mi>n</mi></math> after a certain point.

There are upperbound and lowerbound versions as well which provide less information, but are sometimes the best we can get.

<math display="block"><mtable displaystyle="true" rowspacing="0.25em" columnalign="right left" columnspacing="0em" class="tml-array tml-gather"><mtr><mtd style="text-align:-webkit-right;"><mrow></mrow></mtd><mtd style="text-align:-webkit-left;"><mrow><mi>O</mi><mo form="prefix" stretchy="false">(</mo><mi>g</mi><mo form="postfix" stretchy="false">)</mo><mo>=</mo><mo form="prefix" stretchy="false">{</mo><mi>f</mi><mo lspace="0.22em" rspace="0.22em" stretchy="false">|</mo><mtext>there exists</mtext><mtext> </mtext><msub><mi>c</mi><mn>2</mn></msub><mo>&gt;</mo><mn>0</mn><mo separator="true">,</mo><msub><mi>n</mi><mn>0</mn></msub><mo>&gt;</mo><mn>0</mn><mtext> </mtext><mtext>such that</mtext><mtext> </mtext><mi>f</mi><mo form="prefix" stretchy="false">(</mo><mi>n</mi><mo form="postfix" stretchy="false">)</mo><mo>≤</mo><msub><mi>c</mi><mn>2</mn></msub><mi>g</mi><mo form="prefix" stretchy="false">(</mo><mi>n</mi><mo form="postfix" stretchy="false">)</mo><mtext> </mtext><mtext>for all</mtext><mtext> </mtext><mi>n</mi><mo>≥</mo><msub><mi>n</mi><mn>0</mn></msub><mo form="postfix" stretchy="false">}</mo></mrow></mtd></mtr><mtr><mtd style="text-align:-webkit-right;"><mrow></mrow></mtd><mtd style="text-align:-webkit-left;"><mrow><mrow><mi mathvariant="normal">Ω</mi></mrow><mo form="prefix" stretchy="false">(</mo><mi>g</mi><mo form="postfix" stretchy="false">)</mo><mo>=</mo><mo form="prefix" stretchy="false">{</mo><mi>f</mi><mo lspace="0.22em" rspace="0.22em" stretchy="false">|</mo><mtext>there exists</mtext><mtext> </mtext><msub><mi>c</mi><mn>1</mn></msub><mo>&gt;</mo><mn>0</mn><mo separator="true">,</mo><msub><mi>n</mi><mn>0</mn></msub><mo>&gt;</mo><mn>0</mn><mtext> </mtext><mtext>such that</mtext><mtext> </mtext><mn>0</mn><mo>≤</mo><msub><mi>c</mi><mn>1</mn></msub><mi>g</mi><mo form="prefix" stretchy="false">(</mo><mi>n</mi><mo form="postfix" stretchy="false">)</mo><mo>≤</mo><mi>f</mi><mo form="prefix" stretchy="false">(</mo><mi>n</mi><mo form="postfix" stretchy="false">)</mo><mtext> </mtext><mtext>for all</mtext><mtext> </mtext><mi>n</mi><mo>≥</mo><msub><mi>n</mi><mn>0</mn></msub><mo form="postfix" stretchy="false">}</mo></mrow></mtd></mtr></mtable></math>

We say *f is <math><mrow><mrow><mi mathvariant="normal">Θ</mi></mrow><mo form="prefix" stretchy="false">(</mo><mi>g</mi><mo form="postfix" stretchy="false">)</mo></mrow></math>* if <math><mrow><mi>f</mi><mo>∈</mo><mrow><mi mathvariant="normal">Θ</mi></mrow><mo form="prefix" stretchy="false">(</mo><mi>g</mi><mo form="postfix" stretchy="false">)</mo></mrow></math>. By abuse of notation, some authors write <math><mrow><mi>f</mi><mo>=</mo><mrow><mi mathvariant="normal">Θ</mi></mrow><mo form="prefix" stretchy="false">(</mo><mi>g</mi><mo form="postfix" stretchy="false">)</mo></mrow></math>.

We can check some properties of <math><mrow><mi mathvariant="normal">Θ</mi></mrow></math> notation that comes in handy:

<math display="block"><mtable displaystyle="true" rowspacing="0.25em" columnalign="right left" columnspacing="0em" class="tml-array tml-gather"><mtr><mtd style="text-align:-webkit-right;"><mrow></mrow></mtd><mtd style="text-align:-webkit-left;"><mrow><mi>a</mi><mi>f</mi><mo>∈</mo><mrow><mi mathvariant="normal">Θ</mi></mrow><mo form="prefix" stretchy="false">(</mo><mi>f</mi><mo form="postfix" stretchy="false">)</mo></mrow></mtd></mtr><mtr><mtd style="text-align:-webkit-right;"><mrow></mrow></mtd><mtd style="text-align:-webkit-left;"><mrow><mtext>i</mtext><mtext>f</mtext><mtext> </mtext><mrow><mi>f</mi><mo>∈</mo><mrow><mi mathvariant="normal">Θ</mi></mrow><mo form="prefix" stretchy="false">(</mo><mi>h</mi><mo form="postfix" stretchy="false">)</mo></mrow><mtext> </mtext><mtext>a</mtext><mtext>n</mtext><mtext>d</mtext><mtext> </mtext><mrow><mi>g</mi><mo>∈</mo><mrow><mi mathvariant="normal">Θ</mi></mrow><mo form="prefix" stretchy="false">(</mo><mi>h</mi><mo form="postfix" stretchy="false">)</mo></mrow><mtext> </mtext><mtext>t</mtext><mtext>h</mtext><mtext>e</mtext><mtext>n</mtext><mtext> </mtext><mrow><mi>f</mi><mo>+</mo><mi>g</mi><mo>∈</mo><mrow><mi mathvariant="normal">Θ</mi></mrow><mo form="prefix" stretchy="false">(</mo><mi>h</mi><mo form="postfix" stretchy="false">)</mo></mrow></mrow></mtd></mtr><mtr><mtd style="text-align:-webkit-right;"><mrow></mrow></mtd><mtd style="text-align:-webkit-left;"><mrow><msub><mi>a</mi><mi>k</mi></msub><msup><mi>n</mi><mi>k</mi></msup><mo>+</mo><mo>⋯</mo><mo>+</mo><msub><mi>a</mi><mn>1</mn></msub><mi>x</mi><mo>+</mo><msub><mi>a</mi><mn>0</mn></msub><mo>∈</mo><mrow><mi mathvariant="normal">Θ</mi></mrow><mo form="prefix" stretchy="false">(</mo><msup><mi>x</mi><mi>k</mi></msup><mo form="postfix" stretchy="false">)</mo></mrow></mtd></mtr><mtr><mtd style="text-align:-webkit-right;"><mrow></mrow></mtd><mtd style="text-align:-webkit-left;"><mrow><mtext>i</mtext><mtext>f</mtext><mtext> </mtext><mrow><mi>f</mi><mo>∈</mo><mrow><mi mathvariant="normal">Θ</mi></mrow><mo form="prefix" stretchy="false">(</mo><mi>g</mi><mo form="postfix" stretchy="false">)</mo></mrow><mtext> </mtext><mtext>a</mtext><mtext>n</mtext><mtext>d</mtext><mtext> </mtext><mrow><mi>g</mi><mo>∈</mo><mrow><mi mathvariant="normal">Θ</mi></mrow><mo form="prefix" stretchy="false">(</mo><mi>h</mi><mo form="postfix" stretchy="false">)</mo></mrow><mtext> </mtext><mtext>t</mtext><mtext>h</mtext><mtext>e</mtext><mtext>n</mtext><mtext> </mtext><mrow><mi>f</mi><mo>∈</mo><mrow><mi mathvariant="normal">Θ</mi></mrow><mo form="prefix" stretchy="false">(</mo><mi>h</mi><mo form="postfix" stretchy="false">)</mo></mrow></mrow></mtd></mtr></mtable></math>

Thus, we immediately get that Bubble Sort and Insertion Sort's running times are <math><mrow><mrow><mi mathvariant="normal">Θ</mi></mrow><mo form="prefix" stretchy="false">(</mo><msup><mi>n</mi><mn>2</mn></msup><mo form="postfix" stretchy="false">)</mo></mrow></math>, that is, they are *quadratic time algorithms*.

