
;; append two lists together

(defun app (x y) 
   (if (endp x) 
       y 
       (cons (car x) (app (cdr x) y))))


;; length of a list

(defun mylength (x)
  (if (endp x)
      0
      (+ 1 (mylength (cdr x)))))


;; theorem: the length of appending two lists is the sum of the lengths of the two lists

(defthm length-of-app (= (mylength (app x y)) (+ (mylength x) (mylength y))))

;; theorem: the append operation is associative

(defthm app-app (= (app (app x y) z) (app x (app y z))))



;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; INSERTION SORT
;;


;; insert into a sorted list

(defun insert (e x)
  (if (endp x) 
    (cons e x)
    (if (<= e (car x))
      (cons e x)
      (cons (car x) (insert e (cdr x))))))

(insert 3 '(1 2 4 5))


;; insertion sort: insert the first element of the list into the result
;; of sorting the rest of the list

(defun isort (x)
  (if (endp x)
    nil
    (insert (car x)
            (isort (cdr x)))))

(isort '(5 3 6 2 1 0 4))


;; first part of the sorting specification:
;;   the result is ordered

(defun ordered (x)
  (if (endp x)
    t
    (if (endp (cdr x))
      t
      (and (<= (car x) (car (cdr x)))
           (ordered (cdr x))))))

(ordered '(1 2 3))
(ordered '(1 2 2 3))
(ordered '(1 3 2))

(defthm ordered-isort (ordered (isort x)))



;; second part of the sorting specification:
;;   the result is a permutation of the input

(defun rm (e x)
  (if (consp x)
      (if (equal e (car x))
          (cdr x)
          (cons (car x) (rm e (cdr x))))
      nil))

(defun memb (e x)
  (if (consp x)
      (or (equal e (car x))
          (memb e (cdr x)))
      nil))

(defun perm (x y)
  (if (consp x)
      (and (memb (car x) y)
           (perm (cdr x) (rm (car x) y)))
      (not (consp y))))

     
(defthm perm-isort (perm (isort x) x))
