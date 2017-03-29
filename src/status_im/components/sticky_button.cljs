(ns status-im.components.sticky-button
  (:require-macros [status-im.utils.styles :refer [defstyle defnstyle]])
  (:require [status-im.components.styles :as common]
            [status-im.utils.platform :refer [platform-specific]]
            [status-im.components.react :refer [view
                                                text
                                                touchable-highlight]]))

(def sticky-button-style
  {:flex-direction   :row
   :height           52
   :justify-content  :center
   :align-items      :center
   :background-color common/color-light-blue})

(defstyle sticky-button-label-style
  {:color   common/color-white
   :ios     {:font-size      17
             :line-height    20
             :letter-spacing -0.2}
   :android {:font-size      14
             :letter-spacing 0.5}})

(defn sticky-button [label on-press]
  [touchable-highlight {:on-press on-press}
   [view sticky-button-style
    [text {:style sticky-button-label-style
           :uppercase? (get-in platform-specific [:uppercase?])}
          label]]])