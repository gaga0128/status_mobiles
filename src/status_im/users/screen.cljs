(ns status-im.users.screen
  (:require-macros [status-im.utils.views :refer [defview]])
  (:require [re-frame.core :refer [subscribe dispatch dispatch-sync]]
            [status-im.components.react :refer [view
                                                text
                                                list-view
                                                list-item
                                                image
                                                linear-gradient
                                                touchable-highlight]]
            [status-im.components.toolbar :refer [toolbar]]
            [status-im.components.styles :refer [color-purple
                                                 color-white
                                                 icon-search
                                                 icon-back
                                                 icon-qr
                                                 toolbar-background1
                                                 toolbar-title-container
                                                 toolbar-title-text
                                                 button-input-container
                                                 button-input
                                                 white-form-text-input]]
            [status-im.utils.listview :as lw]
            [status-im.users.views.user :refer [user-view]]
            [status-im.i18n :refer [label]]
            [status-im.users.styles :as st]))

(def toolbar-title
  [view toolbar-title-container
   [text {:style (merge toolbar-title-text {:color color-white})}
    (label :t/switch-users)]])

(defn render-row [row _ _]
  (list-item [user-view row]))

(defn render-separator [_ row-id _]
  (list-item [view {:style st/row-separator
                    :key   row-id}]))

(defview users []
  [accounts [:get :accounts]]
  (let [accounts (conj accounts {:name (label :t/add-account)
                                 :address "0x0"
                                 :photo-path :icon_plus})]
  [view st/screen-container
   [linear-gradient {:colors ["rgba(182, 116, 241, 1)" "rgba(107, 147, 231, 1)" "rgba(43, 171, 238, 1)"]
                     :start [0, 0]
                     :end [0.5, 1]
                     :locations [0, 0.8 ,1]
                     :style  st/gradient-background}]

   [toolbar {:background-color :transparent
             :nav-action     {:image   {:source {:uri :icon_back_white}
                                        :style  icon-back}
                              :handler  #(dispatch [:navigate-back])}
             :custom-content   toolbar-title
             :action           {:image   {:style  icon-search}
                               :handler #()}}]

   [view st/user-list-container
    [list-view {:dataSource          (lw/to-datasource accounts)
                :enableEmptySections true
                :renderRow           render-row
                :renderSeparator     render-separator
                :style               st/user-list}]]]))


;(re-frame.core/dispatch [:set :view-id :users])