(ns messenger.comm.intercom
  (:require [cljs.core.async :as async :refer [put!]]
            [messenger.state :refer [state
                                     pub-sub-publisher]]
            [syng-im.utils.logging :as log]))

(defn publish! [topic message]
  (let [publisher (->> (state)
                       (pub-sub-publisher))]
    (put! publisher [topic message])))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn set-user-phone-number [phone-number]
  (publish! :service [:user-data :user-data/set-phone-number phone-number]))

(defn save-user-phone-number [phone-number]
  (publish! :service [:user-data :user-data/save-phone-number phone-number]))

(defn load-user-phone-number []
  ;; :service [service_name action_id args_map]
  (publish! :service [:user-data :user-data/load-phone-number nil]))

(defn load-user-whisper-identity []
  (publish! :service [:user-data :user-data/load-whisper-identity nil]))

(defn sign-up [phone-number whisper-identity handler]
  (publish! :service [:server :server/sign-up {:phone-number phone-number
                                               :whisper-identity whisper-identity
                                               :handler handler}]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

