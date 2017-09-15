(ns liguria.recognizer.recognizer
  (:require [liguria.shared.native-modules :as nm]
            [micro-rn.react-native :as rn :refer [alert text text-input view spacer flexer touchable-opacity]]
            [micro-rn.styles :as st]
            [micro-rn.react-navigation :as nav]
            [micro-rn.text-decorator :as text-decorator]
            [liguria.recognizer.model :as model]
            [reagent.core :as r :refer [atom]]
            [micro-rn.rn-utils :as rn-util]
            [re-frame.core :refer [subscribe dispatch dispatch-sync]]
            [liguria.shared.nlp :as nlp]
            [cljs.core.async :as async :refer [<! >! put! chan timeout]]
            [micro-rn.utils :as utils]
            [clojure.string :as string]
            [liguria.screens.recording.controls :as recording-controls]
            [liguria.shared.liguria-text :refer [liguria-text]])
  (:require-macros
   [micro-rn.macros :refer [...]]
   [cljs.core.async.macros :refer [go go-loop]]))

(def editor-ref (atom nil))

(defn- map-decorations [values]
  (->> values
       flatten
       (map #(case %
               :invert (st/color "white")
               :s      [st/line-through (st/color "#ccc")]
               :u      st/underline
               :b      st/bold
               :i      st/italic
               %))
       flatten vec
       (filter (complement nil?))))

(def create-responder
  (memoize
   (fn [id]
     (let [long-press-timeout (atom 0)]
       (rn/pan-responder-create
        {:on-start-should-set-pan-responder #(do true)
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
                                                (dispatch [::model/word-long-press id false])))})))))

