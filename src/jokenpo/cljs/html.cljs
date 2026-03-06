(ns jokenpo.cljs.html)

(defn add-event-listener [id type f]
  (let [el (js/document.getElementById id)]
    (.addEventListener el type f)))

(defn add-class [id class]
  (let [el (js/document.getElementById id)]
    (.add (.-classList el) class)))

(defn remove-class [id class]
  (let [el (js/document.getElementById id)]
    (.remove (.-classList el) class)))

(defn set-inner-text [id text]
  (let [el (js/document.getElementById id)]
    (set! (.-innerText el) text)))

(defn conditional-class [id class enabled]
  (if enabled
    (add-class id class)
    (remove-class id class)))