(ns syng-im.db
  (:require [schema.core :as s :include-macros true]))

;; schema of app-db
(def schema {:greeting s/Str})

(def default-view :chat-list)

;; initial state of app-db
(def app-db {:identity-password    "replace-me-with-user-entered-password"
             :identity             "me"
             :contacts             []
             :contacts-ids         #{}
             :current-chat-id      "console"
             :chat                 {:command      nil
                                    :last-message nil}
             :chats                {}
             :chats-updated-signal 0
             :show-actions         false
             :new-group            #{}
             :new-participants     #{}
             :signed-up            true
             :view-id              default-view
             :navigation-stack     (list default-view)
             ;; TODO fix hardcoded values
             :username             "My Name"
             :phone-number         "3147984309"
             :email                "myemail@gmail.com"
             :status               "Hi, this is my status"
             :current-tag          nil})

(def protocol-initialized-path [:protocol-initialized])
(defn chat-name-path [chat-id]
  [:chats chat-id :name])
(defn chat-color-path [chat-id]
  [:chats chat-id :color])
(defn chat-input-text-path [chat-id]
  [:chats chat-id :input-text])
(defn chat-staged-commands-path [chat-id]
  [:chats chat-id :staged-commands])
(defn chat-command-path [chat-id]
  [:chats chat-id :command-input :command])
(defn chat-command-to-msg-id-path [chat-id]
  [:chats chat-id :command-input :to-msg-id])
(defn chat-command-content-path [chat-id]
  [:chats chat-id :command-input :content])
(defn chat-command-requests-path [chat-id]
  [:chats chat-id :command-requests])
(defn chat-command-request-path [chat-id msg-id]
  [:chats chat-id :command-requests msg-id])
(def group-settings-selected-member-path [:group-settings-selected-member])
(def group-settings-show-color-picker [:group-settings-show-color-picker])
