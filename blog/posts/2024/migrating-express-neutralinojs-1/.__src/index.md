---
title: Migrating from Express to Neutralinojs, Part 1
date: 2024-12-15
reading: Nickel Mountain (by John Gardner)
---

A long time ago, I wrote a web app to caption and tag my pictures. Nothing fancy, just a local
alternative to online photo albums. The data itself — the pictures, associated text, tags — is kept
in a [SQLite](https://www.sqlite.org/)  database, and the interface to display those pictures and create and edit descriptions
and tags is a React web app that runs off a small Node.js Express-based application server whose
sole purpose is to provide a connection to the SQLite database. I start the server during my
machine's boot process, and use a bookmark in my browser to access the web app.

It's crude, but it gets the job done. Boring tech works.

Let me emphasize that last point: it works. I've been using it for the past
five years, and aside from library upgrades, I haven't had to touch it. This
post is _not_ about fixing something that does not work.

This post is about answering a question that I've had ever since I created the app: could I rewrite
it as a desktop app instead of a server-based web app? Having a server spinning in the background on the bit chance that I'll use the app feels inelegant; most days, I do not use it. Again, it works. It's just inelegant.

The answer to the question is trivially yes. I could reimplement the in a language that comes with a
GUI toolkit. I use a Mac, so something like [Swift](https://www.swift.org/) or even [Objective-C](https://developer.apple.com/library/archive/documentation/Cocoa/Conceptual/ProgrammingWithObjectiveC/Introduction/Introduction.html). That's
less interesting to me right now. I do not particularly want to recreate that app. So let me revise
the question: could I rewrite it as a desktop app without changing too much? I have the UI built
using standard web technologies, can I reuse that?

For the longest time, the answer to that revised question has been: "Sure, use [Electron](https://www.electronjs.org/)." Electron
lets you bundle a web frontend and a Node application server alongside a Chromium browser to produce
a desktop app that's basically a web browser dedicated to running that specific web frontend. I
tried Electron years ago, and while it worked, the resulting app was rather hungry in terms of
resources, both memory and CPU. It does, after all, run a full instance of Chromium. A couple of
these apps running at the same time would bring a machine to its knees. This has always been an
issue with Electron.

But it is now years later, and other options have emerged that compete with Electron. [Tauri](https://v2.tauri.app/) is a
lighter desktop web app bundler that uses [Rust](https://www.rust-lang.org/) as the backend language instead of Javascript. It
relies on the operating system's native WebView component to render the web-based frontend of the
app instead of Chromium. [Neutralinojs](https://neutralino.js.org/) is an alternative that also uses the native WebView component,
but rather than requiring a specific language for writing the backend allows that backend to be
implemented in a separate process and using inter-process communication (a websocket, to be precise)
to connect the web-based frontend to the backend.

Since my goal was to try to rewrite as little as possible, I decided to try Neutralinojs. In theory,
it would let me reuse not just the web frontend but also the Node.js backend. The only thing I would
need to figure out is how to get those pieces working with the Neutralinojs infrastructure. I am still
curious about Tauri, but will postpone an evaluation until another project requires it.

I set out to migrate my web app to Neutralinojs. To jump straight to the conclusion: it can be done,
it's not that hard, and it works reasonably well. Because my web app did not use server-side
rendering in any deep way, all the rendering could easily be folded into Neutralinojs without
rewriting. If you use server-side rendering more intensely, your experience may well be
different from mine.

As it was, the bulk of the work was refactoring the backend (the server) to work with Neutralinojs:
the web app relies on a standard REST-like API with several endpoints, while Neutralinojs requires the
frontend to communicate with the backend via a single websocket. The frontend code required no
change except for communicating with the refactored backend. The migration therefore boiled down to
translating a multi-endpoint HTTP-based RPC communication backend into a websocket-based
message-passing asynchronous backend. If that's enough for you to picture the full migration in your
head, great. No need to read the rest of this series. If you're wondering *what are you talking
about, Riccardo?*, then follow me on this three-parter trip.

To make sure I was not trying to solve too many possibly interrelated problems at the same time, I broke the migration down into a sequence of smaller migrations that handled one problem at a time:

- Migrate the Express-based web app with multiple HTTP endpoints to an Express-based web app with a single HTTP endpoint.
- Migrate the Express-based web app with a single HTTP endpoint to an Express-based web app with a websocket.
- Migrate from an Express-based web app with a websocket to a Neutralinojs app.

Intuitively, the first migration collapses communication to a single endpoint, the second introduces
a websocket for the communication, and the final migration does the actual switch over to
Neutralinojs. In this post and subsequent ones, I will walk you through the above migrations. None of
them are difficult in isolation.

To simplify the presentation, instead of working through the actual app I migrated, I'll use a smaller version that nevertheless contains all the pieces that make the original web app interesting: a list of pictures with a way to add a new picture to the list (by downloading it from a URL), and a way to click on a picture to see it in full. It will not give you a way to add a description to the picture, but that functional is both easy to add and orthogonal to the topic I want to focus on.

While the frontend of my original web app was in React, absolutely nothing in this migration project relied or was aware that the frontend was in React. To emphasize this, the simpler version has a primitive frontend written in raw Javascript. Call it revisiting the early 2000s. It is not meant to be illustrative of how you write a frontend — it simply highlights that the migration doesn't require any framework shenanigans.

All the code is available in a [GitHub repo](https://github.com/rpucella/neutralino-testbed) with each step of the migration living in its own directory.


## Starting Point: A Multi-Endpoints Express-Based Web App

Here is the basic Express-based web app, complete with multiple endpoints. The code can be found in directory `1-express-rest-multi` of the [repo](https://github.com/rpucella/neutralino-testbed), and its structure is pretty immediate:

    client/
      index.html
    server/
      package.json
      server.js
      
Yeah, this is as simple as it gets. The frontend is a single HTML file, and the server is the only thing that needs a build step.

You will need a recent version of Node installed globally. To install the packages that support the server:

    $ (cd server; npm install)
    
Once you have the packages, you can start the server — just make sure you are in the root directory of the web app:

    $ node server/server.js
    
The backend server holds the list of images, each image consisting of a URL (basically the name of the image) along with the actual binary array of the image, and endpoints to manage that list:

    GET /api/images
    GET /api/image?index={n}
    POST /api/images

The first endpoint retrieves the list of image names stored on the server. An image name is just the
URL where the picture was downloaded from. The second endpoint retrieves a specific image at a given
index in the list, and the third endpoint adds a new picture to the list, taking a URL of the image
and downloading it before adding it to the list.  With an eye towards what will happen down the line
for the migration, the picture in the second method is returned as a base64 encoded [data URL](https://developer.mozilla.org/en-US/docs/Web/URI/Reference/Schemes/data). All
methods return their result as a JSON object. I'll dig into the server code below.

But first, let's take a look at `index.html`, which is the _whole_ frontend! You can look at the [full
code](https://github.com/rpucella/neutralino-testbed/blob/main/1-express-rest-multi/client/index.html), but let me break it down into its interesting pieces.

The HTML proper is straightforward, if not boring:

      <h1>Image Viewer</h1>

      <img id="image">

      <div id="controls">
        <input id="load-input" type="text" placeholder="Image URL">
        <button id="load-button">Load</button>
      </div>

      <div id="images">
      </div>

Basically, there's a placeholder for an image, some controls, and a placeholder for a list of images. Some CSS ensures that things look reasonable — see the full file.

![Screenshot](./screenshot-1.png)

Script blocks contain the Javascript used by the frontend: communication with the server, and handlers for the controls in the UI.

Communication with the server is through an API class that exposes three methods to communicate with
the server, corresponding to the three API calls supported by the server and described above.

        class API {
          async _fetch(url, method, params) {
            const paramsObj = params || {}
            const response = await fetch(url, {
              method,
              ...params
            })
            const result = await response.json()
            return result.data
          }
    
          async fetchImages() {
            return this._fetch("/api/images", "get")
          }
            
          async fetchImage(index) {
            return this._fetch(`/api/image?index=${index}`, "get")
          }
            
          async addImage(url) {
            return this._fetch("/api/images", "post", {
              headers: {
                'Content-Type': 'application/json'
              },
              body: JSON.stringify({url: url})
            })
          }
        }

The implementation of the class relies on a common `_fetch_` method that does the actual job of communicating with the server. In the future, this `_fetch` method will bear the bulk of the changes needed to support Neutralinojs.

The handler code for the controls is equally straightforward. Two globals hold the list of image names retrieved from the server and an index into the currently displayed image, if any. (A -1 represents no image selected.), and the API is initialized. 

          // State.
          let images = []
          let currentImage = -1

          // Instantiate the API.
          const api = new API()

When the page loads, an `init()` function loads the image names from the server, and populate the images list in the UI by looping over every image name and adding it to element `#images`. 

          document.addEventListener("DOMContentLoaded", init)

          // Initialize the page.
          const init = async () => { 
            // Load initial images.
            images = await api.fetchImages()
            images.forEach(createImageEntry)
            if (images.length > 0) {
              await selectImage(0)
            }
          }
          
          const createImageEntry = (img, idx) => {
            const ul = document.getElementById("images")
            const elt = document.createElement("div")
            elt.setAttribute("id", `image-${idx}`)
            elt.addEventListener("click", () => selectImage(idx))
            elt.innerText = img
            ul.appendChild(elt)
          }

Each image name in the list gets assigned a click handler to fetch the actual image from the server and drop it into the `#image` element:

          // Fetch an image from the server and display it on the page.
          const selectImage = async (index) => {
            if (currentImage >= 0) {
              document.getElementById(`image-${currentImage}`).classList.remove("selected")
            }
            const img = await api.fetchImage(index)
            document.getElementById("image").setAttribute("src", img)
            document.getElementById(`image-${index}`).classList.add("selected")
            currentImage = index
          }
          
There are two controls in the UI: an input box and a **Load** button. When you enter a URL in the input box and click **Load**, a click handler makes the server download the picture from the given URL and add it to the picture list. It then adds the new picture name to the list, fetches the newly added picture from server, and displays it as if the user had clicked on it in the list

          // Load an image from the internet and add it to the pictures list.
          document.getElementById("load-button").addEventListener("click", async () => {
            const url = document.getElementById("load-input").value
            document.getElementById("load-input").value = ""
            if (url.trim().length > 0) {
              const newIndex = images.length
              await api.addImage(url)
              createImageEntry(url, newIndex)
              images.push(url)
              selectImage(newIndex)
            }
          })
    
That's it for the frontend. The backend server is equally simple, and implements the three endpoints described above by accessing a list kept on the server:

    import express from 'express'

    // Initial app and port.
    const app = express()
    const port = 8000
    
    // The list of images.
    const images = []

    app.use(express.json())

    // A class to represent the storage of images.
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

    // A class to hold the code for the endpoints.
    // Basically retrieve images from storage, or add 
    // an image to the storage.
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

    // Available routes for endpoints.
    
    app.post('/api/images', async (req, res) => {
        const {url} = req.body
        await controller.addImage(url)
        res.status(200).json({
            data: "ok"
        })
    })

    app.get('/api/image', (req, res) => {
        const {index} = req.query
        const img = controller.getImageDetails(index)
        res.status(200).json({
            data: img
        })
    })

    app.get('/api/images', (req, res) => {
        const imageNames = controller.getImages()
        res.status(200).json({
            data: imageNames
        })
    })

    app.use(express.static('client'))

    app.listen(port, () => console.log(`Listening at http://localhost:${port}`))

The endpoints use a `Controller` object to isolate the logic of the endpoints: interacting with a `Storage` object that abstract away from the underlying image storage facility. As yet another nod towards simplicity, images are stored in memory in a global array. Yes, I know, that means that images are not persisted, so that whatever you add to the array gets wiped out whenever you stop and restart the server. Modifying `Storage` so that it persists images to a database is easy, and does not affect the way in which the migration to Neutralinojs I want to illustrate functions. If you want to add a persistent database layer here, go right ahead — as long as it is accessed through the `Storage` object methods, it will transparently be usable from the Neutralinojs app we will end up with.

One additional detail: the line `app.use(express.static('client'))` ensures that if you hit any route that is not a defined endpoint, it will look for a file at a path corresponding to the route relative to directory `client`. That's how we get the frontend to display: pointing the web browser to `/index.html` gets the `index.html` file above encapsulating the frontend.


## First Migration: A Single-Endpoint Express-Based Web App

Because eventually we'll want to collapse all of our communication over a single websocket to use
Neutralinojs, let's take a step in that direction by making our express-backed app use a single
endpoint instead of the three that we are currently using. The code can be found in directory `2-express-rest-single` of the [repo](https://github.com/rpucella/neutralino-testbed):

    client/
      index.html
    server/
      package.json
      server.js

There are no changes in the structure of the code, or how to install and run the code. The only
differences are isolated to the `API` class in the `index.html` frontend and the endpoint routes in the `server.js` backend.

Let's start with the endpoint routes in the backend. The server now exposes a single API endpoint, called
`/api/message`. Because it needs to unify both `GET` and `POST` endpoints, I'm using a `POST` endpoint for it, passing data through a JSON body. The JSON body uses a `mode` field with possible values `get-image`, `get-images`, and `post-image` to identify the three operations that can be performed via that endpoint. Those operations, obviously, corresponding to the original endpoints. Any additional information (the index for `get-image`, or the URL for `post-image`) is added as a dedicated field in the body. Processing the message — that is, determining the kind of operation to perform and calling the controller to perform that operation — is delegated to a `processMessage` function that will turn out handy in future migrations:

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

    // Routes.

    app.post('/api/message', async (req, res) => {
      const msg = req.body
      const result = await processMessage(msg)
      res.status(200).json({
        data: result
      })
    })

We still have the "catch-all" endpoint to serve the frontend from the static `client/` directory.

On the frontend side, we replace the previous `API` class with a new `API` class that sends every request to the `/api/message` endpoint, with a suitable JSON body:

    class API {
      async _fetch(url, jsonBody) {
        const response = await fetch(url, {
          method: "POST",
          headers: {
            'Content-Type': 'application/json'
          },
          body: JSON.stringify(jsonBody)
        })
        const result = await response.json()
        return result.data
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

I admit that it feels weird to write the server in such a way that everything gets funneled through
a single route. But keep in mind that under the hood, the HTTP protocol sends every message through
a single port, and the route embedded in the HTTP message is used by Express to determine which
function to call. So in a way, the above is simply pushing the determination of which function to
call for every message later in the process (in the code, via the `processMessage` function). Of
course, there are some differences. For instance, the HTTP protocol permits `GET` messages to be
cached, something that we lose via this "everything is done through a single `POST` request. This would be important if we were writing a server to serve web traffic on the Internet. However, we are migrating a local server to support a desktop tab, which is a different (and simpler) use case.

Fun anecdote: an old colleague of mine would approach his database work in exactly that way. Instead
of exposing a collection of stored procedures to be called by our applications, he would write a
single stored procedure to act as the entry point of the system, dispatching to the appropriate
internal stored procedure based on a `mode`-type argument which would describe the operation to be
performed. This would keep the interface to the database uniform, so that new operations simply
required adding a new `mode` to the dispatching stored procedure.

Anyway, that's it for now. Next time, we'll take this single-endpoint Express-based web app and
migrate to use a websocket instead of an HTTP endpoint.
