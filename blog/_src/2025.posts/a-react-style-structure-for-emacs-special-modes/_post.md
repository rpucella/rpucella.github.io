---
title: A React-Style Structure for Emacs Special Modes
date: 2025-12-29
reading: Career of Evil (by Robert Galbraith)
---

Emacs [special
modes](https://www.gnu.org/software/emacs/manual/html_node/elisp/Basic-Major-Modes.html) are major
buffer modes that provide functionality that are not strictly speaking related to editing files. For
example, [`dired`](https://www.gnu.org/software/emacs/manual/html_node/emacs/Dired.html)
(to list files in a directory and manipulate them) or [`magit`](https://www.masteringemacs.org/article/introduction-magit-emacs-mode-git) (to issue `git` commands on local repository). I internalize special modes as little apps within Emacs that use a buffer to provide a user interface for some specific functionality. 

Creating a special mode is straightforward: 

    (define-derived-mode counter-mode
      special-mode "Counter"
      "Major mode for a simple counter.")

This defines a special mode called `counter-mode` which I will flesh out below. It shows a simple
counter in a dedicated buffer that can be incremented and reset.

By itself, defining a special mode like I did above does nothing. The real action is done by writing a function that creates a new buffer, sets it to the given mode, and then populates the buffer to show whatever interface we have in mind, from rendering output to handling mode-specific key bindings and on-screen interactions.

There are several ways of creating interfaces for special modes. I have had some success structuring that code in the same way you would structure a React-based web application: populate the content of the buffer based on the value of state variables, and repopulate that content when the value of those state variables updates after a user or system action.

_(I know, I know, React relies on a virtual DOM to determine what components to update so that the whole screen does not need redrawing every time, and I'm not doing any of that. For the modes that I have worked on, this has not been an issue. Your mileage may vary.)_

For the counter example I started above, we only need a single state variable, `counter--count`. It is buffer local and is initialized to 0. A `counter` command creates a new buffer, sets the buffer to `counter-mode`, sets up the `counter--count` buffer local variable, and then calls `counter--render` to render the buffer based on the content of the state variable:

    (defun counter ()
      (interactive)
      (let* ((name "*Counter*")
             (buff (get-buffer-create name)))
        (switch-to-buffer buff)
        (counter-mode)
        (make-local-variable 'counter--count)
        (setq counter--count 0)
        (counter-render)))
        
Function `counter--render` does what it says on the tin: it renders the buffer after erasing it:

    (defun counter--render ()
      (interactive)
      (let ((inhibit-read-only t))
        (erase-buffer)
        (insert (format "Count = %d\n\nPress i to increment, r to reset\n" counter--count))))

Since special modes are read-only by default, we need to make the buffer writable to erase it and insert the output into it. Note that we do not pass the state to the render function. It reads it off the state variable. That will be important later.

To update the state variable, we write a function `counter--setstate` that takes a new value for the state variable, sets it, and calls `counter--render`:

    (defun counter--setstate (new-count)
      (setq counter--count new-count)
      (counter--render))

To increment and reset the counter, we write two commands that are bound to specific keys in a `counter-mode` buffer. This is done by adding the bindings to the `counter-map-map` keymap which is defined automatically via the `define-derived-mode` macro above:

    (define-key counter-mode-map (kbd "i") 'counter-increment)
    (define-key counter-mode-map (kbd "r") 'counter-reset)

    (defun counter-increment ()
      (interactive)
      (let ((count counter--count))
        (counter--setstate (+ count 1))))

    (defun counter-reset()
      (interactive)
      (counter--setstate 0))

This all works great. Run `M-x counter` and a new counter is created showing a 0 counter, and hitting `i` increments the count and `r` resets it back to 0. Like in any special mode, `q` quits the window, and `g` refreshes it.

![Screenshot](./screenshot.png)


## A macro to abstract state variables and rendering

Can we generalize the code above and find the right abstraction that would let us create new more complex special modes without having to write too much boilerplate code?

The key ideas are: the notion of a state associated with the buffer, and the notion that updating that state should trigger a render of the buffer. Here's a macro and two helper functions that capture these notions:

    (defmacro defstate (name vars render)
      "Macro to define a new local variable with a given NAME holding the state.
    The state is a property list with the properties ('state properties') listed in VARS.
    Record the RENDER function so that it can be invoked when the state is updated."
      (let ((proplist (mapcan (lambda (x) (list x nil)) vars)))
        `(progn
           (make-local-variable (quote ,name))
           (make-local-variable '**defstate--name**)
           (make-local-variable '**defstate--render**)
           (setq ,name (quote,proplist))
           (setq **defstate--name** (quote ,name))
           (setq **defstate--render** ,render))))

    (defun getstate (prop)
      "Get the value of a state property."
      (plist-get (symbol-value **defstate--name**) prop))

    (defun setstate (&rest props)
      "Update some state properties and invoke the render function."
      (let ((rest props))
        (when (> (mod (length props) 2) 0)
            (error "arguments to setstate must have even length"))
        (while (not (null rest))
          (plist-put (symbol-value **defstate--name**) (car rest) (cadr rest))
          (setq rest (cddr rest)))
        (funcall **defstate--render**)))
      
The `defstate` macro creates a new state variable, which is implemented as a property list with the given properties initialized to `nil`. Those properties I call _state properties_. The macro also takes the render function to call when the state is updated.

To query and update the state, two functions are provided: `getstate` returns the value of one of the state properties, while `setstate` updates one or more state property before automatically calling the provided render function to clear and recreate the buffer. 
    
This is not a particularly safe abstraction — it is easy enough to just access the property list stored in the state variable without triggering a render, wherein what is shown in the buffer is not in sync with the content of the state and lead to unpredictable behavior for the user, but it's good enough for business. 

Here is the full code of the sample counter mode using the `defstate` abstraction.

    (define-derived-mode counter-mode
      special-mode "Counter"
      "Major mode for a simple counter.")

    (define-key counter-mode-map (kbd "i") 'counter-increment)
    (define-key counter-mode-map (kbd "r") 'counter-reset)

    (defun counter ()
      (interactive)
      (let* ((name "*Counter*")
             (buff (get-buffer-create name)))
        (switch-to-buffer buff)
        (counter-mode)
        (defstate **state** (:count) 'counter--render)
        (setstate :count 0)))

    (defun counter--render ()
      (interactive)
      (let ((inhibit-read-only t))
        (erase-buffer)
        (insert (format "\nCount = %d\n\nPress i to increment, r to reset\n\n" (getstate :count)))))

    (defun counter-increment ()
      (interactive)
      (let ((count (getstate :count)))
        (setstate :count (+ count 1))))

    (defun counter-reset()
      (interactive)
      (setstate :count 0))
      
Next time, I will build something a bit more substantial with this.
