(ns ^:figwheel-always flux-challenge.core
    (:require [rum.core :as rum]))

(enable-console-print!)

(println "Edits to this text should show up in your developer console.")

;; define your app data so that it doesn't get over-written on reload

(def current-planet (atom "Tatooine"))
(def current-planet-socket (js/WebSocket. "ws://localhost:4000"))

(def sith-lords (atom [{:name "Jorak Uln" :homeworld "Korriban"}
                      {:name "Skere Kaan" :homeworld "Coruscant"}
                      {:name "Na'daz" :homeworld "Ryloth"}
                      {:name "Kas'im" :homeworld "Nal Hutta"}
                      {:name "Darth Bane" :homeworld "Apatros"}]))


(defn on-js-reload []
  ;; optionally touch your app-state to force rerendering depending on
  ;; your application
  ;; (swap! app-state update-in [:__figwheel_counter] inc)
)

(rum/defc list-slot [current-planet sith-lord]
  [:li {:class (str "css-slot " (when (= current-planet (:homeworld sith-lord)) "matching-planet")) }
   [:h3 (:name sith-lord)]
   [:h6 (str "Homeworld: " (:homeworld sith-lord))]])

(rum/defc scroll-buttons []
  [:div {:class "css-scroll-buttons"}
   [:button {:class "css-button-up"}
   [:button {:class "css-button-down"}]]])

(rum/defc list-slots [sith-lords current-planet]
  [:div {:class "app-container"}
   [:div {:class "css-root"}
    [:h1 {:class "css-planet-monitor"}
     (str "Obi-Wan currently on " current-planet)]
    [:section {:class "css-scrollable-list"}
     [:ul {:class "css-slots"}
      (map (partial list-slot current-planet) sith-lords)]
      (scroll-buttons)]]])

(rum/defc app < rum/reactive [state]
  (let [planet (rum/react current-planet)]
    (list-slots @sith-lords planet)))

(rum/mount (app) (.getElementById js/document "app"))
(aset current-planet-socket "onmessage" (fn [e]
                                          (let [json (.parse js/JSON (aget e "data"))]
                                            (reset! current-planet (aget json "name")))))
