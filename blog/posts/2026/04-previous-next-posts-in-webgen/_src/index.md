---
title: Previous/Next Posts In WebGen
date: 2026-04-11
reading: Arthur & George (by Julian Barnes)
prevTitle: Remembering Joe Halpern
prevUrl: ../../2026/03-remembering-joe-halpern
---
        
Two features have been missing from my static site generator WebGen (the one I've created to generate the site you're currently reading): connecting blog posts together with _Previous post_ and _Next post_ buttons, and tagging blog posts. This post is about the first feature, which I implemented recently. I'm going to work on tagging next and describe that in a future post.

To a first approximation, linking posts through _Previous_ and _Next_ buttons is pretty straightforward: each post needs the concept of the post that precedes it and the post that follows it. The only challenge is how to integrate it with the philosophy of the broader static site generator in such a way that I do not need to manually update two other posts when I upload a new post. 

If you dig into my [original descriptions of WebGen](/blog/posts/2023/static-web-site-generation-2/) you'll find that the tool is based on _cascading_ transformations. The core transformation consists of combining `.content` files in `_src/` directories along with templates to create the final `.html` files. Additional transformations can be defined to transform files of type _X_ into files of type _Y_ in those `_src` directories, for various instances of `X` and `Y`. For example, transforming a `.md` markdown file into a `.content` file, etc. The cascade means that an `.md` file gets transformed into a `.content` file, which itself gets converted into an `.html` file. If there was another transformation to take a file of type `Z` into an `.md` file, then that transformation would naturally lead to a file of type `Z` ultimately being converted into an `.html` file via the obvious sequence of transformations `Z` ⟶ markdown ⟶ content ⟶ html. If you've ever used [makefiles](https://www.gnu.org/software/make/manual/html_node/Introduction.html) then none of this is new.

Blog posts are [handled in the same way](/blog/posts/2023/static-web-site-generation-3/). A preliminary transformation takes all posts stored in subdirectories of `/blog/_src/` (where each such subdirectory contains a `_post.md` file with the content of the post in markdown format) and copies all those subdirectories into `/blog/posts/` subdirectories, creating in each a `_src/` subdirectory containing an `index.md` file filled with the content of the original `_post.md` file. Once that transformation is done, the normal markdown ⟶ content ⟶ html cascade kicks in, turning the markdown `index.md` post into a `index.html` website page containing the text of the post.

Two things are key for my support of _Previous_ and _Next_ buttons on each post:

- There is a loop in the original "copy posts to `/blog/posts/`" phase that loops through every post subdirectory in `/blog/_src/` in order to copy them to `/blog/posts`.

- Each `_post.md` file (which becomes `_src/index.md` in each target post directory) uses _metadata_ to handle information such as title and publish date to show appropriately on the post page; for example, this is the metadata info for the current post you're reading:

        ---
        title: Previous/Next Posts In WebGen
        date: 2026-04-11
        reading: Arthur & George (by Julian Barnes)
        ---

The markdown ⟶ content transformation knows how to take that metadata and inject it into the content file, controlled via a `MARKDOWN.template` template file.

Why is this important? Well, creating the `.content` file for each post is done separately for each post from the markdown file of the post via the markdown ⟶ content transformation. Since the _Previous_ and _Next_ button need to be created in each `.content` file, they must already be present in each individual markdown file. But a markdown file by itself doesn't know which post comes before or after it. That information is only known when we loop over all posts in the `/blog/_src` directory. We somehow need to put that information into the markdown file that gets created during the "copy posts to `/blog/posts`" loop. 

This leads to the following algorithm to perform the "copy posts to `/blog/posts/`" phase: do a first scan to accumulate the posts _in order_ so that we can determine for each posts which precedes it and which follows it, then loop over all posts to copy the individual markdown files in the appropriate `_src/` subdirectories in `/blog/posts/`, making sure to add new metadata field in each markdown file with links to the previous and next posts. More precisely:

1. Collects all the posts in one list, ordered by date, most recent first.
2. Go through every `_src/{x}.posts/{y}` directory, copy them to their destination folder in `/blog/posts/`, adding the previous and next post information from the collected posts list.

(A final third step involves using the collected posts list to create a table of content (summary) page with all the post titles. This is independent of any _Previous_ and _Next_ buttons handling.)

To show the buttons on a post page, the `MARKDOWN.template` is adjusted to take the new fields into account:

    <main>
      <article class="post">
      
        <h1 class="title">{{ .Title }}</h3>
        <div class="date">{{ .FormattedDate }}</div>
        <div class="body">
          {{ .Body }}
        </div>
        <div class="human">
          This post was written entirely by a human.
        </div>
        {{if .Reading}}
          <div class="reading">{{ .Reading }}</div>
        {{end}}

        <hr>

        <div class="navigation">
        {{if .PrevUrl}}
          <div class="previous">
            <span>&lsaquo; Previous</span>
            <a href="{{.PrevUrl}}">{{.PrevTitle}}</a>
          </div>
        {{else}}
          &nbsp;
        {{end}}

        {{if .NextUrl}}
          <div class="next">
            <span>Next &rsaquo;</span>
            <a href="{{.NextUrl}}">{{.NextTitle}}</a>
          </div>
        {{else}}
          &nbsp;
        {{end}}
        </div>

      </article>
    </main>

(It's the `div class="navigation"` bit...)

And that's about it. All the fun is is in the "copy posts to `/blog/posts/`" phase.

Here's the full code to process posts, if you are curious. It splits neatly into the three steps above: collect posts, loop through post directories to copy posts to their final target subdirectory, and build the table of content (summary) page.

    func ProcessPostFiles(rootPath, srcDir string, force bool) error {
        srcPath := filepath.Join(rootPath, srcDir)
        
        // 1. Find all posts folder
        postsDirs, err := findAllPostsDirectories(srcPath)
        if err != nil {
            return fmt.Errorf("cannot read _src directory: %w", err)
        }
        if len(postsDirs) == 0 {
            // No posts directories - skip.
            return nil
        }
        // Get full list of posts.
        posts, err := extractAllPosts(srcPath, postsDirs)
        if err != nil {
            return err
        }
        if len(posts) == 0 {
            // Nothing to do.
            return nil
        }
        sort.Sort(byDate(posts))
        // Patch previous/next.
        // Remember, we're going from recent to old!
        for i, p := range posts {
            if i > 0 {
                pp := posts[i-1]
                p.NextUrl = filepath.Join("..", "..", fmt.Sprintf("%d", pp.Year), pp.EntryName)
                p.NextTitle = pp.Title
            }
            if i < len(posts)-1 {
                pp := posts[i+1]
                p.PrevUrl = filepath.Join("..", "..", fmt.Sprintf("%d", pp.Year), pp.EntryName)
                p.PrevTitle = pp.Title
            }
        }
        postPath := filepath.Join(rootPath, POSTDIR)
        anyPostUpdated := false
        
        // 2. Copy post folders.
        // ...Skipped... code to clear out /post folder completely.
        for _, p := range posts {
            srcKey := filepath.Join(fmt.Sprintf("%d.posts", p.Year), p.EntryName)
            tgtKey := filepath.Join(fmt.Sprintf("%d", p.Year), p.EntryName)
            // Copy content of folder YYYY.posts/entry-name.
            // Do not copy subfolders.
            posture's, err := os.ReadDir(filepath.Join(srcPath, srcKey))
            if err != nil {
                log.Printf("error: %s\n", err)
                continue
            }
            srcPaths := make([]string, 0, len(postEntries))
            dstPaths := make([]string, 0, len(postEntries))
            for _, f := range postEntries {
                if !f.IsDir() {
                    srcPath := filepath.Join(srcPath, srcKey, f.Name())
                    dstPath := filepath.Join(postPath, tgtKey, f.Name())
                    if f.Name() == POSTMD {
                        dstPath = filepath.Join(postPath, tgtKey, SRCDIR, "index.md")
                    }
                    srcPaths = append(srcPaths, srcPath)
                    dstPaths = append(dstPaths, dstPath)
                }
            }
            needsUpdating, err := isAnyFileNeedsUpdating(srcPaths, dstPaths)
            if err != nil {
                return err
            }
            // Check if metadata changed!
            indexPath := filepath.Join(postPath, tgtKey, SRCDIR, "index.md")
            metadata, _, err := readMarkdownFile(indexPath)
            if err != nil && !errors.Is(err, os.ErrNotExist) {
                return fmt.Errorf("cannot access index.md file: %w", err)
            } else if err == nil {
                // Index file exists.
                needsUpdating = needsUpdating || (metadata.PrevTitle != p.PrevTitle)
                needsUpdating = needsUpdating || (metadata.PrevUrl != p.PrevUrl)
                needsUpdating = needsUpdating || (metadata.NextTitle != p.NextTitle)
                needsUpdating = needsUpdating || (metadata.NextUrl != p.NextUrl)
            }
            if !needsUpdating {
                continue
            }
            if !anyPostUpdated {
                // First post we're updating, so show the header.
                log.Println(rootPath)
                anyPostUpdated = true
            }
            for _, f := range postEntries {
                if !f.IsDir() {
                    srcPath := filepath.Join(srcPath, srcKey)
                    srcName := f.Name()
                    dstPath := filepath.Join(POSTDIR, tgtKey)
                    dstName := f.Name()
                    if err := os.MkdirAll(filepath.Join(postPath, tgtKey), 0755); err != nil {
                        log.Printf("error: %s\n", err)
                        continue
                    }
                    // Do something different for the main post file - add next/previous metadata.
                    if f.Name() == POSTMD {
                        if err := os.MkdirAll(filepath.Join(rootPath, dstPath, SRCDIR), 0755); err != nil {
                            log.Printf("error: %s\n", err)
                            continue
                        }
                        metadata, body, err := readMarkdownFile(filepath.Join(srcPath, srcName))
                        if err != nil {
                            log.Printf("error: %s\n", err)
                            continue
                        }
                        metadata.PrevTitle = p.PrevTitle
                        metadata.NextTitle = p.NextTitle
                        metadata.PrevUrl = p.PrevUrl
                        metadata.NextUrl = p.NextUrl
                        dst := filepath.Join(rootPath, dstPath, SRCDIR, "index.md")
                        err = writeMarkdownFile(dst, metadata, body)
                        if err != nil {
                            log.Printf("error: %s\n", err)
                            continue
                        }
                    } else {
                        fsrc, err := os.Open(filepath.Join(srcPath, srcName))
                        defer fsrc.Close()
                        if err != nil {
                            log.Printf("error: %s\n", err)
                            continue
                        }
                        fdst, err := os.Create(filepath.Join(rootPath, dstPath, dstName))
                        defer fdst.Close()
                        if err != nil {
                            log.Printf("error: %s\n", err)
                            continue
                        }
                        if _, err := io.Copy(fdst, fsrc); err != nil {
                            log.Printf("error: %s\n", err)
                            continue
                        }
                    }
                }
            }
            log.Printf("  => %s\n", filepath.Join(POSTDIR, tgtKey))
        }
        
        // 3. Construct summary.
        // ...Skipped...
        
        return nil
    }

The full code is in this file in the git repository: [`internal/core/posts.go`](https://github.com/rpucella/webgen/blob/main/internal/core/posts.go).
