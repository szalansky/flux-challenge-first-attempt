(ns ^:figwheel-always flux-challenge.core
    (:require [rum.core :as rum]
              [flux-challenge.store :as store]))

(enable-console-print!)

(println "Edits to this text should show up in your developer console.")

;; define your app data so that it doesn't get over-written on reload

(def current-planet (atom "Tatooine"))
(def current-planet-socket (js/WebSocket. "ws://localhost:4000"))

(defn on-js-reload []
  (store/reset-lords-list!))

(rum/defc list-slot [current-planet sith-lord]
  [:li {:class (str "css-slot " (when (= current-planet (:name (:homeworld sith-lord))) "matching-planet")) }
   [:h3 (:name sith-lord)]
   [:h6 (str "Homeworld: " (:name (:homeworld sith-lord)))]])

(rum/defc scroll-buttons [sith-lords]
  (let [up-disabled (not (:url (:master (first sith-lords))))
        down-disabled (not (:url (:apprentice (last sith-lords))))]
  [:div {:class "css-scroll-buttons"}
   [:button {:class (str "css-button-up " (when up-disabled "css-button-disabled"))
             :disabled up-disabled
             :on-click (fn [_] (store/fetch-masters (:url (:master (first sith-lords))) 1 3))}
   [:button {:class (str "css-button-down " (when down-disabled "css-button-disabled"))
             :disabled down-disabled
             :on-click (fn [_] (store/fetch-apprentices (:url (:apprentice (last sith-lords))) 1 3))}]]]))

(rum/defc list-slots [sith-lords current-planet]
  [:div {:class "app-container"}
   [:div {:class "css-root"}
    [:h1 {:class "css-planet-monitor"}
     (str "Obi-Wan currently on " current-planet)]
    [:section {:class "css-scrollable-list"}
     [:ul {:class "css-slots"}
      (map (partial list-slot current-planet) sith-lords)]
      (scroll-buttons sith-lords)]]])

(rum/defc app < rum/reactive [state]
  (let [planet (rum/react current-planet)
        sith-lords (rum/react store/sith-lords)]
    (list-slots sith-lords planet)))

(rum/mount (app) (.getElementById js/document "app"))
(store/init "http://localhost:3000/dark-jedis/3616")
(aset current-planet-socket "onmessage" (fn [e]
                                          (let [json (.parse js/JSON (aget e "data"))]
                                            (reset! current-planet (aget json "name")))))
