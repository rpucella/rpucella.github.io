---
title: Static Web Site Generation (Part 2)
date: 2023-05-02
reading: Mantissa (by John Fowles)
---

*Previous post in the series: [Static Web Site Generation (Part 1)](../static-web-site-generation)*.

In my last post, I talked about `webgen`, the tool I wrote to maintain this website. It is a
fairly conventional static web site generator, based on the simplest of all ideas: HTML templates
that can be filled with HTML content. Writing it from scratch has been a good exercise and gave me a
tool perfectly suited to my workflow that I can modify however and whenever said workflow changes.

A quick recap: the tool assumes that a web site is structured as a folder hierachy containing the
HTML files to be served statically through a standard web server. Some of those HTML files are
pre-written files, while others are generated from content and HTML templates. Site generation is
done before the web site is deployed, so that only static files are served by the web server.

The tool scans all folder in the hierarchy and for any folder `F` with a `__src` subfolder containing a
file `foo.content` holding some HTML content it generates a file `foo.html` in folder `F` by inserting
`__src/foo.content` into an HTML template. That HTML template is found by searching the parent folders
of `F` for a template `__src/CONTENT.template`. Templates can be stored at any levels of the folder
hiearchy, and the template in the nearest enclosing folder is used.

This means, for example, that you can write a site template capturing the structure of all the
pages of the final website (header, footer, navigation bar, layout, CSS), that you can write the HTML
content of the various pages within those various content files, and that you can then generate the
final website by simply injecting each content file into the site template to obtain the final pages.

