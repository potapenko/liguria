(ns audiobooks-creator-app.screens.recording.recognizer
  (:require [audiobooks-creator-app.shared.native-modules :as nm]
            [micro-rn.react-native :as rn :refer [alert text text-input view spacer flexer touchable-opacity]]
            [micro-rn.styles :as st]
            [micro-rn.react-navigation :as nav]
            [reagent.core :as r :refer [atom]]
            [micro-rn.rn-utils :as rn-util]
            [re-frame.core :refer [subscribe dispatch dispatch-sync]]
            [audiobooks-creator-app.screens.recording.model :as model]
            [audiobooks-creator-app.screens.recording.rte-css :refer [css]]
            [audiobooks-creator-app.screens.recording.nlp :as nlp]
            [cljs.core.async :as async :refer [<! >! put! chan timeout]]
            [micro-rn.utils :as utils]
            [clojure.string :as string]
            [micro-rn.utils :as util])
  (:require-macros
   [cljs.core.async.macros :refer [go go-loop]]))

;; react-native-speech-to-text-ios

(def editor-ref (atom nil))

(defn- map-decorations [values]
  (->> (map #(case %
               :invert (st/color "white")
               :s      [st/line-through (st/color "#ccc")]
               :u      st/underline
               :b      st/bold
               :i      st/italic nil) values)

       (filter #(-> % nil? not)) flatten vec))


(defn create-responder [id]
  (let [long-press-timeout (atom 0)]
    (rn/pan-responder-create
     {:on-start-should-set-pan-responder #(not= @(subscribe [::model/mode]) :search)
      :on-pan-responder-grant            (fn [e g] (reset! long-press-timeout
                                                           (utils/set-timeout
                                                            #(dispatch [::model/word-long-press id true]) 600))
                                           (dispatch [::model/word-click id (rn-util/->getsture-state g)]))
      :on-pan-responder-move             (fn [e g]
                                           (utils/clear-timeout @long-press-timeout)
                                           (when @(subscribe [::model/long-press])
                                             (dispatch [::model/select-progress id (rn-util/->getsture-state g)])))
      :on-pan-responder-release          (fn [e g]
                                           (utils/clear-timeout @long-press-timeout)
                                           (dispatch [::model/word-release id (rn-util/->getsture-state g)])
                                           (when @(subscribe [::model/long-press])
                                            (dispatch [::model/word-long-press id false])))
      :on-pan-responder-terminate        (fn []
                                           (utils/clear-timeout @long-press-timeout)
                                           (when @(subscribe [::model/long-press])
                                            (dispatch [::model/word-long-press id false])))
      })))

(defn word [{:keys [id]}]
  (r/create-class
   {:component-will-mount   #(dispatch [::model/word-state id :mounted true])
    :component-will-unmount #(dispatch [::model/word-state id :mounted false])
    :reagent-render         (fn [{:keys [id text background-gray text-style selected searched]}]
                              (let [text-size       @(subscribe [::model/text-size])
                                    responder       (create-responder id)
                                    background-gray (and (not selected) background-gray)
                                    text-style      (-> text-style (conj (when selected :invert)) map-decorations)]
                                [view (merge
                                       {:ref   #(dispatch [::model/word-state id :ref %])
                                        :style [(st/padding 4 2)
                                                (when selected (st/gray 9))
                                                (when background-gray (st/gray 1))]}
                                       (rn-util/->gesture-props responder))
                                 [rn/text {:style (conj text-style (st/font-size text-size))} text]]))}))

(defn word-empty [text id]
  (let []
    (dispatch [::model/word-data id :ref nil])
    (fn [] [view {:style [(st/padding 4 2)]}
            [rn/text {:style [(st/font-size @(subscribe [::model/text-size]))]} text]])))

(defn icon-button [icon-name icon-text focused]
  [touchable-opacity {:style [(st/justify-content "center")
                              (st/align-items "center")
                              (st/padding 22 0 0 0)]}
   [nm/icon-fa {:color "#ccc" :size 22 :name icon-name}]
   [text {:style [(st/color "#ccc") (st/font-size 8) (st/text-align "center")]} icon-text]])

(defn editor-toolbar []
  [view {:style [(st/width 60)]}
   [rn/scroll-view {:style [(st/flex)]}
    [icon-button "question-circle-o" "Help"]
    [icon-button "eye-slash" "Hide deleted"]
    [icon-button "strikethrough" "Mark as deleted"]
    [icon-button "eraser" "Erase recording"]
    [icon-button "undo" "Undo"]
    [icon-button "repeat" "Redo"]]])

(defn build-search-rx [search-text]
  (re-pattern (string/lower-case search-text)))

(defn- filter-paragraphs [paragraphs search-text]
  (if (string/blank? search-text)
    paragraphs
    (let [rx (build-search-rx search-text)]
      (->> paragraphs
           (filter #(->> % :sentences
                         (some (fn [s] (->> s :text string/lower-case (re-find rx) nil? not)))))))))

(defn- filter-sentences [sentences search-text]
  (if (string/blank? search-text)
    sentences
    (let [rx (re-pattern (build-search-rx search-text))]
      (->> sentences (filter #(->> % :text string/lower-case (re-find rx) nil? not))))))

(defn sentence [{:keys [id p-id]}]
  (let [mode    (subscribe [::model/mode])]
    (fn [{:keys [words]}]
      [rn/touchable-opacity {:active-opacity 1
                             :on-press       #(dispatch [::model/sentence-click id])
                             :ref            #(dispatch [::model/sentence-data id :ref %])
                             :on-layout      #(dispatch [::model/sentence-data id :layout (rn-util/event->layout %)])}
       [view {:style [(st/width "100%") (st/margin 6 0) st/row st/wrap]}
        (doall
         (for [w words]
           ^{:key (str "word-" (:id w))} [word w]))]])))

(defn paragraph [{:keys [id]}]
  (let [sentences   (subscribe [::model/paragraph-data id :sentences])
        search-text (subscribe [::model/search-text])]
    (fn []
      [rn/touchable-opacity {:active-opacity 1
                             :on-press       #(dispatch [::model/paragraph-click id])
                             :on-layout      #(dispatch [::model/paragraph-data id :layout (rn-util/event->layout %)])
                             :ref            #(dispatch [::model/paragraph-data id :ref %])}
       [view {:style [(st/padding 6 12)
                      (st/border 1 (st/gray-cl 1) "solid")
                      (st/border-left 0)
                      (st/border-right 0)
                      (st/border-top 0)]}
        (doall
         (for [s (filter-sentences @sentences @search-text)]
           ^{:key (str "sentence-" (:id s))} [sentence s]))]])))

(defn one-list-line [x]
  (let [id    (-> x .-item .-id)
        index (-> x .-index)]
    (fn []
      [paragraph {:id id}])))

(defn text-editor []
  (let [transcript         (subscribe [::model/transcript])
        mode               (subscribe [::model/mode])
        select-in-progress (subscribe [::model/select-in-progress])
        search-text        (subscribe [::model/search-text])
        select-in-progress (subscribe [::model/select-in-progress])]
    (println "build text-editor")
    (dispatch [::model/text-fragment nlp/test-text3])
    (fn []
      [view {:style [(st/flex) (st/background "white")]}
       [view {:style [st/row (st/flex)]}
        [editor-toolbar]
        [view {:style [(st/gray 1) (st/width 1)]}]
        [rn/flat-list {:ref                       #(dispatch [::model/list-ref %])
                       :on-layout                 #(dispatch [::model/list-layout (rn-util/event->layout %)])
                       :on-scroll                 #(dispatch [::model/scroll-pos (rn-util/scroll-y %)])
                       :scroll-enabled            (not @select-in-progress)
                       :remove-clipped-subviews   true
                       :initial-num-to-render     5
                       :on-viewable-items-changed (fn [data]
                                                    (doseq [[id visible] (->> data .-changed
                                                                              (map (fn [e] [(-> e .-item .-id)
                                                                                            (-> e .-isViewable)])))]
                                                      (dispatch [::model/paragraph-hidden id (not visible)])))
                       :data                      (map #(select-keys % [:id]) (filter-paragraphs @transcript @search-text))
                       :render-item               #(r/as-element [one-list-line %])
                       :key-extractor             #(str "paragraph-" (-> % .-id))}]]])))

(comment
  (subscribe [::model/word-data 1 :selected])
  (subscribe [::model/word-data 1 :layout])
  (subscribe [::model/word 13])
  (-> (subscribe [::model/word 1]) deref (get 1))
  (map-decorations [:u :b])
  (word {:text "четверг" :text-style [:u :b] :editable true :selected true})
  (-> @editor-ref (.showTitle false))
  (go
    (let [[err res] (<! (await (-> @editor-ref .getContentHtml)))]
      (println ">>>" res))))

