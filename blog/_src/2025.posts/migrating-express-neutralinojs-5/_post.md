---
title: Migrating from Express to Neutralinojs, Part 5: Mac OS Bundling
date: 2025-09-27
reading: Haute Démolition (by Jean-Philippe Baril Guérard); Tromper Martine (by Stéphane Dompierre)
---

*Previous post in the series: [Part 4](/blog/posts/2025/migrating-express-neutralinojs-4)*.

In previous posts in this series, I migrated a web app with an Express-based backend to
[Neutralinojs](https://neutralino.js.org/), a desktop application platform based on web technologies, as an easy way to turn it into 
a desktop app. Neutralinojs can bundle together a web-based frontend with a dedicated
web renderer to create a native desktop app, _à la_ [Electron](https://www.electronjs.org/).

Neutralinojs is basically platform independent. The code for the app can be _built_ to produce an
executable for any common platform (Windows, Mac OS, Linux) that when run will open a web renderer
and render the web-based frontend bundled into the app.

Today, I want to focus specifically on the Mac OS executables that Neutralinojs produces.  And
that's for two reasons.  The first is pragmatic: that's the system I use. The second is more
interesting: there are two "kinds" of Mac OS executables: command-line executables and application
bundles.  A command-line executable is just what it says on the tin — it is an executable that you
can run from a terminal window.  An [application bundle](https://developer.apple.com/library/archive/documentation/CoreFoundation/Conceptual/CFBundles/BundleTypes/BundleTypes.html#//apple_ref/doc/uid/10000123i-CH101-SW1) is just a directory with a specific structure
and which contains a command-line executable and an icon. The Mac OS Finder recognizes and shows
such an application bundle as a standalone icon that can be double-clicked to execute the underlying
command-line executable. The executable within the bundle naturally has access to any of the support
files included in the bundle. Application bundles are the main distribution mechanism for Mac OS
applications.

When you build a Neutralinojs desktop app, the result is a command-line executable.  Ideally, on Mac OS, we
should wrap the executable into an application bundle so that the desktop app doesn't need to be run from the command
line.  Additionally, Neutralinojs desktop apps can come with extensions, which are supporting
executables providing access to OS resources what Neutralinojs supports natively, and those extensions can
be included in the application bundle.  

So the question becomes: how do we go about bundling a Neutralinojs desktop app into an application
bundle?


## Mac OS Bundling

Mac OS application bundling is a bit of a dark art. [XCode](https://developer.apple.com/xcode/) (Apple's development suite) comes with an
application bundler as part of its build process.  If you're not using XCode to create a Mac OS
application, however, it looks like there's no support.  I could not find a standalone application bundle
creator, at least not one that could be considered off the shelf.  (Full disclosure: I did not
search very long or very wide.)

I did find a GitHub repository with the pithy name [Neutralino Build-Automation for macOS, Linux and
Windows App-Bundles](https://github.com/hschneider/neutralino-build-scripts).  And it turns out to do the job just fine.  It consists of a set of shell
scripts that plug into a Neutralinojs app code base and let you build a Mac OS application bundle
following the build of the Neutralinojs command-line executable.  There seems to be support for
so-called Linux and Windows application bundles as well, but I have not used or evaluated these.

The rest of this post is going to be an illustration of how the Build-Automation tool is used to create an application bundle for the sample web app we have migrated to Neutralinojs in the rest of this series. As usual, all the code is in the [Github repository](https://github.com/rpucella/neutralino-testbed).  The code specifically for today — the bundled version of the desktop app — is in the `6-neutralino-app/` directory. Let's take a look at the content first. I'll describe how I got there afterward.

    image-viewer/
      resources/
        index.html
        js/
          neutralino.js
        icons/
         appIcon.png
      backend/
        backend.go
        go.mod
        go.sum
      neutralino.config.json
      
      build-mac.sh
      preproc-mac.sh
      postproc-mac.sh
      _app_scaffolds/
        win
        mac
        linux
  
The structure of the directory is the same as [last time](/blog/posts/2025/migrating-express-neutralinojs-4), with additional files tossed in from the Build-Automation repository.

The crux of the bundle builder (say _that_ three times fast...) are the three `.sh` shell scripts and the `_app_scaffolds/` directory. All of which comes from the Build-Automation installation.

To build the bundle, first we build the Neutralinojs executable via the usual 

    npx @neutralinojs/neu run 
    
(or the `npm` version if you installed `@neutralinojs/neu` locally). Remember, this will drop a command-line executable in `dist/image-viewer`. Then we run `./build-mac.sh` to bundle the just-created executable into an application bundle. The result is in `dist/` as well, classified by system architecture:

    dist/
      mac_arm64/
        ImageViewer.app/
          ...
      mac_universal/
        ImageViewer.app/
          ...
      mac_x64/
        ImageViewer.app/
          ...

The various `ImageViewer.app` _are_ the application bundles. Opening `dist/` in the Mac OS Finder will show these as double-clickable icons like any other app. And double-clicking them indeed opens our picture viewing desktop app. Success!

So, how did I populate the directory above? How did I install the Build-Automation tool? Short answer: lots of trial and error, and some luck. I'm also not entirely sure I'm doing it right, but then again I _am_ creating an application bundle at the end, so I cannot be _that_ wrong. Right?

What is this reproducible flow? How did I set up the `6-neutralino-app/` directory?

Step 1: I copied over the structure in `5-neutralino-go/`, the version of the Neutralinojs desktop app I described in the [last article](/blog/posts/2025/migrating-express-neutralinojs-4) of this series. 

Step 2: I downloaded a zipped version of the [Build-Automation repository](https://github.com/hschneider/neutralino-build-scripts) into the root directory of the Neutralinojs project, `6-neutralino-app/image-viewer/`. I could also have just cloned it, but a git repository embedded inside another git repository is sometimes problematic, so I went the zip file route instead.

Step 3: I created a subdirectory `unused-build_scripts/` and moved everything from Build-Automation that I did not need into it, keeping only `_app_scaffolds/` and the three `mac-XXX.sh` files in the `6-neutralino_app/image_viewer/` directory.

Step 4: I added an entry to `neutralino.config.js` to tell the Build-Automation scripts how to build the application bundle:

    "buildScript": {
      "mac": {
        "architecture": ["x64", "arm64", "universal"],
        "minimumOS": "10.13.0",
        "appName": "ImageViewer",
        "appBundleName": "ImageViewer",
        "appIdentifier": "net.rpucella.neutralino_testbed.imageviewer",
        "appIcon":  "resources/icons/appIcon.png"
      }
    }

Presumably, if you wanted to create bundles for Windows or for Linux, you'd add a corresponding section to the `buildScript` entry.

And that's _almost_ it. If you were to run `./build-mac.sh` at this point, and assuming that the command-line executable had been built, then an application bundle would get created. It wouldn't run correctly though. Because our Neutralinojs app uses an extension, and we haven't accounted for it in the bundle.


## What About Extensions?

Extensions are the mechanism by which Neutralinojs apps can communicate with the underlying OS to
perform OS-specific operations: accessing the file system, accessing databases, communicating with
other applications, and so on. Neutralinojs itself provides built-in APIs that expose some of those
operations to frontends running in the Neutralinojs apps. Extensions are the way to extend those
APIs. An extension is a separate program that connects to the Neutralinojs desktop app via a
WebSocket, and can receive and response to messages from the desktop app.

Last time, we implemented a Go-based extension to provide persistence for our sample picture viewing
app. This had the advantage that the extension could be compiled into a command-line executable. The
Neutralinojs desktop app starts this extension when it launches, and shuts it down when it
quits. The extension's executable is `backend/bin/backend`.

In order for us to create a complete application bundle for the picture viewing app, we need to
bundle the extension executable alongside the Neutralinojs executable.

More specifically, we need to do two things: we need to copy the extension executable into the
application bundle, and then we need to register the command to start the extension in a way that
the Neutralinojs app can find it. Note that the command to start the extension's executable will
change based on where we put that executable in the application bundle.

Let's first take a look at the structure of the `ImageViewer.app` application bundle created by the
Build-Automation tool:

    ImageViewer.app/
      Contents/
        Info.plist
        MacOS/
          ...
        Resources/
          ...

[Apple's documentation](https://developer.apple.com/documentation/bundleresources/placing-content-in-a-bundle) states that any helper executable (which an extension qualifies as) should be
placed in the `Contents/MacOS/` or `Contents/Helpers/` subdirectories of the bundle. I chose to place
the extension in `Contents/MacOS` alongside the Neutralinojs executable.

How do we copy the extension executable into the bundle? The Build-Automation tool provides a hook:
we can edit the `postproc-mac.sh` shell script which is designed to be run by `build-mac.sh` after the
application bundle created to perform any post-build update. This is the bit I added to that script:

    cp backend/bin/backend "${APP_MACOS}/"

The Build-Automation scripts set environment variable `APP_MACOS` to the `Contents/MacOS` subdirectory
of the constructed application bundle during execution of the `build-mac.sh` script chain.

How do we tell Neutralinojs the command to use to start the extension? That information is held in `neutralino.config.json`, just like before.  I modified the `extensions` entry of `neutralino.config.json` as follows:

    "extensions": [
      {
        "id": "imageviewer_backend",
        "command": "${NL_PATH}/../MacOS/backend"
      }
    ]
    
This entry specifies that our Neutralinojs app has one extension that can be by running `${NL_PATH}/../MacOS/backend`. 
When running within an application bundle, `NL_PATH` seems to hold the `Contents/Resources/` subdirectory. Hence, I use it to construct a relative path to the `Contents/MacOS` directory where I put the `backend` extension executable.

After these two changes, running `build-mac.sh` produces an application bundle that can indeed be
executed by double-clicking its icon, starting both the Neutralinojs app and the supporting extension. Bottom line: it works. And the application bundle can be moved anywhere else once it is created.

One minor annoyance remains: since I changed the `neutralino.config.json` file to account for where the extension executable lives in the application bundle, this prevents the Neutralinojs app from running correctly either from the command line or via `npx @neutralinojs/neu run`. I suspect there is a clean solution to that problem, but I have not really investigated what it might look like. Perhaps just replicating the application bundle structure within the `6-neutralino-app/` directory might do the trick. That's okay. Something to explore in the future.

