(ns status-im.commands.handlers
  (:require [re-frame.core :refer [register-handler after dispatch subscribe
                                   trim-v debug]]
            [status-im.utils.handlers :as u]
            [status-im.components.react :as r]
            [status-im.utils.utils :refer [http-get toast]]
            [clojure.string :as s]
            [status-im.persistence.realm :as realm]
            [status-im.components.jail :as j]))

(defn reg-handler
  ([name handler] (reg-handler name nil handler))
  ([name middleware handler]
   (register-handler name [debug trim-v middleware] handler)))

(def commands-js "commands.js")

(defn load-commands!
  [_ [identity]]
  (if-let [{:keys [file]} (realm/get-one-by-field :commands :chat-id identity)]
    (dispatch [::parse-commands! identity file])
    (dispatch [::fetch-commands! identity])))

(defn fetch-commands!
  [db [identity]]
  (when-let [url (:dapp-url (get-in db [:chats identity]))]
    (http-get (s/join "/" [url commands-js])
              #(dispatch [::validate-hash identity %])
              #(dispatch [::loading-failed! identity ::file-was-not-found]))))

(defn dispatch-loaded!
  [db [identity file]]
  (if (::valid-hash db)
    (dispatch [::parse-commands! identity file])
    (dispatch [::loading-failed! identity ::wrong-hash])))

(defn get-hash-by-identity
  [db identity]
  (get-in db [:chats identity :dapp-hash]))

(defn get-hash-by-file
  [file]
  ;; todo tbd hashing algorithm
  (hash file))

(defn json->clj [json]
  (js->clj (.parse js/JSON json) :keywordize-keys true))

;; todo remove this
(def res {:commands  {:location {:description "Send location"
                                 :color       "#9a5dcf"
                                 :name        "location"}
                      :phone    {:description "Send phone number"
                                 :color       "#5fc48d"
                                 :name        "phone"}
                      :help     {:description "Help" :color "#9a5dcf" :name "help"}}
          :responses {:money             {:description "Send money" :color "#5fc48d" :name "money"}
                      :confirmation-code {:description "Confirmation code"
                                          :color       "#7099e6"
                                          :name        "confirmationCode"}
                      :keypair-password  {:description "Keypair password"
                                          :color       "#7099e6"
                                          :name        "keypair-password"
                                          :icon        "icon_lock_white"}}})

(defn parse-commands! [_ [identity file]]
  (j/parse identity file
           (fn [result]
             (let [commands (json->clj result)]
               ;; todo use commands from jail
               (dispatch [::add-commands identity file res])))
           #(dispatch [::loading-failed! identity ::error-in-jail %])))

(defn validate-hash
  [db [identity file]]
  (let [valid? (= (get-hash-by-identity db identity)
                  (get-hash-by-file file))]
    (assoc db ::valid-hash valid?)))

(defn add-commands
  [db [id _ {:keys [commands responses]}]]
  (-> db
      (update-in [:chats id :commands] merge commands)
      (update-in [:chats id :responses] merge responses)))

(defn save-commands-js!
  [_ [id file]]
  (realm/create-object :commands {:chat-id id :file file}))

(defn loading-failed!
  [db [id reason details]]
  (let [url (get-in db [:chats id :dapp-url])]
    (toast (s/join "\n" ["commands.js loading failed"
                         url
                         id
                         (name reason)
                         details]))))

(reg-handler :load-commands! (u/side-effect! load-commands!))
(reg-handler ::fetch-commands! (u/side-effect! fetch-commands!))

(reg-handler ::validate-hash
             (after dispatch-loaded!)
             validate-hash)

(reg-handler ::parse-commands! (u/side-effect! parse-commands!))

(reg-handler ::add-commands
             (after save-commands-js!)
             add-commands)

(reg-handler ::loading-failed! (u/side-effect! loading-failed!))
