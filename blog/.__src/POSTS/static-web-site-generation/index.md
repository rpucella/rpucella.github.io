---
title: Static Web Site Generation (Part 1)
date: 2023-03-23
reading: Daniel Martin (by John Fowles)
---

Creating a static web site generator seems to have become a rite of passage. Perhaps it always has been?

A static web site generator is a tool for taking some representation of the content of a website,
and generating from it a set of static web pages in a way that can be served by an off-the-shelf web
server, generally using simple file-based routing.

There is no shortage of static web site generators out there.  [GitHub supports Jekyll](https://docs.github.com/en/pages/setting-up-a-github-pages-site-with-jekyll/about-github-pages-and-jekyll) out of the
box. Friends have successfully used [Hugo](https://gohugo.io/). I've used [Gatsby](https://www.gatsbyjs.com/) myself for a work project before
switching to [Next.js](https://nextjs.org/docs/advanced-features/static-html-export). There's a lot of great technology out there for static web site generation.

Yet, here I am, maintaining my website using a DIY static web site generator. It's the age-old
story. My website has always been a set of static pages hosted on GitHub pages, because I wanted it
to be low maintenance. But eventually I wanted a consistent navigation bar atop my site across these
static pages.  Using Javascript for that felt like overkill.  So I cooked up a small script to add
the navigation bar to every page.

The next natural step was to generalize the script to control where the banner went on a page. From
there, it was a small step to use a page layout as a template that would get filled by the content
specific to any given page of the site. Once you have the notion of a page layout, it is another
small step to support different page layouts for different part of the site. And once you have the
page content separated from the layout of the page, well, that page content can easily be written in
some other format and converted to HTML.  Which is the first step in supporting content written as
posts for a blog hosted on the site, doesn't it? The kind of blog post like the one you're currently reading.  

Yes, I know. I simply recreated the exact reasoning that went into the creation of these static web
site generators I just mentioned.  I am not claiming originality here. But it's been a fun and
natural process, and I ended up with something that feels perfectly adapted to my current workflow.
My dependencies are minimal and I spend no time maintaining the tool.

I am of course [not alone in this approach](http://jonashietala.se/blog/2022/08/29/rewriting_my_blog_in_rust_for_fun_and_profit/).  It's an easy way to experiment with site generation.
Designers seem to [feel the same](https://ethanmarcotte.com/wrote/let-a-website-be-a-worry-stone/) about their own website.

This post (and upcoming ones) will dig into the details of my tool, in case you're curious.

# The `webgen` tool

The tool, called `webgen`, is built around two ideas: 

- templates living at multiple levels of the folder hierarchy;
- dependency-based content generation. 

These are obviously not new ideas, but finding a balance between simplicity and usefulness has been
entertaining.

The tool is implemented in [Go](https://go.dev/), my preferred programming language for personal projects for the last
year. It is statically typed, completely procedural, and the closest to programming in C that I've
felt in a long while — but without the memory management headaches.  That Go compiles to a single
statically-linked executable is a bonus.  The code for the tool is part of the [Git repository of this
website](https://github.com/rpucella/rpucella.github.io), which I recognize is not great for sharing.  (I will likely move it to its own git repository at
some future date, or if there's a need to it. **UPDATE**: [done](https://github.com/rpucella/webgen)) This website as a whole in fact serves as an example
of using `webgen` to create a static web site.

The idea of templates living at multiple levels of the folder hierarchy is simple. You create your
static website as usual, with possibly nested folders of HTML, CSS, and Javascript files laid out
however you want them. Any folder can have a *source folder* `__src` that includes content that
`webgen` can use to generate HTML files in addition to those HTML files that are already
present. The folder containing a `__src` folder is called the *anchor folder* of that `__src`
folder.  Any file `xyz.content` (containing an HTML fragment) in a `__src` folder is converted to an
HTML file `xyz.html` in the anchor folder by inserting it into a template. That template is a file
`CONTENT.template` which is found either in the same `__src` folder as the `xyz.content` file or in
a `__src` folder hanging off a parent of the anchor folder. The first template found in a `__src`
folder moving up from the anchor folder is the one that gets used. Thus, a template put in a `__src`
folder at the root of the website folder hierarchy can serve as a generic template used for all
pages, except when overridden by a template in a sub-folder.

Templates are implemented using Go's [`html/template`](https://pkg.go.dev/html/template) package. They are simply HTML documents with a
placeholder of the form `{{.Body}}` that gets replaced by some the content from a `xyz.content` file to
create a file `xyz.html` in the anchor folder of the `__src` folder containing `xyz.content`.

For example, the structure

    root/
        __src/
            CONTENT.template
            index.content
        A/
            __src/
                page-A.content
        B/
            __src/
                CONTENT.template
                page-B.content
            
will generate the following files:

    root/
        index.html
        A/
            page-A.html
        B/
            page-B.html

where `root/index.html` and `root/A/page-A.html` are generated by plugging
`root/__src/index.content` and `root/A/__src/page-A1.content` into template
`root/__src/CONTENT.template` (respectively), while `root/B/page-B.html` is generated by plugging
`root/B/__src/page-B.content` into template `root/B/__src/CONTENT.template`.

The structure of the HTML generation code is straightforward:

    for every folder F:
       if F/__src/ exists:
          find nearest enclosing template T
          for all files F/__src/C.content:
              insert C.content into T and create F/C.html 
    
       
In the tool, the above algorithm is contained in function `WalkAndProcessContents()`, and relies on
package function [`filepath.WalkDir()`](https://pkg.go.dev/path/filepath#WalkDir) to walk the folder hierarchy from a given root path:

    func WalkAndProcessContents(root string) {
        cwd, err := os.Getwd()
        if err != nil {
            rep.Fatal("ERROR: %s\n", err)
        }
        walk := func(path string, d fs.DirEntry, err error) error {
            if err != nil {
                // Error in processing the path - skip.
                return nil
            }
            if !d.IsDir() {
                // Skip over files.
                return nil
            }
            if filepath.Base(path) == ".git" {
                return fs.SkipDir
            }
            if isGenDir(path) {
                // Skip GENDIR.
                return fs.SkipDir
            }
            ProcessFilesContent(cwd, path)
            return nil
        }
        if err := filepath.WalkDir(root, walk); err != nil {
            rep.Fatal("ERROR: %s\n", err)
        }
    }

(Variable `GENDIR` holds the name of source folder `__src`.) 

The above code finds every anchor folder (skipping files) and calls `ProcessFilesContent()` on any
folder `F` to handle HTML file generation in `F` from a content file appearing in `F/__src`. Note
the special cases for `.git` and for `__src` to avoid generating files in `.git/` and in
`__src/`. Clearly, this could and should be generalized into a generic list of folders to skip.

Function `ProcessFilesContent()` does the bulk of the work:

    func ProcessFilesContent(cwd string, path string) {
        genDir, err := identifyGenDir(path)
        if err != nil {
            return
        }
        entries, err := os.ReadDir(filepath.Join(path, genDir))
        if err != nil {
            // if we can't read GENDIR, skip.
            return
        }
        for _, d := range entries {
            if !d.IsDir() && isContent(d.Name()) {
                relPath, err := filepath.Rel(cwd, path)
                if err != nil {
                    relPath = path
                }
                target := filepath.Join(relPath, targetFilename(d.Name(), "content", "html"))
                w, err := os.Create(target)
                if err != nil {
                    w.Close()
                    rep.Printf("ERROR: %s\n", err)
                    continue
                }
                if err := ProcessFileContent(w, filepath.Join(relPath, genDir, d.Name())); err != nil {
                    w.Close()
                    rep.Printf("ERROR: %s\n", err)
                    continue
                }
                rep.Printf("  wrote %s", target)
                w.Close()
            }
        }
    }
    
    func ProcessFileContent(w io.Writer, fname string) error {
        rep.Printf("%s\n", fname)
        tmpl, err := FindTemplate(fname)
        if err != nil {
            return err
        }
        main, err := ioutil.ReadFile(fname)
        if err != nil {
            return err
        }
        current := template.HTML(main)
        rep.Printf("  using template %s\n", tmpl.name)
        c := Content{"", time.Time{}, "", "", current}
        filledTmpl, err := ProcessTemplate(tmpl.template, c)
        if err != nil {
            return err
        }
        bytes := []byte(filledTmpl)
        if _, err := w.Write(bytes); err != nil {
            return err
        }
        return nil
    }

Finding templates is handled by function `FindTemplate()` and inserting content into a template by function `ProcessTemplate()`: 

    type template_info struct {
        template *template.Template
        name     string
    }
    
    func FindTemplate(path string) (template_info, error) {
        // Given a path, find the nearest enclosing CONTENT.template file.
        previous, _ := filepath.Abs(path)
        current := filepath.Dir(previous)
        for current != previous {
            gdPath, err := identifyGenDirPath(current)
            if err == nil {
                tname := filepath.Join(gdPath, TEMPLATE)
                tpl, err := template.ParseFiles(tname)
                if err == nil {
                    result = template_info{tpl, tname}
                    return result, nil
                }
            }
            previous = current
            current = filepath.Dir(current)
        }
        return template_info{}, fmt.Errorf("no template found")
    }

    func ProcessTemplate(tpl *template.Template, content Content) (template.HTML, error) {
        var b strings.Builder
        if err := tpl.Execute(&b, content); err != nil {
            return template.HTML(""), err
        }
        result := template.HTML(b.String())
        return result, nil
    }
    
Nested templates is a straightforward extension of the above. They let an HTML fragment 
`file.content` be injected into a template to create a larger fragment that itself can be injected
into a template further up the folder hierarchy.  Details are left as an exercise to the reader.

And that's it, really. I have some glue code to launch the tool from the command line and helper
functions to simplify the code, but the core is as simple as can be.

The second idea underlying `webgen`, dependency-based content generation, I will address in Part
2, which covers Markdown content.

