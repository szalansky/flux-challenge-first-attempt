(ns flux-challenge.store
  (:require-macros [cljs.core.async.macros :refer [go go-loop]])
  (:require [cljs-http.client :as http]
            [cljs.core.async :refer [chan <! >!]]))

(def sith-lords (atom []))

(defn reset-lords-list! []
  (reset! sith-lords []))

(defn fetch-until-limit [url idx limit]
  (when (< idx limit)
    (go []
        (let [response (<! (http/get url {:with-credentials? false}))]
          (swap! sith-lords conj (:body response))
          (fetch-until-limit (:url (:apprentice (:body response))) (inc idx) limit)))))

(defn fetch-sith-lord [url]
  (if url
    (http/get url {:with-credentials? false})
    (let [null-sith-lord {:body {:null-sith-lord true :homeworld nil :apprentice {:url nil}}}
          response (chan)]
      (go []
          (>! response null-sith-lord))
      response)))

(defn fetch-apprentices [url start end]
  (go-loop [url url
            start start
            end end]
           (when (< start end)
             (let [response (<! (fetch-sith-lord url))]
               (swap! sith-lords conj (:body response))
               (recur (:url (:apprentice (:body response))) (inc start) end)))))

(defn fetch-masters [url start end]
  (go-loop [url url
            start start
            end end]
           (when (< start end)
             (let [response (<! (fetch-sith-lord url))]
               (reset! sith-lords (vec (take 5 (into [(:body response)] @sith-lords))))
               (recur (:url (:master (:body response))) (inc start) end)))))

(defn init [url]
  (go []
      (let [response (<! (http/get url {:with-credentials? false}))]
        (swap! sith-lords conj (:body response))
        (fetch-apprentices (:url (:apprentice (:body response))) 1 5))))
