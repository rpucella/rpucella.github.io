---
title: Atomic Design and Go Templates
date: 2023-07-17
reading: Wormholes (by John Fowles)
---

I was introduced to the concept of [Atomic Design](https://bradfrost.com/blog/post/atomic-web-design/) at work, where our design team uses a design system
structured around it.

The idea underlying Atomic Design is straightforward: a design system (a collection of user
interface elements with which you can create branded products like websites and mobile apps) should
be based on a hierarchy of components, with the more primitive components being simple
controls or views, and more complex components being a combination of simpler components.

That structure is of course entirely familiar to programmers, because that's how they organize code:
primitive units like functions are wrapped up into higher-level units like classes or modules or
packages, and those units are composed to create an application. In particular, that's how frontend
development is encouraged in frameworks like [React](https://react.dev/) and [Svelte](https://svelte.dev/) where you define components combining
into larger components to create the final interface. Libraries such as [Tailwind](https://tailwindcss.com/), [Bulma](https://bulma.io/), or
[Bootstrap](https://getbootstrap.com/) are framework-agnostic implementations of the same idea.

Atomic Design basically pulls back this structuring philosophy much earlier in the development
process, namely at the graphic design stage. The *design itself* and not just its implementation
becomes a hierarchy of design components. And this may be done mostly independently of the exact
specifics of the final product. This provides not only a hierarchical library of units that can be
used to design a product, but those units can also be adjusted for different screen sizes, and can
be used to provide a systematic visual language for responsive design.

This is all very abstract. Brad Frost has an ebook out describing the approach. You can check out the
[first chapter](https://atomicdesign.bradfrost.com/chapter-1/) for free, which does a good job summarizing the problem he set out to solve with
Atomic Design as well as detailing the chemical metaphor:

> In the natural world, atomic elements combine together to form molecules. These molecules can
> combine further to form relatively complex organisms. To expound a bit further: 
>
> - Atoms are the basic building blocks of all matter. Each chemical element has distinct properties,
>   and they can’t be broken down further without losing their meaning. (Yes, it’s true atoms are
>   composed of even smaller bits like protons, electrons, and neutrons, but atoms are the smallest
>   functional unit.)
>
> - Molecules are groups of two or more atoms held together by chemical bonds. These combinations of
>   atoms take on their own unique properties, and become more tangible and operational than atoms.
>
> - Organisms are assemblies of molecules functioning together as a unit. These relatively complex
>   structures can range from single-celled organisms all the way up to incredibly sophisticated
>   organisms like human beings.

To a first approximation, atoms are the primitive components, which do not use other components in
their design. For example, input controls and buttons:

![Input](./input.png)  
![Button](./button.png)

Molecules, on the other hand,
are higher-level components built from atoms and possibly other molecules. For example, a search box
may be made up of an input control and a button to perform the search. (It may also have a button to
clear the input field, a label, and additional atoms):

![Search Box](./searchbox.png)

Organisms, for their part, are combinations of molecules making up a specific functional section of
a product: a comment feed, an article carousel, a diagram editor, and so on. In my experience, atoms
and molecules are reusable across projects, while organisms tend to be more specific to a
project. Your mileage may vary.

Frost's book is a quick easy read and worth digging into if you're at all involved or interested in
design. But it's in Chapter 3 *Tools of the Trade* where Frost introduces [Pattern Lab](https://patternlab.io/) that my lizard
brain kicked in. Pattern Lab is Frost's project/tool for helping designer create atomic design
systems. I will let you check it out for the details, but from what I gathered it looks a bit like
an HTML template framework with a restricted template framework that lets you insert "components"
defined elsewhere within a more general layout.

It looked similar enough to template frameworks with which I'm familiar, like Python's [Jinja](https://jinja.palletsprojects.com/en/3.1.x/) or Go's
[`html/template`](https://pkg.go.dev/html/template), that it made me wonder the extent to which one could replicate that set up in a
classical template framework.

Here are the results of that exploration. Just an evening's worth, so take it with a huge grain of
salt.

# First approach: nested templates

Let's take Go's `html/template` engine, if only because that's the one I've been actively using
lately. I'm going to assume you know the basics of Go's template framework, including the use of
`.` as the name of the main template argument. If you need a refresher, the most useful write-up I've
seen about HTML templates in Go is the four-parts article [An Introduction to Templates in
Go](https://www.calhoun.io/intro-to-templates/).

The most natural idea is to have each component (atom, molecule, organism) live in its own
template, and build up either larger components or the final pages out of those independently
defined components. As a specific example, I'm going to use the search box example from earlier.

The out-of-the-box approach would be to use [nested templates](https://www.calhoun.io/intro-to-templates-p2-actions#nested-templates), that is, templates defined in their
own template files and imported into a target template. Nested templates can therefore be reused
across different target templates, and can be used to represent atoms, molecules, and organisms. The
one downsize to nested templates is that they need to be included alongside the target template when
parsing the target template before it can be filled with data. Nested templates are defined

Let's define some basic atoms. First, the input atom, parameterized by the placeholder
text. Clearly, we could add all sorts of styling, but let's keep things clean.

    <!-- input.tpl -->
    
    {{define "input"}}

      <input type="text" placeholder="{{.}}">

    {{end}}

The button atom is also straightforward, and parameterized by the button label:

    <!-- button.tpl -->
    
    {{define "button"}}

      <button>
        {{.}}
      </button>
      
    {{end}}

Per `html/template`'s implementation, this *defines* nested templates that can used in other
templates. They do not by themselves generate any output. To use a defined template, you use the
`template` cation. To illustrate, here's a simple search box molecule:

    <!-- search-box.tpl -->
    
    {{define "search-box"}}

        <div style="display: flex; flex-direction: row;">
          {{template "input" .}}
          {{template "button" "Search"}}
        </div>

    {{end}}

This defines a nested template that can be invoked using the name `"search-box"`, that uses the
input control template and the button template, and whose argument is passed to the input control template
and therefore represents the placeholder text.

Using a search box on a page just amounts to invoking that molecule template on a page, passing in
the placeholder text:

    <!-- page.tpl -->
    
    <html>
      <head>
      </head>
      <body>

        <p>
          A sample molecule:
        </p>

        {{template "search-box" "Enter keywords"}}

      </body>
    </html>

As an example of code that can process the above template, here's a simple `main` function that takes
as input the page template as well as all the templates that are used by the page, and creates the
final HTML content:

    // atomic1.go
    
    package main

    import (
        "os"
        "strings"
        "fmt"
        "html/template"
    )

    func main() {
        args := os.Args
        if len(args) < 2 {
            panic(fmt.Sprintf("Usage: %s <template> ...", args[0]))
        }

        // Parse *all* templates files passed as arguments.
        // The main page is the first template.
        tpl_src := template.New(args[1])
        tpl, err := tpl_src.ParseFiles(args[1:]...)
        if err != nil {
            panic(err)
        }

        // Invoke the main template.
        var b strings.Builder
        if err := tpl.Execute(&b, nil); err != nil {
            panic(err)
        }
        
        // Output the result.
        result := b.String()
        fmt.Println(result)
    }

Here's a sample run of the program (with extra spacing removed):

    $ go run atomic1.go page.tpl input.tpl button.tpl search-box.tpl
    <html>
      <head>
      </head>
      <body>

        <p>
          A sample molecule:
        </p>
        
        <div style="display: flex; flex-direction: row;">

      <input type="text" placeholder="Enter keywords">

      <button>
        Search
      </button>

        </div>

      </body>
    </html>

All in all, this is the result we were hoping for.


# Second approach: a component-loading action

The approach above, using nested templates, works reasonably well for a simple component hierarchy,
and has the advantage of being supported out of the box in the Go ecosystem. But it has two problems
that don't seem easy to alleviate:

1. You need to parse all the nested templates used by the main template while filling the main
   template. That doesn't work so well with the idea of putting together the templates into a
   library that can be reused across projects, since you basically have to load all the nested
   templates files whenever you fill a page template. Either that, or you have to track which nested
   templates are used where so that you only load the required templates. 

2. Go's `html/template` package doesn't seem to support creating map or struct literals to pass to
   nested templates when used in a template. This makes it tricky to, say, extend the input control
   atom to take not just the placeholder as a parameter, but also another parameter such as a
   width. But passing more than one parameter to a template is achieved by passing a
   struct or a map, which can only be obtained from the original invocation of `Execute` from the Go
   program. This means that most of the options used within molecules to control the behavior of
   atoms probably need to be propagated up and exposed to the invoking code, even when those options
   are internal to the molecule. That's not great.
   
The question I idly contemplated last week was whether there was a way to (1) allow a template to
use nested templates without requiring those templates to be given explicitly to `Execute`, and (2)
allow a template to invoke a nested template with a parameter map defined within the template and
not only in the invocation code.

I could think of a way using a custom action `component` to load and fill a nested template pulled
from a fixed given folder, and setting it up so that it can pass different values for a set of
parameters (props). Since the custom action is implemented in Go, it can take those values passed as
props and bundle them up into a map to be used in the nested template. I don't know the extend to
which this scales, and I clearly have not used it *in anger*, but it's an intriguing design that I
may explore more seriously at some point in the future.

Here's the input control atom, rewritten to take a not only the placeholder text but an explicit
width as parameters:

    <!-- input.tpl -->
    
    <input style="width: {{.width}};" type="text" placeholder="{{.placeholder}}">

Notice that we are not strictly defining a nested template, but a standalone template that can be
used to generate an HTML extract. The two parameters are accessed using the standard template
arguments, where the parameters are accessed via keys `width` and `placeholder`.

Similarly, here is the button atom, a variant of the nested template implementation:

    <!-- button.tpl -->
    
    <button> {{.label}} </button>

Again, not a nested template, and the parameter is accessed via key `label`.

To show how we can use such atoms, here is the search box molecule:

    <!-- search-box.tpl -->
    
    <div style="display: flex; flex-direction: row;">
      {{component "input" "placeholder" .placeholder "width" .width}}
      {{component "button" "label" "Search"}}
    </div>

No nested template here. And the atoms are inserted into the template using the new custom action
`component`, which takes as a first argument the name of the template to insert (found in the
current folder by appending `.tpl`), and the remaining arguments are pairs of a key name and a value
associated with that key. 

And that's it. We can use the molecule on a page in exactly the same way as use an atom in a
molecule, by invoking `component`:

    <!-- page.tpl -->
    
    <html>
      <head>
      </head>
      <body>
    
        <p>
          A sample molecule:
        </p>
    
        {{component "search-box" "placeholder" "Enter keywords" "width" "100px"}}
    
      </body>
    </html>

Here is the code to parse and fill a template, including an implementation of the `component` action
through the Go function `ComponentAction`:

    // atomic2.go
    
    package main
    
    import (
        "os"
        "strings"
        "fmt"
        "html/template"
    )
    
    func main() {
        args := os.Args
        if len(args) != 2 {
            panic(fmt.Sprintf("Usage: %s <template>", args[0]))
        }
        tpl_src := template.New(args[1])
        
        // Register the component action.
        tpl_src = tpl_src.Funcs(template.FuncMap{"component": Component})
        
        // Parse the template with the component action available.
        tpl, err := tpl_src.ParseFiles(args[1])
        if err != nil {
            panic(err)
        }
        
        // Invoke the template.
        var b strings.Builder
        if err := tpl.Execute(&b, nil); err != nil {
            panic(err)
        }
        
        // Output the result.
        result := b.String()
        fmt.Println(result)
    }
    
    func Component(comp string, args ...string) template.HTML {
        // The implementation of the component action.
        
        var b strings.Builder
        
        // Load the component template from the current folder.
        tpl_nested := template.New(comp + ".tpl")
        
        // Register the component action so that we can recursively use other components.
        tpl_nested = tpl_nested.Funcs(template.FuncMap{"component": Component})
        
        // Parse the component template.
        tpl, err := tpl_nested.ParseFiles(comp + ".tpl")
        if err != nil {
            panic(err)
        }
        
        // Construct the key-value map to pass to the component.
        argsMap := make(map[string]string)
        if len(args) %2 != 0 {
            panic("Need an even number of arguments")
        }
        for i := 0; i < len(args); i += 2 {
            key := args[i]
            value := args[i + 1]
            argsMap[key] = value
        }
        
        // Fill the template using the key-value map we constructed.
        if err := tpl.Execute(&b, argsMap); err != nil {
            panic(err)
        }
        
        // Return the HTML corresponding to the resulting component.
        return template.HTML(b.String())
    }

The code is very similar to that for nested templates, except that instead of passing all the
relevant templates to the templates parser, we only pass the one template, after registering the new
action. The code for the action gets as inputs the arguments passed to the action in the template,
and from those it first loads the component template from the current folder, then registers the
component action in this new template (so that this template can itself use other components), 
parses the template, creates a key-value map out of the rest of the arguments to the action, then
finally executes the template using that map. The result is HTML code that is returned from the
action, and injected into the original template.

Exercise for the reader: modify the above to pull the component templates from a folder of library
component templates instead of the current folder.

Here's a sample execution:

    $ go run atomic2.go page.tpl
    <html>
      <head>
      </head>
      <body>
    
        <p>
          A sample molecule:
        </p>
    
          <div style="display: flex; flex-direction: row;">
          <input style="width: 100px;" type="text" placeholder="Enter keywords">
    
          <button> Search </button>
    
      </div>
    
      </body>
    </html>

So this idea works. It's pretty rough around the edges, the way of passing props feels like a hack,
and it is not entirely clear how to handle container components — that is, components whose props
are not values like strings and integers, but other components. But you could definitely build
something usable out of this.

Perhaps unsurprisingly, the component-loading action is entirely compatible with the [static
web site generator I wrote](/blog/post/static-web-site-generation/) to maintain this website. I have
no real plan of adding a component system to that generator, but perhaps when I decide to redesign
mmy website I may revisit this idea and see if it's useful.

