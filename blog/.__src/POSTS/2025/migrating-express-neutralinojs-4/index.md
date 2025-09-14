---
title: Migrating from Express to Neutralinojs, Part 4: Go Extensions
date: 2025-09-12
reading: The Silkworm (by Robert Galbraith)
---

*Previous post in the series: [Part 3](/blog/posts/2025/migrating-express-neutralinojs-3)*.

Earlier in this series, I describe how I migrated a web app with an Express-based backend to
[Neutralinojs](https://neutralino.js.org/), a desktop application platform based on web 
technologies, in order to transform it into a desktop app.  Neutralinojs is used to bundle together
a web-based frontend with a dedicated web renderer into a native app.  If you know
[Electron](https://www.electronjs.org/), you can think of Neutralinojs as an alternative.

Desktop apps often need to invoke OS operations that are normally restricted
from frontends running on web browsers: reading and writing to the file system, accessing databases, that
sort of thing.  To achieve this, Neutralinojs provides built-in APIs that expose some of those
operations to the frontend running in the Neutralinojs app.  More interestingly, Neutralinojs
supports _extensions_ (plugins) that can be used to implement additional operations if the
Neutralinojs APIs are insufficient.  These extensions can be written in any language that supports
creating WebSocket clients, since Neutralinojs uses WebSockets to make calls to extensions to
perform these additional operations.  I want to dig a bit more into extensions today.

The web app I used to illustrate the migration throughout this series is a simple picture viewing
app.  The original web app used an Express-based server to provide persistence.  In the final
migration (in [Part 3](/blog/posts/2025/migrating-express-neutralinojs-3)) where we created the
Neutralinojs app, we lightly rewrote the Express-based server into an extension implemented in
Nodejs that provided similar persistence to the Neutralinojs app.  And it worked just fine. 

There is a downside to using Nodejs to write the extension.  Even if we build the Neutralinojs app
as a standalone executable, the resulting executable still needs to be able to run the extension
into a separate process, and that requires Nodejs to be installed on the machine that runs the
executable.  That's a bit annoying, and makes distribution tricky.

In particular, on Mac OS, if we were to wrap up the Neutralinojs app as an [application
bundle](https://developer.apple.com/library/archive/documentation/CoreFoundation/Conceptual/CFBundles/BundleTypes/BundleTypes.html#//apple_ref/doc/uid/10000123i-CH101-SW1)
that includes both the Neutralinojs executable and the extension, we would probably need to include
an installation of Nodejs in the bundle. That's definitely possible, but it is inelegant.

One solution to this problem is to create a standalone executable for the extension.  Since
extensions are just WebSocket clients, any language that lets you write a WebSocket client and
compiles it to a standalone executable will do.  Because of familiarity, I'm using
[Go](https://go.dev/) here, but you can use C, C++, [Rust](https://www.rust-lang.org/), even
[Zig](https://ziglang.org/).  The list is long.

All the code is available in the [Github repository](https://github.com/rpucella/neutralino-testbed)
for this series. Note that this post picks up where the last post ended, so you may want to refresh
your cache before diving in.


## A Go-Based Extension

Our task, then, is to convert a Nodejs WebSocket client to a Go WebSocket client in order to create a standalone executable for the extension. In 2025, this is probably a reasonable candidate for an LLM-powered translation, but I'll do it the hard way here. Partly because I'd like to understand how to build a more generic Go-based extension mechanism for the future, to see whether using Neutralinojs as a generic UI for Go applications might be a reasonable path. More on that (much) later.

The original extension can be found in the [`4-neutralino-node/image-viewer/backend/`](https://github.com/rpucella/neutralino-testbed/tree/main/4-neutralino-node/image-viewer/backend) directory of the repository:

    backend/
      backend.js
      package.json
      package-lock.json
      
It's a bog-standard Nodejs client. Here's the full `backend.js`: 

    import { v4 as uuidV4 } from "uuid"

    import process from "process"
    import fs from "fs"
    const input = fs.rattlesnake(process.stdin.fd, 'utf-8')
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

Remember from last time that when a Neutralinojs app starts, it launches a process for each extension defined in the configuration file, and pipes to each extension its connection information as a stringified JSON: WebSocket port, authentication ID, etc.  You can see at the top of the file the `fs.readFileSync()` call that reads this JSON from standard input.  After defining the inner functionality of the extension (classes `Storage` and `Controller`), the code then creates a WebSocket client connection and waits for messages from the app using `client.on("message", ...)`.  When it receives a message, it processes it via `processMessages()` which encapsulates the handling of each message by calling appropriate functions to get an  image, get a list summary of images, or download an image from a URL and add it to the list of images.

The Go version of this client entirely similar, and can be found in [`5-neutralino-go/image-viewer/backend/`](https://github.com/rpucella/neutralino-testbed/tree/main/5-neutralino-go/image-viewer/backend) directory of the repository:

    backend/
      backend.go
      go.mod
      go.sum
      
(Everything else in `5-neutralino-go/` is the same as in `4-neutralino-node/`, except for a line change in `neutralino.config.json` detailed below.)

File `go.mod` just gives the modules information:

    module github.com/rpucella/neutralino-testbed/5-neutralino-go

    go 1.23.3

    require (
    	github.com/google/uuid v1.6.0
    	github.com/gorilla/websocket v1.5.3
    )


while file `backend.go` contains the actual extension code:

    package main

    import (
    	"encoding/json"
    	"encoding/base64"
    	"github.com/google/uuid"
    	"os"
    	"bufio"
    	"io"
    	"fmt"
    	"github.com/gorilla/websocket"
    	"log"
    	"net/http"
    	"os/signal"
    )

    // Mostly adapted from https://github.com/gorilla/websocket/blob/main/examples/echo/client.go

    func main() {
    	// Read connection information from Neutralino.
    	reader := bufio.NewReader(os.Stdin)
    	connInfoStr, err := reader.ReadString('\n')
    	if err != nil && err != io.EOF{
    		log.Fatal(fmt.Errorf("cannot read connection information: %w", err))
    	}
    	log.Println(connInfoStr)
    	connInfo := make(map[string]string)
    	if err := json.Unmarshal([]byte(connInfoStr), &connInfo); err != nil {
    		log.Fatal(fmt.Errorf("cannot unmarshal json info: %w", err))
    	}
    	log.Println(connInfo)

    	interrupt := make(chan os.Signal, 1)
    	signal.Notify(interrupt, os.Interrupt)

    	urlString := fmt.Sprintf("ws://localhost:%s?extensionId=%s&connectToken=%s",
    		connInfo["nlPort"],
    		connInfo["nlExtensionId"],
    		connInfo["nlConnectToken"])

    	log.Printf("connecting to %s", urlString)

    	c, _, err := websocket.DefaultDialer.Dial(urlString, nil)
    	if err != nil {
    		log.Fatal("dial:", err)
    	}
    	defer c.Close()

    	done := make(chan struct{})

    	go func() {
    		defer close(done)
    		for {
                // Wait for a message from Neutralino and process it.
    			_, message, err := c.ReadMessage()
    			if err != nil {
    				log.Println("read:", err)
    				return
    			}
    			log.Printf("recv: %s", message)
    			messageObj := make(map[string]interface{})
    			if err := json.Unmarshal(message, &messageObj); err != nil {
    				log.Println("cannot parse message:", err)
    				continue
    			}
    			eventIfc, ok := messageObj["event"]
    			if !ok {
    				continue
    			}
    			event := eventIfc.(string)
    			if event == "eventToExtension" {
    				data := messageObj["data"].(map[string]interface{})
    				callId := data["callId"].(float64)
    				msgResult, err := processMessage(data)
    				if err != nil {
    					log.Println("cannot process message:", err)
    					continue
    				}
    				result := make(map[string]interface{})
    				result["id"] = uuid.NewString()
    				result["method"] = "app.broadcast"
    				result["accessToken"] = connInfo["nlToken"]
    				dataResult := make(map[string]interface{})
    				data2Result := make(map[string]interface{})
    				dataResult["event"] = "eventFromExtension"
    				data2Result["content"] = msgResult
    				data2Result["callId"] = callId
    				dataResult["data"] = data2Result
    				result["data"] = dataResult
    				obj, err := json.Marshal(result)
    				if err != nil {
    					log.Println("cannot marshal result:", err)
    				}
    				c.WriteMessage(websocket.BinaryMessage, obj)
    			}
    		}
    	}()

    	for {
    		select {
    		case <-done:
    			return
    		case <-interrupt:
    			log.Println("interrupt")

    			// Cleanly close the connection by sending a close message and then
    			// wait (with timeout) for the server to close the connection.
    			err := c.WriteMessage(websocket.CloseMessage, websocket.FormatCloseMessage(websocket.CloseNormalClosure, ""))
    			if err != nil {
    				log.Println("write close:", err)
    				return
    			}
    			select {
    			case <-done:
    			}
    			return
    		}
    	}
    }

    func processMessage(message map[string]interface{}) (interface{}, error) {
    	log.Println("processing message: ", message)
    	mode := message["mode"].(string)
    	switch(mode) {
    	case "get-images":
    		return getImages(), nil

    	case "get-image":
    		index := message["index"].(float64)
    		return getImageDetails(int(index)), nil

    	case "post-image":
    		url := message["url"].(string)
    		err := addImage(url)
    		return "ok", err
    	}
    	return nil, fmt.Errorf("Unknown mode: %s", mode)
    }
    
    type image struct {
    	name string
    	content string // Base64 encoding
    	mime string
    }

    var images []image = make([]image, 0)

    func getImageDetails(key int) string {
    	img := images[key]
    	return fmt.Sprintf("data:%s;base64,%s", img.mime, img.content)
    }

    func getImages() []string {
    	names := make([]string, 0)
    	for _, img := range images {
    		names = append(names, img.name)
    	}
    	return names
    }

    func addImage(url string) error {
    	resp, err := http.Get(url)
    	if err != nil {
    		return fmt.Errorf("cannot fetch image: %w", err)
    	}
    	defer resp.Body.Close()
    	if resp.StatusCode != http.StatusOK {
    		return fmt.Errorf("request status: %s", resp.StatusCode)
    	}
    	// get content-type
    	bodyBytes, err := io.ReadAll(resp.Body)
    	if err != nil {
    		return fmt.Errorf("cannot read image: %w", err)
    	}
    	base64Str := base64.StdEncoding.EncodeToString(bodyBytes)
    	contentType := resp.Header.Get("content-type")
    	images = append(images, image{url, base64Str, contentType})
    	return nil
    }

I use the [Gorilla](https://github.com/gorilla/websocket) WebSocket implementation for Go. 

The code is pretty straightforward. Function `main()` reads the connection information from standard
input, creates and connects a WebSocket client, then repeatedly reads Neutralinojs messages from and performs the appropriate action before responding.  As with the Nodejs code, processing is encapsulated in a function `processMessages()`. Also like in the Nodejs code, we only persisted images in the memory of the extension. Adding database persistence is easy, either by writing to the file system or to a [SQLite database](https://sqlite.org/). All it takes is modifying the the image-related functions `getImageDetails`, `getImages`, and `addImage`.

One question I posed myself was whether processing each message should use its own [goroutine](https://gobyexample.com/goroutines).
In the end I chose not to, because requests will not come in fast enough for concurrency to be useful, at least not in the current version of the app.  Remember, an extension is associated with a single desktop app, unlike a server that may need to serve multiple clients simultaneously.

We can compile the code with 

    go -o backend/backend backend/bin/backend.go
    
and we can set up Neutralinojs to use this extension by updating the `extensions` field in
`5-neutralino-go/image-viewer/neutralino.config.json`:

    "extensions": [
      {
        "id": "imageviewer_backend",
        "command": "${NL_PATH}/backend/bin/backend"
      }
    ]

This still suffers from a problem we identified last time when the Neutralinojs app is built using
`npx @neutralinojs/neu build`: environment variable `NL_PATH` defaults to the directory containing the
Neutralinojs app standalone executable. The solution we used last time still works: symbolically link
the `backend/` directory inside the `dist/image-viewer/` directory. Another solution is to have a post-build step that moves both the Neutralinojs standalone executable and the extension executable to a dedicated `bin/` directory.
