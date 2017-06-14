(ns micro-rn.rn-utils)

(defn event->layout [e]
  (let [{:strs [x y width height ]} (-> e .-nativeEvent .-layout js->clj)]
    {:w width :h height :x x :y y}))
