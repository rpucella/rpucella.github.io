---
title: Chaining Callbacks In Emacs Lisp With Macros
date: 2025-12-20
reading: Butcher's Crossing (By John Williams)
---

If you program Emacs using the [`request.el`](https://github.com/tkf/emacs-request) package (which provides a nice interface for making HTTP requests from within Emacs), you probably already ran into the problem of callback hell, familiar to backend developers that grew up with Nodejs in the heydays of the rise of Javascript in the backend.

Basically, the ``request`` function from the `request.el` package works asynchronously: you give it a callback function through parameter `:success` to be called with the result of the request when it completes. The function call itself returns immediately. That means that whatever post-processing is required on the result, including where to show said result, needs to happen in the callback function. If that callback function needs to call another asynchronous function (such as making another `request`) then you get a chain of callbacks that becomes annoying to manage.

For example, and simply for illustration purposes, consider the following two functions that use a [timer](https://www.gnu.org/software/emacs/manual/html_node/elisp/Timers.html) to simulate the fact that they make take time to produce their result and therefore may have been implemented asynchronously by some package creator:

    (defun generate-value (v callback)
      ;; Generate a value v and 
      ;; send it to the given callback.
      (run-with-timer 3 nil callback v))

    (defun sum (a b callback)
      ;; Calculate the sum of the two arguments and 
      ;; send the result to the given callback.
      (run-with-timer 3 nil callback (+ a b)))

To use these functions, you need to write something like:

    (defun test-raw ()
      (interactive)
      (generate-value 10 
        (lambda (a)
          (generate-value 20 
            (lambda (b)
              (sum a b 
                (lambda (c)
                  (message (format "Result = %s" c)))))))))

This interactive command waits about 9 seconds to finally show `Result = 30` in the minibuffer. The code is nearly unreadable, though. Thankfully, through the use of macros, we can make the code a bit more readable. Here's a macro that lets you call functions that expect a callback, and call them as though they were returning results that you can use immediately:

    ;; -*- lexical-binding: t; -*-

    (defmacro async-let* (bindings &rest body)
      (if bindings
          (let* ((binding (car bindings))
                 (res-var (car binding))
                 (exp (cadr binding))
                 (fn-name (car exp))
                 (fn-args (cdr exp)))
            `(,fn-name
              ,@fn-args
              (lambda (,res-var) (async-let* ,(cdr bindings) ,@body))))
        `(progn ,@body)))

(This macro requires lexical binding, hence the `lexical-binding: t` annotation at the top of the file containing the macro.) 

Here is the test above, written with this macro:

    (defun test-mac ()
      (interactive)
      (async-let* ((a (generate-value 10))
                   (b (generate-value 20))
                   (c (sum a b)))
                  (message (format "Result = %s" c))))

Observe that instead of passing a callback to `generate-value` and to `sum`, we simply call the functions as though it returned the value passed to the callback. Each entry in `async-let*` binds a variable to the result of calling such an _asynchronous_ function — basically, the macro inserts the callback implicitly as the last argument to each call. Each entry is executed sequentially, and has access to the variable set by the previous calls. The body of the `async-let*` is evaluated with all the variables available, and needs to perform the final action on the values computed. Of course, because this is all done asynchronously, the final evaluation cannot return a useful value — where would it return that value to anyway?

The functions called within `async-let*` must take their callback as the last argument. If your function doesn't, you must wrap it so that arguments get reordered appropriately. 

The macro works by literally turning the entries in the `async-let*` into the kind of chained functions calls seen in `test-raw`. You can use [`macroexpand-all`](https://www.gnu.org/software/emacs/manual/html_node/elisp/Expansion.html) to see what it does in more detail:

    (macroexpand-all '(async-let* ((a (generate-value 10))
                                  (b (generate-value 20))
                                  (c (sum a b)))
                                 (message (format "Result = %s" c))))

yields

    (generate-value 10 #'(lambda (a) (generate-value 20 #'(lambda ... ...))))

There's obviously improvements you could make to the macro, starting with more error checking. 

## Footnote

If you have any experience with modern Javascript, you might recognize the `async-let*` macro as a form of [`await`](https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Operators/await) for `async` functions with an implicit promise wrapper around the function call. No surprise there, since `await` was definitely the inspiration. It is yet one more illustration of the expressive power of macros that you can readily implement a reasonable form of `await` directly within the Emacs Lisp language.
