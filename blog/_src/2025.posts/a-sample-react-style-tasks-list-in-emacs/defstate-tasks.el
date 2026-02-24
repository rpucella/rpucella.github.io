
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


;; Tasks mode.
;; Mostly for illustration purposes.

(define-derived-mode tasks-mode
  special-mode "Tasks"
  "Major mode for managing a tasks list.")

(define-key tasks-mode-map (kbd "n") 'tasks-create)
(define-key tasks-mode-map (kbd "a") 'tasks-show-all)
(define-key tasks-mode-map (kbd "c") 'tasks-show-active)
(define-key tasks-mode-map (kbd "d") 'tasks-show-completed)

(defun tasks ()
  ;; need a file!
  (interactive)
  (let* ((name "*Tasks*")
         (buff (get-buffer-create name)))
    (switch-to-buffer buff)
    (tasks-mode)
    ;; Automatically called by `g` in special modes.
    (setq revert-buffer-function (lambda (&rest ignore) (tasks--render)))
    ;; To navigate buttons using tab/backtab.
    (button-mode)
    (defstate **state** (:tasks :show) 'tasks--render)
    (setstate :tasks (list (tasks--new-task "sample task 1")
                           (tasks--new-task "sample task 2"))
              :show :all)))

(defun tasks--new-task (name)
  (list :name name :completed nil))

(defun tasks--render ()
  (interactive)
  (let* ((inhibit-read-only t)
         (show (getstate :show))
         (tasks (getstate :tasks)))
    (erase-buffer)
    (insert "\nShow tasks: ")
    (tasks--simple-button show :all "ALL" 'tasks-show-all)
    (insert " ")
    (tasks--simple-button show :active "ACTIVE" 'tasks-show-active)
    (insert " ")
    (tasks--simple-button show :completed "COMPLETED" 'tasks-show-completed)
    (insert "\n\n")
    (tasks--simple-button show nil "ADD NEW TASK" 'tasks-create)
    (insert "\n\n\n")
    (dolist (task tasks)
      (let ((completed (plist-get task :completed)))
        (when (or (eq show :all)
                  (and (eq show :active) (not completed))
                  (and (eq show :completed) completed))
          (insert-button (if (plist-get task :completed) "[X]" "[ ]")
                         'face '(:underline nil :inherit default)
                         'task task
                         'action (lambda (btn) (tasks--toggle-task (button-get btn 'task))))
          (insert (format " %s\n" (plist-get task :name))))))
    (goto-char (point-min))))

(defun tasks--simple-button (show if-show text action-fn)
  (if (and (not (null if-show))
           (eq show if-show))
      (insert (concat "-" text "-"))
    (insert-button (concat "[" text "]")
                   'face '(:underline nil :inherit default)
                   'action-fn action-fn
                   'action (lambda (btn) (call-interactively (button-get btn 'action-fn))))))

(defun tasks-create (task)
  (interactive (list (read-string "New task to create: ")))
  (let* ((tasks (getstate :tasks))
         (new-task (tasks--new-task task)))
    (setstate :tasks (cons new-task tasks))))

(defun tasks-show-all ()
  (interactive)
  (setstate :show :all))

(defun tasks-show-active ()
  (interactive)
  (setstate :show :active))

(defun tasks-show-completed ()
  (interactive)
  (setstate :show :completed))

(defun tasks--toggle-task (task)
  (let ((completed (plist-get task :completed)))
    (plist-put task :completed (not completed))
    (setstate :tasks (getstate :tasks))))
