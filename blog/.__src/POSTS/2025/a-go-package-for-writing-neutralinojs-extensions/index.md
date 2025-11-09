---
title: A Go Package for Writing Neutralinojs Extensions
date: 2025-11-07
reading: The Pornographer (by John McGahern)
---

My last several posts have all been about [Neutralinojs](https://neutralino.js.org/). I'm not obsessed. It's just the latest thing
I've been playing with that I feel like writing about here. Ironically, my writing about it is what
keeps me working with it. If not for that, I probably would have already moved on to something
else. And working with Neutralinojs while writing these posts has exposed me to topics that I would have not 
gone into otherwise. Today's post is one such example.

Much of my recent explorations with Neutralinojs have revolved around [extensions](https://neutralino.js.org/docs/how-to/extensions-overview/). As a reminder,
because Neutralinojs is meant for building desktop apps through web-based technologies, it needs to
provide access to the same kind of OS resources that a typical desktop app has access to: file
system, processes, even hardware. Neutralinojs provides APIs that expose some of those OS resources,
but this needs to be supplemented with custom APIs for resources that have no built-in support.
Extensions are the way to implement those custom APIs; they are just separate programs that can
connect to a Neutralinojs desktop app via a WebSocket and can receive and respond to messages from
the desktop app. They can be implemented in any language that supports WebSockets.  Thus, if you
need to access a local database, then you need to create an extension that accesses the database and
call that extension from your Neutralinojs app to perform database operations. There's nothing complicated going on here.

In [Part 4](/blog/posts/2025/migrating-express-neutralinojs-4/) of my series of posts about migrating an Express-based web app to a Neutralinojs desktop app, I implemented a Go-based extension to provide (fake) persistence for the sample picture viewing
app we were migrating. That extension was minimalist: it started a WebSocket client that connected to the Neutralinojs app, and waited for and processed messages from the app by either returning pictures from an internal list of pictures or adding pictures to the list after downloading them from a URL. I call what it did _fake_ persistence because it kept the list of pictures in memory instead of persisting it to disk.
My argument was that adding actual persistence by writing the pictures to a [SQLite](https://sqlite.org/) database was a simple exercise. (I'll return to persistence later in the post.)

If I'm seriously going to use Neutralinojs to build desktop apps (and I am) then I'll need to
develop more extensions, most likely in Go. It therefore makes sense to create a package to abstract
away all the common stuff that an extension needs to do, so that the only thing I need to specify is
how to handle the individual messages.  If you've ever used the `http` package in Go for writing a web
server, then you get it. 

Here's my first stab at just such a package: [`go-neutralino-extension`](https://github.com/rpucella/go-neutralino-extension), which imports as `neutralinoext`. It exposes a couple of functions. The first is

    func ReadConnInfo(r io.Reader) (ConnInfo, error)
    
which reads the Neutralinojs connection information sent from the app when it starts the
extension. The input should almost always be `os.Stdin`. The resulting connection object of type `ConnInfo` supports a method

    func (ci ConnInfo) StartMessageLoop(process ProcessFn) error

which starts the loop that waits for messages from the app and takes a callback function to invoke on every received message, with type 

    func(string, any) (map[string]any, error)

Once the loop is started, every received message triggers a call to the callback function with
name of the event in the message and the data in the message as an unmarshaled JSON object. It is up to the callback function to process the message appropriately to implement whatever you want the extension to do. 

The package implements a simple form of RPC, inspired by this [WebSocket RPC example](https://github.com/small-tech/site.js-websocket-rpc-example): when a received message has a `_respId`
field and a `_respEvent` field and the callback function returns a non-nil `map[string]any` value, then
the message loop sends back a message to the Neutralinojs app via the `app.broadcast` API with
`_respEvent` as the event and a JSON object marshaled from the `map[string]any` as data, that object extended with a `_respId` field holding the same `_respId` value that was received. Thus, from the Neutralinojs app perspective, if it sends a message to the extension by setting `_respId` in the message, it can then wait for a message from the extension with a matching `_respId`.

Here's some sample Javascript code to do just that. The following function sends a
message `obj` with event `evtName` to extension `ext` and waits for a response:

    // Store a sequence number for each call to the extension.
    let _callId = 0
    
    async function _fetch(ext, evtName, obj) {
      const callId = _callId
      _callId += 1
      const response = new Promise((resolve) => {
        const listener = (event) => {
          const data = event.detail
          if (data._respId === callId) {
            Neutralino.events.off(evtName, listener)
            resolve(data)
          }
        }
        Neutralino.events.on(evtName, listener)
      })
      await Neutralino.extensions.dispatch(extension, evtName, {...obj, _respId: callId, _respEvent: evtName})
      const data = await response
      return data
    }

To illustrate the use of the package, here's a trivial callback function that echoes back every message with event `echo` and a data object with a field `message` containing the string to be echoed back. The echoed string is constructed by prepending `echoed value: ` in front of the string received:

    func processMsg(event string, data any) (map[string]any, error) {
    	if event != "echo" {
    		return nil, nil
    	}
    	dataObj, ok := data.(map[string]any)
    	if !ok {
    		return nil, errors.New("data not an object")
    	}
    	messageIfc, ok := dataObj["message"]
    	if !ok {
    		return nil, errors.New("no message field")
    	}
    	message, ok := messageIfc.(string)
    	if !ok {
    		return nil, errors.New("mesage not a string")
    	}
    	result := make(map[string]any)
    	result["echo"] = "echoed value: " + message
    	return result, nil
    }

The complete example can be found in the [repository](https://github.com/rpucella/go-neutralino-extension), along with the skeleton of an extension that can query a SQLite database. I intend to flesh this one out to a full extension, since most of my desktop apps are backed by such a database.

Two quick notes on the package before I drop off:

- Messages are not buffered. They could be, but they're not. Building a buffer to receive messages
  is an easy Go exercise. In practice, because messages send to the extension all come from the one
  desktop app, it seems unlikely that the extension would get more messages that it can handle. But
  I guess we'll find out.
  
- A function to send a message to the app, unprompted, is available. I haven't found a use for it
  yet, but I can imagine a scenario where the extension is running some task in the background and
  alerts the app when something special happens. The requires the message loop to run in a goroutine
  alongside a goroutine to run the background task. Again, a typical Go pattern.
  
