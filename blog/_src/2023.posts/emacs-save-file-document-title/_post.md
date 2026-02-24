---
title: Saving a File Under the Document Title Name in Emacs
date: 2023-05-14
reading: A Maggot (by John Fowles)
---

I write and code in [Emacs](https://www.gnu.org/software/emacs/). It's too late for me now to change the muscle memory I've acquired over a
lifetime. Moreover, now that I've started to work in [Emacs Lisp](https://www.gnu.org/software/emacs/manual/eintr.html) more seriously, I have much of my
workflow directly embedded in the editor, a few keystrokes away.

For example, here's a useful routine for saving a buffer under the same title as the document
contained in that buffer, even if the title of the document changes during editing. Because this
code was written to handle [Markdown](https://daringfireball.net/projects/markdown/) documents, the title of a document here is taken to be the first
non-empty line of the document when it starts with a `#`. Every time the document is saved, the buffer
and the file it visits is automatically renamed to the new title before saving. The original file
extension is kept. All the magic is encapsulated within the [`before-save-hook`](https://www.gnu.org/software/emacs/manual/html_node/elisp/Saving-Buffers.html), which gets invoked
when a buffer is about to be saved to a file.

To enable this functionality in a buffer visiting a (Markdown) document, simply hit `M-x track-title-change`.


    (defun buffer-document-title ()
      "Return document title as defined by # <title>
    on the first non-empty line in the buffer."
      (let ((line nil)
            (found nil)
            (title-regexp (rx string-start
                              (one-or-more "#")
                              (one-or-more space)
                              (group (zero-or-more not-newline))
                              string-end)))
        (save-excursion
          (goto-char (point-min))
          (while (and (not found) (not (eobp)))
            (setq line (string-trim (buffer-substring-no-properties
                                     (line-beginning-position)
                                     (line-end-position))))
            (when (not (string-empty-p line))
              (setq found t))
            (forward-line 1))
          (and found
               (string-match title-regexp line)
               (match-string 1 line)))))

    (defun rename-current-buffer-file-to-title ()
      "Rename current buffer and file it is visiting to the
    title of the file (as determined by buffer-document-title)."
      (let* ((title (buffer-document-title))
             (old-name (buffer-file-name))
             (directory nil)
             (extension nil)
             (new-name nil))
        (when (and old-name title)
          (setq directory (file-name-directory old-name))
          (setq extension (file-name-extension old-name))
          (setq new-name (concat directory title "." extension))
          (when (not (equal new-name old-name))
            ;; Rename buffer + saved file name.
            (rename-current-buffer-file new-name)))))

    (defun rename-current-buffer-file (new-name)
      "Renames current buffer and file it is visiting."
      (let ((name (buffer-name))
            (filename (buffer-file-name)))
        (when (not (and filename (file-exists-p filename)))
          (error "Buffer '%s' is not visiting a file!" name))
        (when (get-buffer new-name)
          (error "A buffer named '%s' already exists!" new-name))
        (rename-file filename new-name 1)
        (rename-buffer new-name)
        (set-visited-file-name new-name)
        (set-buffer-modified-p nil)
        (message "File '%s' successfully renamed to '%s'."
                 name (file-name-nondirectory new-name))))

    (defun track-title-change ()
      "Track the title of a buffer and use the title as filename when saving."
      (interactive)
      (let ((filename (buffer-file-name)))
        (when (not (and filename (file-exists-p filename)))
          (error "Buffer '%s' is not visiting a file!" name))
        (add-hook 'before-save-hook 'rename-current-buffer-file-to-title nil t)))


Function `rename-current-buffer-file` in the code above is taken directly from [What the .emacs.d](http://whattheemacsd.com/file-defuns.el-01.html).

Though it was written to handle Markdown documents, there is nothing particularly Markdown-specific
in this code. If you want to apply this other kind of documents, you only need to redefine
`buffer-document-title` to identify what you consider the title of the document. Of course, a more robust
implementation would allow you to have different ways to determine the document title based on the
major mode of the buffer. Let me leave that as an exercise to the reader.