(defn word [{:keys [id]}]
  (let [responder-props (rn-util/->gesture-props (create-responder id))
        animation-ref   (atom nil)
        diff-state      (atom {})]
    (nm/animatable-new-animation {:word {:from {:scale 1.2 :z-index 10} :to {:scale 1 :z-index 1}}})
    (fn [{:keys
          [id text background-gray text-style
           selected searched deleted recorded] :as data}]

      (let [recorded      selected selected false
            text-style    (-> text-style (conj
                                          (when selected [:invert
                                                          (when recorded (st/color "gold"))])
                                          (when deleted :s)) map-decorations)
            text-bg-style [(cond
                             recorded (st/background-color "gold")
                             selected (st/gray 9)
                             :else    (st/background-color "transparent"))]]
        (go
          (let [diff-data (... recorded)
                diffs (->> @diff-state
                           (filter (fn [[k v]] (and (k diff-data) (not= (k diff-data) (k v)))))
                           (map (fn [[k v] k])))]
            (when-not (empty? diffs)
              (<! (timeout 100))
              (some-> @animation-ref (.word 800)))
            (reset! diff-state diff-data)))

        [nm/animatable-view {:ref #(reset! animation-ref %)}
         [view (merge {:ref #(dispatch [::model/word-state id :ref %])} responder-props)
          [view {:style (conj text-bg-style (st/padding 4 2))}
           [rn/text {:style (conj text-style
                                  (st/font-size @(subscribe [::model/text-size])))}
            text]]]]))))

(defn icon-button [{:keys [icon label focused on-press]}]
  [touchable-opacity {:style [(st/justify-content "center")
                              (st/align-items "center")
                              (st/padding 22 0 0 0)]
                      :on-press on-press}
   [nm/icon-fa {:color "#ccc" :size 22 :name icon}]
   [text {:style [(st/color "#ccc") (st/font-size 8) (st/text-align "center")]} label]])

(defn editor-toolbar []
  [view {:style [(st/width 60)]}
   [rn/scroll-view {:style [(st/flex)]}
    [icon-button {:icon "strikethrough" :label "Mark as deleted" :on-press #(dispatch [::model/toggle-deleted])}]
    [icon-button {:icon "eraser" :label "Отменить запись" :on-press #(dispatch [::model/toggle-recorded])}]]])

(defn- build-search-rx [search-text]
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
  (let []
    (fn [{:keys [words]}]
      [nm/update-scope {:ref       #(dispatch [::model/sentence-data id :ref %])
                        :on-layout #(dispatch [::model/sentence-data id :layout (rn-util/event->layout %)])
                        :equals    =
                        :value     words}
       [rn/touchable-opacity {:active-opacity 1
                              :on-press       #(dispatch [::model/sentence-click id])}
        [view {:style [(st/width "100%") (st/margin 6 0) st/row st/wrap]}
         (doall
          (for [w words]
            ^{:key (str "word-" (:id w))} [word w]))]]])))

(defn paragraph [{:keys [id]}]
  (let [sentences   (subscribe [::model/paragraph-data id :sentences])
        hidden      (subscribe [::model/paragraph-data id :hidden])
        layout      (subscribe [::model/paragraph-data id :layout])
        search-text (subscribe [::model/search-text])]
    (fn []
      [nm/update-scope {:on-layout #(dispatch [::model/paragraph-data id :layout (rn-util/event->layout %)])
                        :ref       #(dispatch [::model/paragraph-data id :ref %])
                        :equals    =
                        :value     @sentences}
       [rn/touchable-opacity {:active-opacity 1
                              :on-press       #(dispatch [::model/paragraph-click id])}
        [view {:style [(st/padding 6 12)]}
         (doall
          (for [s (filter-sentences @sentences @search-text)]
            ^{:key (str "sentence-" (:id s))} [sentence s]))]]])))

(defn one-list-line [x]
  (let [id    (-> x .-item .-id)
        index (-> x .-index)]
    (fn []
      ^{:key (str "paragraph-" id)} [paragraph {:id id}])))

(defn text-list [{:keys [lesson]}]
  (println "build text-list component")
  (let [transcript         (subscribe [::model/transcript])
        search-text        (subscribe [::model/search-text])
        select-in-progress (subscribe [::model/select-in-progress])
        build-data-fn      (memoize
                            (fn [transcript search-text]
                              (map #(select-keys % [:id])
                                   (filter-paragraphs transcript search-text))))]
    (fn []
      [nm/update-scope {:style [(st/flex) (st/background "white")] :equals = :value [(count @transcript) @select-in-progress]}
       (if (empty? @transcript)
         [rn/view {:style [(st/flex) st/align-center st/justify-center]}
          [rn/activity-indicator {:size "large"}]]
         [nm/animatable-view {:style [(st/flex)] :animation (when-not (= 11 lesson) "fadeInUp") :duration 200}
          [rn/flat-list {:ref                       #(dispatch [::model/list-ref %])
                         :on-layout                 #(dispatch [::model/list-layout (rn-util/event->layout %)])
                         :on-scroll                 #(dispatch [::model/scroll-pos (rn-util/scroll-y %)])
                         :scroll-enabled            (not @select-in-progress)
                         :initial-num-to-render     3
                         :on-viewable-items-changed (rn-util/on-viewable-items-changed
                                                     (fn [id visible index]
                                                       (dispatch [::model/paragraph-hidden id (not visible)])))
                         :data                      (build-data-fn @transcript @search-text)
                         :render-item               #(r/as-element [one-list-line %])
                         :ItemSeparatorComponent    #(r/as-element [view {:style [(st/height 1) (st/gray 10)]}])
                         :key-extractor             #(str "paragraph-list-" (-> % .-id))}]])])))

(defn text-editor [{:keys [text lesson] :as props}]
  (let [lesson (if-not (nil? lesson) lesson 0)
        text   (if-not (nil? text) text (-> liguria-text nlp/create-paragraphs first))]
    (recording-controls/stop-recording)
    (dispatch-sync [::model/transcript []])
    (go
      (<! (utils/await-cb rn/run-after-interactions))
      (loop [parts   (nlp/create-text-parts text)
             current '()]
        (let [v     (first parts)
              parts (time (rest parts))]
          (when v
            (let [new-list (concat current [v])]
              (<! (timeout 100))
              (dispatch-sync [::model/transcript new-list])
              (recur parts new-list)))))
      (<! (timeout 500))
      (<! (utils/await-cb rn/run-after-interactions))
      (recording-controls/start-recording))
    (fn []
      [text-list props])))

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

