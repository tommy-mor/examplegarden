(defun cider-eval-recalling (&optional debug-it)
  "Evaluate the current toplevel form, and print result in the minibuffer.
With DEBUG-IT prefix argument, also debug the entire form as with the
command `cider-debug-defun-at-point'."
  (interactive "P")
  (let ((inline-debug (eq 16 (car-safe debug-it))))
	(cider-interactive-eval (concat "#recall\n" (cider-defun-at-point))
							nil
							(cider-defun-at-point 'bounds)
							(cider--nrepl-pr-request-map))))



;; put something like this in ur emacs.el (load-file "~/programming/examplegarden/bind.el")
;; then bind a key to it, ~~ (define-key map "\C-x\C-e" 'cider-eval-recalling)
  
