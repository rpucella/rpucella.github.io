---
title: Migrating from Express to Neutralinojs, Part 2
date: 2025-07-18
reading: Sidi (by Arturo Perez-Reverte)
---

*Previous posts in the series: [Part 1](/blog/posts/2024/migrating-express-neutralinojs-1)*.

This is part 2 of a series about migrating an Express-based web app to [Neutralinojs](https://neutralino.js.org/), a
desktop application platform based on web technologies. Neutralinojs is an
alternative to [Electron](https://www.electronjs.org/) that in theory requires less resources, as it does not embed a full Chromium
build. More interestingly, Neutralinojs lets you write the backend logic using any programming
language by relying on IPC via a WebSocket to a separate process running the backend code. Neutralinojs calls those separate processes _extensions_.

To illustrate this migration, I use a simple picture viewing web app as an example. The code is [freely available on GitHub](https://github.com/rpucella/neutralino-testbed).  The migration itself is achieved through a sequence of small migrations, each isolating one interesting aspect of the whole migration:

- Migrate the Express-based web app with multiple HTTP endpoints to an Express-based web app with a single HTTP endpoint.
- Migrate the Express-based web app with a single HTTP endpoint to an Express-based web app with a WebSocket.
- Migrate from an Express-based web app with a WebSocket to a Neutralinojs app.

We completed the first migration in the [last post](/blog/posts/2024/migrating-express-neutralinojs-1), ending up with an Express-based web app with a
single HTTP endpoint. You can find it in directory `2-express-rest-single/` of the [repository](https://github.com/rpucella/neutralino-testbed):

Today, we tackle the second migration to obtain an Express-based web app with a WebSocket.


## Second Migration: A WebSocket Express-Based Web App

A [WebSocket](https://en.wikipedia.org/wiki/WebSocket) is a long-duration two-way data communication channel over TCP between a client and a
server. In that sense, it is like an [Internet socket](https://en.wikipedia.org/wiki/Network_socket). One difference is that it is designed to work
well alongside HTTP (or HTTPS). More specifically, you can start with an existing HTTP connection
and ask it to switch to the WebSocket protocol via the [HTTP Upgrade header](https://en.wikipedia.org/wiki/HTTP/1.1_Upgrade_header). This makes using
WebSockets fairly transparent from a web app perspective. WebSockets are typically used when we need real-time collaboration, where messages may need to be send from the server to the client without a request by the client.  For example, a chat system needs to send messages to user A's client when some other user types a message directed at user A. Without a WebSocket or something similar, the client frontend for a user would have to
poll the server at regular intervals to check if new messages are available directed at the user.

MDN has a reasonable [introduction to and description of the WebSocket API in browsers](https://developer.mozilla.org/en-US/docs/Web/API/WebSockets_API).

Since Neutralinojs uses WebSockets to communicate with processes running backend logic, we prepare that forthcoming migration by modifying our Express server to use a WebSocket for
communication instead of an HTTP endpoint. The code can be found in directory `3-express-websocket`
of the [repository](https://github.com/rpucella/neutralino-testbed): 

    client/
      index.html
    server/
      package.json
      server.js

Again, there are no changes in the architecture of the web app: one directory for the client code (a
single HTML file), and one directory for the server code (using Nodejs and Express). The changes are 
confined to the `API` class in the `index.html` client and the new WebSocket endpoint
in the `server.js` server.

Let's start with the new WebSocket endpoint on the server. Instead of the `app.post` route created
in the single-endpoint version of the server, we now create a WebSocket endpoint. I use the
[`express-ws`](https://www.npmjs.com/package/express-ws) package to manage the creation of this server-side WebSocket.


    import expressWS from 'express-ws'

    expressWS(app)

    app.ws('/api/ws', async (ws, req) => {
      ws.on('message', async (msg) => {
        const obj = JSON.parse(msg)
        const callId = obj.callId
        const result = await processMessage(obj)
        ws.send(JSON.stringify({content: result, callId}))
      })
    })

The `expressWS(app)` call sets up the Express app to manage WebSocket endpoints. It handles all the logic to upgrade the HTTP request to a WebSocket request, and transforms actions and messages over the WebSocket into events. From the perspective of the server, all we need to do is create a route (here, `/api/ws`) and expose it as a WebSocket. As we'll see below, a client only has to open a WebSocket connection to `/api/ws` and everything should flow beautifully. 

WebSockets work through events triggered when WebSockets connect, receive messages, close, or error out. For simplicity, the server above only handles receiving messages; a production system would need a lot more error checking.

The HTTP-based server uses a JSON object to pass and return information via the `POST` endpoint. Our WebSocket will use the same JSON. It needs to be stringified explicitly though. Recall that the JSON object sent by the client has a field `mode` indicating the operation to perform (`get-images`, `get-image`, `post-image`) and other fields hold whatever parameters are pertinent to the operation. For example, this is the JSON for downloading and adding a new picture to the server list:

    {
      "mode": "post-image",
      "url": URL
    }
    
where `URL` is the URL of the picture to download.

When a message is received over the WebSocket by the server and triggers the `message` event, the body
of the message is parsed into a JSON object and handed over to the `processMessage()` function to
perform the operation represented by the JSON object. The `processMessage()` function is exactly as
before, requiring no change.  The result of the operation is wrapped into a JSON object that is
stringified and sent back to the client over the WebSocket. 

There is one slight complication with switching from an HTTP endpoint to a WebSocket endpoint. The
HTTP protocol is naturally a request-response protocol: when you send an HTTP request, you can wait
for the response and you will get the response that corresponds exactly to the request you sent. If
you send two requests at the same time, you can wait for the respective responses and they will not
conflict with each other — the protocol keeps track of which response correspond to which
request. The WebSocket protocol, in contrast, is a two-way shared communication channel that has no
concept of request and response. Either end of the channel can post a message on the channel to send
it to the other end. There is no notion of a response to that message. If we want to impose a
request-response structure on top of a WebSocket, we need to do it ourselves. The easiest way is
simply to attach a unique ID to every message sent, and when we send back what we consider a
response to that message, we attach that ID to the response. The client who sent the original
request can wait for a message that has a matching ID and ignore all others.

In the code above, you see that the JSON object received has a field `callId`, which is how that
unique ID will be passed by the frontend code. That `callId` is attached to the response before it is
sent back. The sample code from [Small Technology Foundation](https://small-tech.org/)'s [WebSocket RPC example](https://github.com/small-tech/site.js-websocket-rpc-example) was an an
inspiration here.

To round up the description of the server, note that we still need the "catch-all" endpoint to serve
the frontend from the static `client/` directory.

On the frontend side, we replace the previous `API` class with a new `API` class that creates a WebSocket connection to `/api/ws` and sends every message to that WebSocket:

    class API {
    
      constructor() {
        const websocket = new WebSocket("/api/ws", "ws")
        this._websocket = websocket
        websocket.onerror = (event) => {
          console.dir(event, {depth:null})
        }
        this._isReady = new Promise((resolve) => {
          websocket.onopen = (event) => {
            resolve(true)
          }
        })
        this._callId = 0
      }

      async _fetch(_, obj) {
        await this._isReady
        const websocket = this._websocket
        // callId is a unique identifier for the call so that we can catch the response.
        const callId = this._callId
        this._callId += 1
        const response = new Promise((resolve) => {
          const listener = (event) => {
            const data = JSON.parse(event.data)
              if (data.callId === callId) {
              websocket.removeEventListener('message', listener)
              resolve(data)
            }
          }
          websocket.addEventListener('message', listener)
        })
        websocket.send(JSON.stringify({...obj, callId}))
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

Only the constructor and the `_fetch()` method have been modified. Let's look at them in order.

First, the constructor creates the WebSocket connection to the `/api/ws` WebSocket endpoint on the server. It
stores the resulting WebSocket connection and initializes the counter `callId` that keeps track of the
unique IDs we'll need to associate to every sent message.

Second, the `_fetch()` method, which is used by the three API methods `fetchImages`, `fetchImage`, and `addImage`, does the bulk of the work. It adds a `callId` to the JSON object representing the request (incrementing the `callId` for the next call) and sends the stringified JSON object over the WebSocket. It also sets up a dedicated event listener _for the specific `callId` that went out_ that waits for a message back from the WebSocket. When a message arrives, the listener checks if the `callId` matches the `callId` of the message it is associated with: if it's a match, then the message is taken to be the response and the `_fetch()` can return. If it's not a match, the message is discarded. (Don't worry — if that message was a response to some other request, then _that_ request's event listener will process the response.) To prevent a memory leak, when a response to a request is received, the event listener associated with the request self-destructs.

Perhaps astonishingly, this works. I'm not sure how expensive it is to keep a long-running WebSocket
open for each user session if we were to put this server online, but since this is meant as a
step towards a single-user desktop application, I'm not particularly worried about this.

Next time, we'll take the plunge and migrate the app to Neutralinojs. Stay tuned!