Moreover, and this is the point of this post, there is no reason why content used to generate the
final pages need to be HTML. It is easy enough to generate HTML from markup languages such as [Markdown](https://daringfireball.net/projects/markdown/) or
[Org files](https://orgmode.org/). The approach that `webgen` takes is to *cascade* content generation. For Markdown, for
example, there is a Markdown-to-HTML translator in `webgen` that knows how to translate Markdown files
into HTML content files that can be then be processed using the HTML content generation described
above as though they were original content files. There is no difference between generated content files and original content files. This compositionality was an important design criterion for `webgen`.

More specifically, every `__src/foo.md` file gets translated into a `__src/foo.content` HTML file in the
same source folder. The implementation uses the library [Blackfriday](https://github.com/russross/blackfriday) for parsing Markdown and
generating the HTML. The translation from Markdown to HTML is completely standard: headers get
translated into `<h1>`, `<h2>`, ..., italics into `<i>`, bold into `<b>`, lists into `<ul>`, etc.

A Markdown-specific HTML template can additionally be used to mediate the translation from Markdown
to HTML. The tool searches for a template `__src/MARKDOWN.template` by walking up the folder
hierarchy from the source folder of any Markdown file, in the same way as it searches for a template
`__src/CONTENT.template`. If it finds such a Markdown template file, the HTML obtained by
translating the Markdown file is inserted into the template to create the final content file.

The implementation supporting this process is a simple extension of what's already available. Recall
from last time that the tool does a recursive walk over the folder hierarchy of the web site to
generate `foo.html` from every `__src/foo.content` it finds.  To handle Markdown translation, the tool
does a preliminary recursive walk to translate every Markdown file into a content file before. Thus,
given the following hierarchy:

    root/
        __src/
            MARKDOWN.template
        C/
            __src/
                page-C.md
        D/
            __src/
                MARKDOWN.template
                page-D.md
            
the tool will first generate the following files in the Markdown translation pass:

    root/
        C/
            __src/
                page-C.content
        D/
            __src/
                page-D.content

using template `root/__src/MARKDOWN.template` for `page-C.md` and `root/D/__src/MARKDOWN.template` for
`page-D.md`. The subsequent recursive walk will generate `page-C.html` and `page-D.html` in the usual way:

    root/
        C/
            page-C.html
        D/
            page-D.html

The underlying walk-and-translate algorithm is straightforward:

    for every folder F:
       if F/__src/ exists:
          for all files F/__src/C.md:
              transform C.md into some HTML content
              if there is a nearest MARKDOWN.template file T:
                 insert HTML content into T to update HTML content
              write HTML content to F/__src/C.content

In the tool, the above algorithm is contained in function `WalkAndProcessMarkdowns()`, invoked before
`WalkAndProcessContents()` from last time and working very similarly:

    func WalkAndProcessMarkdowns(root string) {
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
    		if isGenPosts(path) {
    			return fs.SkipDir
    		}
    		ProcessFilesMarkdown(cwd, path)
    		return nil
    	}
    	if err := filepath.WalkDir(root, walk); err != nil {
    		rep.Fatal("ERROR: %s\n", err)
    	}
    }

(Variable `GENDIR` abstracts the name of source folder `__src` in case I want to change it in the future.) 

A function `ProcessFilesMarkdown()` does the bulk of the work of translating all the Markdown files in
a `__src` folder in a way similar to `ProcessFilesContents()` for content files:

    func ProcessFilesMarkdown(cwd string, path string) {
    	gdPath, err := identifyGenDirPath(path)
    	if err != nil {
    		return
    	}
    	entries, err := os.ReadDir(gdPath)
    	if err != nil {
    		// if we can't read GENDIR, skip.
    		return
    	}
    	for _, d := range entries {
    		if !d.IsDir() && isMarkdown(d.Name()) {
    			relPath, err := filepath.Rel(cwd, gdPath)
    			if err != nil {
    				relPath = gdPath
    			}
    			target := filepath.Join(relPath, targetFilename(d.Name(), "md", "content"))
    			w, err := os.Create(target)
    			if err != nil {
    				w.Close()
    				rep.Printf("ERROR: %s\n", err)
    				continue
    			}
    			if err := ProcessFileMarkdown(w, filepath.Join(relPath, d.Name())); err != nil {
    				w.Close()
    				rep.Printf("ERROR: %s\n", err)
    				continue
    			}
    			rep.Printf("  wrote %s", target)
    			w.Close()
    		}
    	}
    }

    func ProcessFileMarkdown(w io.Writer, fname string) error {
    	rep.Printf("%s\n", fname)
    	md, err := ioutil.ReadFile(fname)
    	if err != nil {
    		return err
    	}
    	metadata, restmd, err := ExtractMetadata(md)
    	if err != nil {
    		return err
    	}
    	output := blackfriday.Run(restmd, blackfriday.WithNoExtensions())
    	tpl, tname, err := FindMarkdownTemplate(fname)
    	if tpl != nil {
    		rep.Printf("  using markdown template %s\n", tname)
    		result, err := ProcessMarkdownTemplate(tpl, metadata, template.HTML(output))
    		if err != nil {
    			return err
    		}
    		output = []byte(result)
    	}
    	if _, err := w.Write(output); err != nil {
    		return err
    	}
    	return nil
    }
    
(Clearly, that both the recursive walk to find Markdown files and the code to process Markdown files
parallel the structure of generating HTML from content files suggests that there's an abstraction
here that I can isolate. I'll work on that kind of refactoring the next time I touch this code
base.)

The actual Markdown-to-HTML translation is achieved with a call to `blackfriday.Run()` in the
[Blackfriday](https://github.com/russross/blackfriday) library. Functions `FindMarkdownTemplate()` and `ProcessMarkdownTemplate()` are used
to find a `__src/MARKDOWN.template` and to insert the produced HTML into such a template, respectively. 

One additional detail worth mentioning is that I allow Markdown files to have YAML-style metadata at
the top of the file, such as:

    ---
    title: Static Web Site Generation (Part 1)
    date: 2023-03-23
    ---

This metadata can be used in a Markdown template, although the implementation is not yet generic: I
support only a few fields, such as `title` and `date` as above, mostly dedicated to the creation of blog
posts. I will most likely talk about blog support in `webgen` in a future post. Markdown metadata is
read using a function `ExtractMetadata()`.

And that's about it. Clearly the above generalizes to other markup formats, as long as you can
define an HTML translator for those formats.

In a precise sense, `webgen` works bottom-up: it first transforms every Markdown file it finds into a
content file in a first pass, and then transforms every content file it finds into an HTML file in a
second pass. It is easy to add more steps to the cascade. For instance, I explored the idea of
adding LaTeX-style mathematical markup to Markdown documents to help writing mathematical posts. So
as not to reinvent the Markdown-translation wheel, or hack the underlying Markdown translation
library, the easiest way to support this kind of extended Markdown is to first translate the
extended Markdown file into a normal Markdown file in which the mathematical markup has been
translated to HTML, leaving all standard Markdown untouched. This latter Markdown file can then be
translated to HTML using the process described above, taking advantage of the fact that the
Blackfriday library leaves HTML markup in a Markdown file untouched during translation. (You can see the result on this experiment on these <a href="/courses/dsa-fa22/notes/analysis1.html">lecture notes on algorithmic analysis</a>. The translation from LaTeX to HTML was done through <a href="https://temml.org/">Temml</a>.)

The current implementation of `webgen` is not smart about generation. It will regenerate all files
whenever it is run, whether their source files have been modified or not since the last
generation. I don't see it as a problem right now because this site is small: generating it from
scratch every times takes only a few seconds. Of course, a more efficient generation process would
be to only regenerate files whose source have changed. I'm leaving this optimization for a future
refactoring over the code.

