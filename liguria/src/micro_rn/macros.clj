(ns micro-rn.macros
  (:require [clojure.string :as string]))

(defmacro ... [& vars]
  (let [vars (if (-> vars first vector?) (first vars) vars)]
    (let [m (apply merge
                   (for [v vars]
                     {(keyword (str v)) v}))]
      m)))

