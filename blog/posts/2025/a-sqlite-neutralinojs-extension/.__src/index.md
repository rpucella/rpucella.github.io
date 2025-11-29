---
title: A SQLite Neutralinojs Extension
date: 2025-11-28
reading: Royal (by Jean-Philippe Baril Guérard)
---

Okay, this is probably going to be my last [Neutralinojs](https://neutralino.js.org/) post for a while, because with what I'll talk about today, I have all the ingredients to supports my ongoing desktop app requirements.

I've already rambled on about [extensions for Neutralinojs](https://neutralino.js.org/docs/how-to/extensions-overview), which are the main way to provide non-native OS-level resource access to Neutralinojs apps. In my series of posts where I [migrate an online web app into a Neutralinojs desktop app](/blog/posts/2025/migrating-express-neutralinojs-3), an extension plays the role of the server-side code in the original web app, ostensibly to implement persistence. While creating an extension for that was technically correct, it over complicates the picture; if the goal is to provide persistence, then instead of creating an application-specific extension to manage access to the database, it is better to provide access to the database directly from the Neutralinojs app via a generic "database" extension. This is more in line with how the rest of the Neutralinojs APIs are structure.

In other words, instead of calling an extension that exposes an entry point, say `getImage()`, that in the extension fetches an image from the database table, we should simply issue a `select ... from images where...` query to a database extension. Direct database access from frontend code is frowned upon in a web app, but none of the arguments apply to a desktop application.  That reflect my improving understanding of extensions: they're just OS-level APIs. Approaching them like I'd approach a server in a web app is a distortion.

With all of that in mind, the goal for today is to provide that kind of generic database extension usable by Neutralinojs apps. For the sake of specificity (and because that's my personal use case), the database extension supports a SQLite database, but it shouldn't be difficult to generalize it to provide generic access to any other relational database. In fact, the extension is pretty bare bones, even with the restriction to SQLite: it provides parameterized query access, and that's about it. No transactions, no cursors, nothing. Just queries and result sets. Additional features may be added at some point, but for now, as I said above, my desktop app requirements are happily met.


## The Extension

The SQLite extension is found in the [`neutralino-sqlite`](https://github.com/rpucella/neutralino-sqlite) Github repository. It uses the Neutralinojs extension library for Go that I implemented and described in [A Go Package For Writing Neutralinojs Extensions](/blog/posts/2025/a-go-package-for-writing-neutralinojs-extensions), including the RPC mechanism described there.

The extension expects one command-line argument: the SQLite database file to which all queries will be directed. It then reads the connection information sent from the Neutralinojs app on standard input when it starts up the extension and connects to the Neutralinojs app before entering the message loop and waiting for relevant messages.

Recall that a message sent by a Neutralinojs app to an extension has an `event` field and a `data` field. Two events are currently supported: `query` and `exec`. Both issue a SQL query to the database: `query` returns a sequence of rows while `exec` does not return any value and is merely executed for its side effects.

In more details, the `query` message expects a `data` object of the form

    {
      "sql": <query string>,
      "params": [<value>, ...]
    }

where `sql` is the actual SQL query to issue. The `params` field is there to support _parameterized queries_, that is, queries with _placeholders_ where the placeholders are replaced by values passed separately, and converted to SQL values by the database driver: strings are automatically quoted, etc. The `params` field is a list of JSON values to inject into the SQL query at position indicated by `$1`, `$2`, `$3`, ... in the query. For example:

    {
      "sql": "select name, email from users where user_id = $1 or user_id = $2",
      "params": [101, 102]
    }

(The use of `$1`, `$2`, `$3`, ... comes from the underlying Go package used to interface to SQLite, [`go-sqlite3`](https://github.com/mattn/go-sqlite3).) The `query` message returns a response as an object with one field (`rows`) containing an array of rows, where each row represented by an array of values. For example:

    {
      "rows": [["Alice", "alice@example.com"], ["Bob", "bob@example.com"]]
    }

The `exec` message works exactly the same as the `query` message, except that it doesn't return an array of rows. It is meant to execute DDL statements that change the database without returning data. For example:

    {
      "sql": "update users set name = $1 where user_id = $2",
      "params": ["Robert", 102]
    }

It does return an acknowledgment that the statement was executed, of the form:

    {
      "done": true
    }
    
Since this extension is built upon the Neutralinojs Go extension library [`go-neutralino-extension`](https://github.com/rpucella/go-neutralino-extensions), it supports the RPC mechanism of that library: if we add `_respId` and `_respEvent` to the data object sent as part of the message to the extension, the response from the extension will also contain a field `_respId` with the same value as the message that was sent, and will be sent with the event name specified in `_respEvent`. This means that we can wait for the exact response to any message we send to the extension by waiting on the event we specify, and match the `_respId` that we receive back. (See method `_sql` in class `API` in the example below for one way to do just that.)


## An Example

A full example of a Neutralinojs app using the extension can be found in the `example/` directory of the repository.  It is the image viewer example I've been using in my migration posts, a variant of the [code described in Part 4 that uses a Go extension](/blog/posts/2025/migrating-express-neutralinojs-4).

The code has the same structure as I've described in the migration posts:

    Makefile
    images.db
    images.ddl
    neutralino.config.json
    bin/
    extensions/
    resources/
      index.html
      icons/
      js/
    

The frontend code is in the `resources/` directory, while `bin/` contains the files that Neutralinojs uses to build the final desktop app. File `neutralino.config.json` is the app configuration file. 

The initial SQLite database (which contains a single cat picture) is `images.db`. It was created using the `images.ddl` script which is included for reference. The database contains a single table with one row per image with a name, a base64 encoding of the image, and a mime type for the image:

    create table images (
      id integer primary key, 
      url text, 
      image text
    );
    
The Neutralinojs configuration file tells Neutralinojs how to start the extension when the app starts:

    "extensions": [
        {
          "id": "sqlite",
          "command": "${NL_PATH}/extensions/sqlite-ext ${NL_PATH}/extensions/images.db"
        }
      ],

You see it expects the extension (and the database file) in directory `extensions/`. So the first step of the build process has to be to populate the `extensions/` directory with the extension executable, and the initial database. I do this via `a Makefile`, which also build the Neutralinojs app:

    build:
    	cp ../../bin/sqlite-ext extensions
    	cp images.db extensions
    	npx @neutralinojs/neu build

Yes, I know, this is just a shell script. I use `Makefile`s in all my projects, for consistency. Full disclosure: this works well for this project, because the extension itself is available in the same repository, but I am not entirely sure how I would distribute the source of a Neutralinojs app that uses the SQLite extension. Do I provide a script to fetch and build the extension? The Neutralinojs app is a Node.js project, run via `npm`, while the extension requires building via a Go compiler. I'll need to dig into this a bit more in the future.

Anyway, all of that is setup. To use the SQLite extension from the Neutralinojs app, I rely on the fact that my app code already had its calls to the "database" isolated through an `API` class. That was meant to support the original migration from an Express-based backend (where the API calls were REST calls) to Neutralinojs (where the API calls were extension calls via the Neutralinojs library), and I benefit from that here. It is a simple matter to update the class to use this generic SQLite extension over the custom-built extension:

    class API {
      constructor() {
        this._callId = 0
      }

      async _query(obj) {
        return this._sql("query", obj)
      }

      async _exec(obj) {
        return this._sql("exec", obj)
      }

     async _sql(eventName, obj) {
        const extension = "sqlite-ext"
        const callId = this._callId
        this._callId += 1
        const response = new Promise((resolve) => {
          const listener = (event) => {
            const data = event.detail
            if (data._respId === callId) {
              Neutralino.events.off(eventName, listener)
              resolve(data)
            }
          }
          Neutralino.events.on(eventName, listener)
        })
        const dataObj = {...obj, _respId: callId, _respEvent: eventName}
        await Neutralino.extensions.dispatch(extension, eventName, dataObj)
        const dataResp = await response
        return dataResp
      }

      async fetchImages() {
          console.log("about to query")
        return this._query({
          sql: "select url from images"
        })
    }
        
      async fetchImage(index) {
        return this._query({
            sql: "select url, image from images where id = $1",
            params: [index]
        })
      }
        
      async addImage(index, name, url) {
        const base64 = await imageToBase64(url)
        return this._exec({
            sql: "insert into images (id, url, image) values ($1, $2, $3)",
            params: [index, name, base64]
        })
      }
    }

    async function imageToBase64(url) {
        const response = await fetch(url)
        const blob = await response.blob()
        
        return new Promise((resolve, reject) => {
            const reader = new FileReader()
            reader.onloadend = () => resolve(reader.result)
            reader.onerror = reject
            reader.readAsDataURL(blob)
        })
    }

The `_sql()` method ultimately contains the logic to send a message to the extension and more importantly to wait for the result of the message in the form of a message sent by the extension with the same call ID that was sent with the original message.

That's it. Build the app, run it, and it will correctly save the images you add to it to the database, and persist them even if you close the app and restart it. 

Direct access to a SQLite database in a generic way from any Neutralinojs desktop app achieved.
