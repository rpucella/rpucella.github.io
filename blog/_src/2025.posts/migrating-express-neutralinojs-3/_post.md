---
title: Migrating from Express to Neutralinojs, Part 3
date: 2025-09-06
reading: The Cuckoo's Calling (by Robert Galbraith)
---

*Previous post in the series: [Part 2](/blog/posts/2025/migrating-express-neutralinojs-2)*.

This is the third post in a series about migrating an Express-based web app to [Neutralinojs](https://neutralino.js.org/), a
desktop application platform based on web technologies. Neutralinojs is a more lightweight
alternative to [Electron](https://www.electronjs.org/). More interestingly, Neutralinojs lets you write _extensions_ that can access
OS resources using any programming language that can access a WebSocket.

I chose to illustrate this migration using a simple picture viewing web app. The code is [freely
available on GitHub](https://github.com/rpucella/neutralino-testbed).  The full migration is achieved through a sequence of smaller migrations, each
isolating one interesting aspect of the whole:

- Migrate the Express-based web app with multiple HTTP endpoints to an Express-based web app with a single HTTP endpoint.
- Migrate the Express-based web app with a single HTTP endpoint to an Express-based web app with a WebSocket.
- Migrate from an Express-based web app with a WebSocket to a Neutralinojs app.

I covered the first two steps in [Part 1](/blog/posts/2024/migrating-express-neutralinojs-1) and [Part 2](/blog/posts/2025/migrating-express-neutralinojs-2) of this series. The result of the second step is
an Express-based web app that communicates with the backend server via a single WebSocket. You can
find the code for that version in directory `3-express-websocket/` of the [repository](https://github.com/rpucella/neutralino-testbed).

In this post, I tackle the final migration step to obtain a Neutralinojs desktop app backed by a Nodejs extension.


## Third Migration: Neutralinojs

Neutralinojs is a desktop application platform base on web technologies. What does that mean?  Well,
it is basically a dedicated web browser into which you can bundle a web frontend (HTML, CSS,
Javascript) to create a standalone desktop application that presents the frontend when you run it.
And that's it.  Of course, the web frontend can form a fully-featured single page web app (SPA)
created using any modern web framework (React, Vue, Svelte, dealer's choice). Neutralinojs uses the
OS-supplied web renderer (for instance, WebKit) to achieve its lightweightness.

One challenge to the migration of a web app to Neutralinojs is what to when the web app relies on a server to achieve, for instance, persistence via a database, or to access OS-based services? The simple picture viewing app I use as a running example doesn't do persistence, but it's an easy exercise to add it by saving pictures to the file system, or to a [SQLite](https://sqlite.org/) database. Neutralinojs does offer some internal APIs to interact with
the local file system, but any other kind of interaction needs to be handled by an _extension_. (They could have called it a _plugin_.) An
extension is simply an external application that is started when the Neutralinojs app starts,
and with which the app can be communicate via an [inter-process communication (IPC)](https://en.wikipedia.org/wiki/Inter-process_communication) mechanism. All operations that in a web app are handled by a server will need to be handled by an extension, if those operations are not already available as a Neutralinojs API.

We therefore have two tasks ahead of us to complete the final step of our migration to Neutralinojs:
create the desktop app by suitably modifying our existing frontend code, and transform our existing
server into an extension. Easy peasy.

The resulting code can be found in directory `4-neutralino-node/` of the [repository](https://github.com/rpucella/neutralino-testbed): 

    image-viewer/
      backend/
        backend.js
        package.json
        package-lock.json
      resources/
        icons/
          appIcon.png
        js/
          neutralino.js
        index.html
	  neutralino.config.json

Before I describe the content of the directory, it is worth asking how it was created. A Neutralinojs project is created using the `neutralinojs/neu` npm package. The structure above was created by running the following command in the `4-neutralino-node/` directory:

    npx @neutralinojs/neu create image-viewer
    
This initialized a new Neutralinojs project called `image-viewer` and put in a placeholder `index.html`
file in `image-viewer/resources/`.  Annoyingly, I had to run the above line multiple time because the
initialization often failed.  No clue why.  Something about failing to download the initial
templates.  Once the project was initialized, I added the code for the web app and modified it to
work with the Neutralinojs platform.

An important file in the project is `image-viewer/neutralino.config.json`, which [configures the project](https://neutralino.js.org/docs/configuration/neutralino.config.json/), in roughly the same way that `package.json` configures an npm project. The default settings are reasonable, and the tweaks I had to make are described below when I talk about extensions.

To run the Neutralinojs app, you first need to build the extension that I describe below.  To do so,
go into `image-viewer/backend/` and run `npm install`.  Once the extension is built, the easiest way to run the
Neutralinojs app is to use _dev mode_, by executing

    npx @neutralinojs/neu run
    
from within the `image-viewer/` directory.  This will open the desktop app and show the content of
`image-viewer/resources/index.html`.  The only way to quit the app, right now, is to hit Ctrl-C from
the command line where you started the app.  I know: it's rough.  You can add functionality in the
frontend to quit from a menu option, or something similar, using the Neutralinojs API.  I'll leave
you to figure that part out, if you care.

Let's look at the actual code.  And let's start with the frontend.  It lives in
`image-viewer/resources/`.  The directory `image-viewer/resources/` is taken as the root path for the
front end, so any `<script src="/foo/bar.js">` link will load `image-viewer/resources/foo/bar.js`, and
similarly for CSS files.  This is compatible with the bundle that any web
framework can create.  Theoretically, you could drop a React-based distribution bundle here and it would work similarly.  In fact, that's exactly what I did in the real web app that I migrated and that provided motivation for this series of posts.

I made two changes from the `index.html` we used in [Part 2](/blog/posts/2025/migrating-express-neutralinojs-2), both needed to handle the move from a backend server to a Neutralinojs extension.  First, I added a line in the frontend to load the Neutralinojs API library:

    <script src="/js/neutralino.js"></script>
    
Second, I modified the `_fetch()` method in class `API` to invoke the extension instead of calling a
WebSocket endpoint on the server:

    class API {
      constructor() {
        this._callId = 0
      }

      async _fetch(_, obj) {
        const extension = "imageviewer_backend"
        const event = "eventToExtension"
        // callId is a unique identifier for the call so that we can catch the response.
        const callId = this._callId
        this._callId += 1
        const response = new Promise((resolve) => {
          const listener = (event) => {
            const data = event.detail
            if (data.callId === callId) {
              Neutralino.events.off('eventFromExtension', listener)
              resolve(data)
            }
          }
          Neutralino.events.on('eventFromExtension', listener)
        })
        await Neutralino.extensions.dispatch(extension, event, {...obj, callId})
        const data = await response
        return data.content
      }

      async fetchImages() {
        return this._fetch("/api/message", {
           "mode": "get-images"
        })
      }
        
      async fetchImage(index) {
        return this._fetch(`/api/message`, {
          "mode": "get-image",
          "index": index
        })
      }
        
      async addImage(url) {
        return this._fetch("/api/message", {
          "mode": "post-image",
          "url": url
        })
      }
    }
    
The `_fetch()` method is very similar to what I had last time, with one big difference: Neutralinojs
creates and manages the WebSocket to communicate with the extension and offers an API to send and
receive messages over that WebSocket instead of requiring us to use the WebSocket API directly. (The
API to communicate with extensions needs the name of the extension with which to communicate, since
a Neutralinojs app can use multiple extensions. Every extension gets a name, specified when we
create the extension—see below.)  For example, a function `Neutralino.dispatch()` in the Neutralinojs API
library sends a message to an extension, and a function `Neutralino.events.on()` can be used to assign
an event listener that listens to messages received from the extension.  Aside from this, everything
else is the same, including the use of a `callId` to enforce a request-response protocol over the
WebSocket.
  
The frontend is basically the same.  It's a different story for the backend, though again the
changes are all in how the backend interfaces with the frontend.

The extension is a partial rewrite of the server.  In a standard web app setting, the server is a separate process and is started separately.  In a sense, it controls the web app: a browser connects to the server to retrieve the frontend code, and the frontend can then communicate with the server.  In a Neutralinojs app, an extension is also a separate process, but it is started and is controlled by the app.  How does the app know to start an extension (and how to start it)?  That information is added to the `neutralino.config.json` configuration file.  For our code, I added the following field to the configuration file (after enable extensions by setting field `enableExtensions` to `true`):
    
    "extensions": [
      {
        "id": "imageviewer_backend",
        "command": "node ${NL_PATH}/backend/backend.js"
      }
    ]
    
These lines define an extension called `imageviewer_backend` that gets run when the Neutralinojs app
starts using the command in the `command` field. The `NL_PATH` variable is replaced by the root of the
Neutralinojs app.  The name specified in field `id` identifies the extension to the frontend, and needs to be supplied to the Neutralinojs API library functions that communicate with extensions. 

The extension for this app is in `backend/backend.js`.  Where it lives is largely irrelevant, since
you get to specify the location in the extension execution command. The code of the extension is
similar to the server code from [Part 2](/blog/posts/2025/migrating-express-neutralinojs-2).  The one difference is that instead of being a server that
creates a WebSocket for the frontend to connect to as a client, the Neutralinojs app creates the
WebSocket and the extension is the client.  How does the extension know the address of the WebSocket
to use?  It receives the information at start-up time, as a stringified JSON passed via standard input!  I can't quite decide for myself if this is clever or silly, and the answer is probably _both_
and _it doesn't matter_.  The JSON object sent by Neutralinojs looks like:

    { 
      nlPort: ...
      nlToken: ...
      nlConnectToken: ...
      nlExtensionId: ...
    }
 
Much of this information is passed into the WebSocket connection URL to authenticate the extension
back to the Neutralinojs app, as we'll see in the code below.

I use the `ws` library for creating the WebSocket client. We of course no longer need Express, since we
are not creating a server.  But aside from turning the server into a WebSocket client, the rest of
the code for processing messages over the WebSocket is exactly the same as in the server case. Here's the code:

    import { v4 as uuidV4 } from "uuid"

    import process from "process"
    import fs from "fs"
    const input = fs.readFileSync(process.stdin.fd, 'utf-8')
    const processInput = JSON.parse(input)
    const NL_PORT = processInput.nlPort
    const NL_TOKEN = processInput.nlToken
    const NL_CTOKEN = processInput.nlConnectToken
    const NL_EXTID = processInput.nlExtensionId
    const NL_URL =  `ws://localhost:${NL_PORT}?extensionId=${NL_EXTID}&connectToken=${NL_CTOKEN}`

    const images = []

    class Storage {
        constructor() {
            this.images = []
        }

        readImage(index) {
            return this.images[index]
        }

        readImageNames() {
            return this.images.map(img => img.name)
        }

        createImage(name, content, contentType) {
            this.images.push({
                name: name,
                content: content,
                mime: contentType
            })
        }
    }

    class Controller {
        constructor() {
            this.store = new Storage()
        }

        getImageDetails(key) {
            const img = this.store.readImage(key)
            return `data:${img.mime};base64,${img.content}`
        }

        getImages() {
            return this.store.readImageNames()
        }

        async addImage(url) {
            const response = await fetch(url)
            const contentType = response.headers.get('content-type')
            const abuffer = await response.arrayBuffer()
            const base64Image = Buffer.from(abuffer).toString('base64')
            this.store.createImage(url, base64Image, contentType)
        }
    }

    const controller = new Controller()

    async function processMessage(msg) {
        switch(msg.mode) {
        case "get-image":
            return controller.getImageDetails(msg.index)
            break

        case "get-images":
            return controller.getImages()
            break

        case "post-image":
            await controller.addImage(msg.url)
            return "ok"
        }
        console.log(`Error - unknown message type ${msg.mode}`)
        return "unknown message type"
    }

    // WebSocket client.

    import WebSocket from 'ws'
    const client = new WebSocket(NL_URL)

    client.on('error', (error) => {
        console.log(`Connection error!`)
        console.dir(error, {depth:null})
    })
    client.on('open', () => console.log("Connected"))
    client.on('close', (code, reason) => {
      console.log(`WebSocket closed: ${code} - ${reason}`);
      process.exit()
    })
    client.on('message', async (evt) => {
      const evtData = evt.toString('utf-8')
      console.log("Event = ", evtData)
      const { event, data } = JSON.parse(evtData)
      if (event === "eventToExtension") {
        const callId = data.callId
        const result = await processMessage(data)
        client.send(JSON.stringify({
          id: uuidV4(),
          method: "app.broadcast",
          accessToken: NL_TOKEN,
          data: {
            event: "eventFromExtension",
            data: {content: result, callId}
          }
        }))
     }
    })

Everything above `// WebSocket client`, the core logic of the backend, is as before, aside from
imports and reading the WebSocket information from standard input.

And after this minor rewrite of the frontend and the backend, it works.  It took me maybe a week to
do the migration for my real web app, basically going through the above path with a few missteps
along the way, and quite a bit of banging my head against the wall.  The Neutralinojs documentation has some holes into it, and understanding how to interact with extensions requires peering into sample code more than I expected.   And debugging WebSocket connection errors is its own dedicated circle of Hell.

I clicked pretty late in the process that the WebSocket set up to communicate between the
Neutralinojs desktop app and the extension is basically used as an old-school IPC Unix socket.  In
many ways, the fact that Neutralinojs communicates with extensions via WebSockets is an
implementation detail, hidden underneath the Neutralinojs API.  But it made me curious why an IPC
socket wasn't actually used for this, instead of a WebSocket.  I have no real sense of the efficiency trade-offs between IPC sockets and WebSockets for inter-process communication.


## Building a Standalone App

I've highlighted earlier that using `npx @neutralinojs/neu run` runs a Neutralinojs app in *dev mode*.
This means that the app executes via a pre-installed Neutralinojs executable created in the `bin/`
directory of the project.  That binary, as well as the frontend code, needs to be available to run
the app in dev mode.

It is possible to build a standalone executable for the app, using:

    npx @neutralinojs/neu build
    
As usual, run this from the `4-neutralino-node/image-viewer/` directory.  This will create a
standalone command-line executable in `dist/image-viewer/`.  In fact, it will create multiple
executables, for different architectures. 

At this point, we can run the appropriate executable from a command line terminal.  In my case,
running `./dist/image-viewer/image-viewer-mac_arm64` will open up the desktop app. (There is a
subtlety when running a Neutralinojs standalone executable.  If an extension command uses the
`NL_PATH` environment variable, it may struggle to find the extension.  On my system, when running a
Neutralinojs standalone executable, `NL_PATH` is set to the directory that contains the executable,
and not the root of the Neutralinojs app project. The easiest fix I could think of that let me run
the app both in dev mode and as a standalone executable is to put a symbolic link to the extension
directory `image-viewer/backend/` in the `image-viewer/dist/image-viewer/` directory.)

The standalone executable bundles the frontend code, and can be moved to another directory or moved
to another machine for execution. Of course, any extension that the app uses needs to be copied to
the target machine as well. And this is where things get a bit more complicated.

First, because the extension of our sample Neutralinojs app uses Nodejs, any target machine needs to
have Nodejs installed to run the extension, and therefore the Neutralinojs app. (As you'll notice,
we invoke `node` in the command field of `neutralino.config.json` for this extension.)  There are ways
around that, of course. For example, this [project](https://github.com/hschneider/neutralino-ext-node/) aims at bundling Nodejs with the extension by
essentially installing a local Nodejs environment alongside the extension. A nicer solution would be
to implement the extension in a language that can produce standalone executables, like C, Go, or
Rust.

Second, the Neutralinojs app is both a standalone executable for the app alongside code for the
extension (either as an executable or as source code, depending on how we implement the extension.)
That's a bit of a pain.  As is the fact that we need to run the app from the command line, even
though it's a desktop application.  One solution, on Mac OS, is to package the application into an
[application bundle](https://developer.apple.com/library/archive/documentation/CoreFoundation/Conceptual/CFBundles/BundleTypes/BundleTypes.html#//apple_ref/doc/uid/10000123i-CH101-SW1) which can include both the standalone executable and any auxiliary code or executables
required by the application.

I will leave these explorations to future posts. In the meantime, feel free to explore. Happy
hacking!
