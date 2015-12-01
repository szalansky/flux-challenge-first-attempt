(ns flux-challenge.store
  (:require-macros [cljs.core.async.macros :refer [go go-loop]])
  (:require [cljs-http.client :as http]
            [cljs.core.async :refer [chan <! >!]]))

(def sith-lords (atom []))

(defn reset-lords-list! []
  (reset! sith-lords []))

(defn fetch-sith-lord [url]
  (if url
    (http/get url {:with-credentials? false})
    (let [null-sith-lord {:body {:null-sith-lord true :homeworld nil :apprentice {:url nil}}}
          response (chan)]
      (go []
          (>! response null-sith-lord))
      response)))

(defn fetch-masters [url n]
  (go-loop [url url
            start 0
            end n
            masters @sith-lords]
           (if (< start end)
             (let [response (<! (fetch-sith-lord url))]
               (recur (:url (:master (:body response))) (inc start) end (into [(:body response)] masters)))
             (reset! sith-lords (vec (take 5 masters))))))

(defn fetch-apprentices [url n]
  (go-loop [url url
            start 0
            end n
            apprentices @sith-lords]
           (if (< start end)
             (let [response (<! (fetch-sith-lord url))]
               (recur (:url (:apprentice (:body response))) (inc start) end (conj apprentices (:body response))))
             (reset! sith-lords (vec (take-last 5 apprentices))))))

(defn init [url]
  (go []
      (let [response (<! (http/get url {:with-credentials? false}))]
        (swap! sith-lords conj (:body response))
        (fetch-apprentices (:url (:apprentice (:body response))) 4))))
