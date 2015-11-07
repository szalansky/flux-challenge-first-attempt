(ns flux-challenge.store
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [cljs-http.client :as http]
            [cljs.core.async :refer [<!]]))

(def sith-lords (atom []))

(defn reset-lords-list! []
  (reset! sith-lords []))

(defn fetch-until-limit [url idx limit]
  (when (< idx limit)
    (go []
        (let [response (<! (http/get url {:with-credentials? false}))]
          (swap! sith-lords conj (:body response))
          (fetch-until-limit (:url (:apprentice (:body response))) (inc idx) limit)))))

(defn init [url]
  (go []
      (let [response (<! (http/get url {:with-credentials? false}))]
        (swap! sith-lords conj (:body response))
        (fetch-until-limit (:url (:apprentice (:body response))) 1 8))))
