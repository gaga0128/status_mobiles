(ns status-im.chat.handlers.animation
  (:require [re-frame.core :refer [register-handler after dispatch]]
            [re-frame.middleware :refer [path]]
            [status-im.models.commands :as commands]
            [status-im.handlers.content-suggestions :refer [get-content-suggestions]]
            [status-im.chat.styles.message-input :refer [input-height]]
            [status-im.chat.styles.response :refer [request-info-height response-height-normal]]
            [status-im.chat.styles.response-suggestions :as response-suggestions-styles]
            [status-im.constants :refer [response-input-hiding-duration]]))

(def zero-height input-height)

(defn animation-handler
  ([name handler] (animation-handler name nil handler))
  ([name middleware handler]
   (register-handler name [(path :animations) middleware] handler)))

(animation-handler :animate-cancel-command
  (fn [db _]
    (assoc db
      :command? false
      :to-response-height zero-height
      :messages-offset 0)))

(animation-handler :finish-animate-response-resize
  (fn [db _]
    (let [fixed (:to-response-height db)]
      (assoc db :response-height-current fixed
                :response-resize? false))))

(animation-handler :set-response-height
  (fn [db [_ value]]
    (assoc db :response-height-current value)))

(animation-handler :animate-response-resize
  (fn [db _]
    (assoc db :response-resize? true)))

(defn get-response-height [db]
  (let [command (commands/get-chat-command db)
        text (commands/get-chat-command-content db)
        suggestions (get-content-suggestions command text)
        suggestions-height (reduce + 0 (map #(if (:header %)
                                              response-suggestions-styles/header-height
                                              response-suggestions-styles/suggestion-height)
                                            suggestions))]
    (+ zero-height
       (min response-height-normal (+ suggestions-height request-info-height)))))

(defn update-response-height [db]
  (assoc-in db [:animations :to-response-height] (get-response-height db)))

(register-handler :animate-show-response
  (after #(dispatch [:animate-response-resize]))
  (fn [db _]
    (-> db
        (assoc-in [:animations :response-height-current] zero-height)
        (assoc-in [:animations :command?] true)
        (assoc-in [:animations :messages-offset] request-info-height)
        (update-response-height))))

(animation-handler :set-response-max-height
  (fn [db [_ height]]
    (let [prev-height (:response-height-max db)]
      (if (not= height prev-height)
        (let [db (assoc db :response-height-max height)]
          (if (= prev-height (:to-response-height db))
            (assoc db :to-response-height height
                      :response-height-current height)
            db))
        db))))

(animation-handler :on-drag-response
  (fn [db [_ dy]]
    (let [fixed (:to-response-height db)]
      (assoc db :response-height-current (- fixed dy)
                :response-resize? false))))

(register-handler :fix-response-height
  (fn [db [_ dy vy current]]
    (let [max-height             (get-in db [:animations :response-height-max])
          ;; todo magic value
          middle                 270
          moving-down?           (pos? vy)
          moving-up?             (not moving-down?)
          under-middle-position? (<= current middle)
          over-middle-position?  (not under-middle-position?)
          min-height             (+ zero-height request-info-height)
          new-fixed              (cond (and under-middle-position? moving-down?)
                                       min-height

                                       (and under-middle-position? moving-up?)
                                       middle

                                       (and over-middle-position? moving-down?)
                                       middle

                                       (and over-middle-position? moving-up?)
                                       max-height)]
      (-> db
          (assoc-in [:animations :to-response-height] new-fixed)
          (update-in [:animations :response-height-changed] inc)))))