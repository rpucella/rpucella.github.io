# Homework 4 (Part 1): Backend

* * * 

## Due Date: <del>Monday March 22, 2021 (23h59)</del> Thursday March 25, 2021 (23h59)

- This homework is to be done in teams of two. You're welcome to
discuss with other students, but all submitted work must be original
and your own. If you use a solution from another source you must cite
it &mdash; this includes when that source is someone else helping you.

- Link to a [homework4-1.zip](./homework4-1.zip) file containing support files to use as a starting point.

- <a href="./part2.html">Part 2 can be found here</a>.


* * * 

In Homework 4, we will create the skeleton of what may be an
Instagram-like web app: a way to upload and store images with
associated comments.

In this first part of Homework 4, we're going to build the web
application server backend, with all the required routes. In Part 2,
we will build out a frontend.

I've given you a starting `server.py` Flask file in the
`homework4-1.zip` bundle. It doesn't do much except start the web
application server on port 8080. There are no routes defined.

The basic idea is pretty simple: the backend will store a list of
pictures (JPEG, to nail down a format), and for each picture a list of
comments that have been entered for that image. 

Each picture should be assigned a unique identifier when it is added
to the system, and that identifier is what's used to refer to the
picture throughout the system. I'm going to refer to that identifier
as a Picture ID. (I like to use [UUIDs](https://en.wikipedia.org/wiki/Universally_unique_identifier) as unique identifiers, which are supported by Python's [`uuid`](https://docs.python.org/3/library/uuid.html) library.)

The frontend will need ways to:

- get the list of available picture IDs
- get the picture corresponding to a Picture ID
- get all the comments associated with a Picture ID

and ways to:

- add a new picture
- add a new comment to the list of comments of a given picture ID.

Each of these operations will have a corresponding endpoint (sometime parameterized by a Picture ID).

You will have to decide how to store all of that data. You have full
freedom for what data structure to use, but I'm going to ask you to do
it purely in Python with its standard library without relying on
external tools or databases. 

The pictures themselves should probably be stored to disk, but that's
not strictly required until you get to question 6, at least.

For testing the backend, you can use any tool that can send GET and POST
requests. I suggest you learn how to use [Postman](https://www.postman.com), or
[curl](https://curl.se) if you prefer command-line
tools. You may already have curl installed on your linux or Mac OS X
distribution.



* * * 


## Question 1: Get pictures

Add a `GET` route `/pictures` to the web application server that responds with a JSON containing a single field `pictures`:

    {
      "pictures": <ARRAY of picture objects>
    }

whose value is an array of picture objects, each picture object of the form:

    { 
      "id": <Picture ID>,
      "timestamp": <Date downloaded>,
      "comments": <Number of comments>
    }

Note that we only return the number of comments from this endpoint, and not the comments themselves. The timestamp should be in [ISO 8601](https://en.wikipedia.org/wiki/ISO_8601) format.

For example (using `curl`):

    $ curl -X GET localhost:8080/pictures

    {"pictures":[{"comments":2,"id":"e93eb954-3e6a-4493-83ff-01516ec8399b","timestam
    p":"2021-03-01T09:00:00"},{"comments":0,"id":"c4c0de04-a523-4128-a0c5-c917b08332
    e0","timestamp":"2021-03-01T10:00:00"},{"comments":0,"id":"8ce846ff-3b47-4ca1-84
    7f-5eabe18c56fe","timestamp":"2021-03-02T15:00:00"},{"comments":0,"id":"f888a52f
    -25c7-4818-b41d-7c3a14583652","timestamp":"2021-03-02T16:00:00"},{"comments":0,"
    id":"86f752d5-a657-4a68-984c-bd0c80655b2e","timestamp":"2021-03-02T17:00:00"}]}



## Question 2: Get picture

Add a `GET` route `/picture/<ID>` to the web application server that responds with the JPEG picture corresponding to Picture ID `ID` as specified by the route. Easiest is to use the `send_from_directory` Flask function that can send a JPEG from the file system and takes care of setting up the response format appropriately.


## Question 3: Add picture

Add a `POST` route `/new-picture-url` to the web application server that takes a body containing a JSON object with field:

    {
       "url": <URL to picture>
    }
    
downloads the JPEG picture that the URL is pointing to, assigns it a fresh Picture ID, stores it in whatever data structure you have selected along with the timestamp recording when the picture was downloaded and added to the system. The POST should return a JSON object with one field `id` containing the Picture ID of the created picture and the timestamp:

    {
      "id": <Picture ID>,
      "timestamp": <Date downloaded>
    }
    
The picture should have no comments associated with it initially.

Python's [`urllib.request`](https://docs.python.org/3.0/library/urllib.request.html) library might be useful for this.

For example (using `curl`):

    $ curl -X POST --data '{"url": "https://cdn.britannica.com/11/196711-050-FA58D50
    D/Julius-Caesar-marble-sculpture-Andrea-di-Pietro.jpg"}' --header 'Content-Type:
    application/json' localhost:8080/new-picture-url

    {"id":"cbb11ec4-8039-4015-9291-d9d379458592","timestamp":"2021-03-15T01:19:08.59
    7106"}

and after the pictures has been added, it shows up when you do a GET on `pictures`:

    $ curl -X GET localhost:8080/pictures

    {"pictures":[{"comments":2,"id":"e93eb954-3e6a-4493-83ff-01516ec8399b","timestam
    p":"2021-03-01T09:00:00"},{"comments":0,"id":"c4c0de04-a523-4128-a0c5-c917b08332
    e0","timestamp":"2021-03-01T10:00:00"},{"comments":0,"id":"8ce846ff-3b47-4ca1-84
    7f-5eabe18c56fe","timestamp":"2021-03-02T15:00:00"},{"comments":0,"id":"f888a52f
    -25c7-4818-b41d-7c3a14583652","timestamp":"2021-03-02T16:00:00"},{"comments":0,"
    id":"86f752d5-a657-4a68-984c-bd0c80655b2e","timestamp":"2021-03-02T17:00:00"},{"
    comments":0,"id":"cbb11ec4-8039-4015-9291-d9d379458592","timestamp":"2021-03-15T
    01:19:08.597106"}]}


## Question 4: Get comments

Add a `GET` route `/comments/<ID>` to the web application server that responds with a JSON containing the comments associated with Picture ID `ID` as specified by the route. The result should be a JSON with a single field `comments`:

    {
      "comments": <ARRAY of comment objects>
    }
    
whose value is an array of comment objects, each comment object being a JSON with fields `comment` and `timestamp` holding the text of the comment and the date when the comment was created:

    {
      "comment": <Text of comment>,
      "timestamp": <Date created>
    }
    
Again, the timestamp should be in [ISO 8601](https://en.wikipedia.org/wiki/ISO_8601) format.

For example (using `curl`):

    $ curl -X GET localhost:8080/comments/e93eb954-3e6a-4493-83ff-01516ec8399b

    {"comments":[{"comment":"first","timestamp":"2021-03-08T18:00:00"},{"comment":"s
    econd","timestamp":"2021-03-08T19:00:00"}]}



## Question 5: Add comment

Add a `POST` route `/new-comment/<ID>` to the web application server that takes a body containing a JSON object with field:

    {
       "comment": <Text of comment>
    }

and adds that comment to the comments associated with picture with Picture ID `ID` (as specified in the route) along with the timestamp recording when the comment was added. The POST should return a JSON object with one field `timestamp` containing the timestamp of the newly created comment:

    {
      "timestamp": <Date created>
    }

For example (using `curl`):

    $ curl -X POST --data '{"comment": "this is a test of the emergency broadcast sy
    stem"}' --header 'Content-Type: application/json' localhost:8080/new-comment/e93
    eb954-3e6a-4493-83ff-01516ec8399b

    {"timestamp":"2021-03-15T01:22:04.594422"}

and after the comment has been added, it shows up when you do a GET on `comments` for that picture:

    $ curl -X GET localhost:8080/comments/e93eb954-3e6a-4493-83ff-01516ec8399b

    {"comments":[{"comment":"first","timestamp":"2021-03-08T18:00:00"},{"comment":"s
    econd","timestamp":"2021-03-08T19:00:00"},{"comment":"this is a test of the emer
    gency broadcast system","timestamp":"2021-03-15T01:22:04.594422"}]}

and of course the comments count returned by the GET on pictures is also affected:

    $ curl -X GET localhost:8080/pictures

    {"pictures":[{"comments":3,"id":"e93eb954-3e6a-4493-83ff-01516ec8399b","timestam
    p":"2021-03-01T09:00:00"},{"comments":0,"id":"c4c0de04-a523-4128-a0c5-c917b08332
    e0","timestamp":"2021-03-01T10:00:00"},{"comments":0,"id":"8ce846ff-3b47-4ca1-84
    7f-5eabe18c56fe","timestamp":"2021-03-02T15:00:00"},{"comments":0,"id":"f888a52f
    -25c7-4818-b41d-7c3a14583652","timestamp":"2021-03-02T16:00:00"},{"comments":0,"
    id":"86f752d5-a657-4a68-984c-bd0c80655b2e","timestamp":"2021-03-02T17:00:00"},{"
    comments":0,"id":"cbb11ec4-8039-4015-9291-d9d379458592","timestamp":"2021-03-15T
    01:19:08.597106"}]}


## Question 6: Persistence

Set things up so that your data survives stopping the server and
restarting it. That means that not only should the pictures themselves
be stored to disk, but also the Picture IDs and the comments. 

Easiest is probably to keep things in dictionaries in memory and dump
those dictionaries as JSON files whenever you add to them. The Python [`json`](https://docs.python.org/3/library/json.html) library is your friend.

When the server starts, you can then see if those JSON files exists
and if they do load them up so that all the data is back in the server's
memory.

(Yeah, I know, it's not a great way to achieve persistence - we'll see better ways soon.)

