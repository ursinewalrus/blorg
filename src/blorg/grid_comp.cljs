(ns blorg.grid
  (:require 
   [reagent.core :as r]
   [blorg.utils :as butils]))

(defonce grid-state
  (r/atom {
           :context nil; (.getContext (.getElementById js/document "grid-canvas") "2d")
           :meta {
                  :height 500
                  :width 500; autobound
                  :x-offset 0
                  :y-offset 0
                  }
           :cols 10
           :rows 10
           :abstract-grid (repeat 10 (repeat 10 {} )) ;always must match :cols and :rows
           :square-size 25
           :autofit false
           :line-color "black"
           :line-width 10
           :refresh false
           
           :interact {
                      :mouse-down false
                      :mouse-down-start {} ;{:x num :y num}
                      :symbol "@"
                      }
           }
          ))

 (defn draw-grid-lines
     []
     (let [ctx (:context @grid-state)
           width (:width (:meta @grid-state)) 
           height (:height (:meta @grid-state))
           x-offset (:x-offset (:meta @grid-state))
           y-offset (:y-offset (:meta @grid-state))
           sqr-size  (:square-size @grid-state) ;(/ (:width (:meta @grid-state)) (:cols @grid-state))
           ]
       (.beginPath ctx)
       (set! (.-lineWidth ctx) (:line-width @grid-state))
       (loop [rows 0 cols 0]

         ;vert lines
         (.moveTo ctx (+ x-offset .5 (* sqr-size cols) ) (+ y-offset 0))
         (.lineTo ctx (+ x-offset .5 (* sqr-size cols)) (+ y-offset (* sqr-size (:rows @grid-state))))
         (.stroke ctx)
         
         ;hort lines
         (.moveTo ctx (+ x-offset .5) (+ y-offset .5 (* sqr-size rows)))
         (.lineTo ctx (+ x-offset (* sqr-size (:cols @grid-state))) (+ y-offset .5 (* sqr-size rows)))
         (.stroke ctx)

         (cond 
           (and  (< rows (:rows @grid-state) ) 
                 (< cols (:cols @grid-state) )) 
           (recur (inc rows) (inc cols))
           (< rows (:rows @grid-state)) (recur (inc rows) cols)
           (< cols (:cols @grid-state)) (recur rows (inc cols)) 
           ))))

(defn draw-grid
  []
  (.clearRect (:context @grid-state) 0 0  (:width (:meta @grid-state))  (:height (:meta @grid-state)) )
  (draw-grid-lines)
)

(defn scroll-grid
  [dir];{:vert 1/-1 :hort 1/-1}
  (swap! grid-state assoc-in [:meta :y-offset] (+ (/ (:vert dir) 20) (:y-offset (:meta @grid-state))))
  (swap! grid-state assoc-in [:meta :x-offset] (+ (/ (:hort dir) 20) (:x-offset (:meta @grid-state))))
  (draw-grid)
)

(defn get-grid-cord
  [x y]
  (prn (str "offset " (-> @grid-state :meta :x-offset)))
  (prn (str "loc " x))
  (prn (str "size " (:square-size @grid-state)))
  (prn (Math/floor (/ (- x (-> @grid-state :meta :x-offset)) (:square-size @grid-state))))
 ; {:x x :y y}
)

(defn handle-grid-interact
  [e]
  ;(prn (str "offfsett " (.. e -target -offsetTop)))
  (let [{x :x y :y} (get-grid-cord 
                     (- (. e -clientX) (.. e -target -offsetLeft)) 
                     (- (. e -clientY) (.. e -target -offsetTop)))]
   ; (prn (str "cords " x " : " y))

))

(defn grid-comp-divs
  []
  @grid-state
  [:<>
   (if (:context @grid-state)
     (draw-grid))
   [:div {:class "grid-tools peanut"}
    [:<>
     [butils/general-input :input "grid-cols-set" "grid-cols-set" "Columns" {:required true} "number" grid-state [:cols] "grid-tools"]
     [butils/general-input :input "grid-rows-set" "grid-rows-set" "Rows" {:required true} "number" grid-state [:rows] "grid-tools"]
     [butils/general-input :input "grid-character-set" "grid-character-set" "Symbol" {:required true :maxLength 1} "text" grid-state [:interact :symbol] "grid-tools"]
]]
   [:canvas {:id "grid-canvas" :class "border-dashed-thin outline-dashed-thin os-2" 
             :width (:width (:meta @grid-state)) 
             :height (:height (:meta @grid-state))
             :onClick (fn [e] (handle-grid-interact e))
             :onMouseDown (fn [e] 
                            (swap! grid-state assoc-in [:interact :mouse-down] true)
                            (swap! grid-state assoc-in [:interact :mouse-down-start] {:x (. e -clientX) 
                                                                                      :y (. e -clientY)}))
             :onMouseUp (fn [e] (swap! grid-state assoc-in [:interact :mouse-down] false))
             :onMouseMove (fn [e] 
                            (if (-> @grid-state :interact :mouse-down)
                              (scroll-grid {;this is going to accelerate unless clamped
                                            :hort  (- (. e -clientX) (-> @grid-state :interact :mouse-down-start :x)) 
                                            :vert  (- (. e -clientY) (-> @grid-state :interact :mouse-down-start :y)) 
                                            })))}]])




(defn grid-comp
  []
  (r/create-class {
                   :display-name "grid-canvas"
                   :component-did-mount (fn [this]
                                          (swap! grid-state assoc-in [:meta :width] (.-width (.getBoundingClientRect (.getElementById js/document "center-area"))))
                                          (swap! grid-state assoc :context (.getContext (.getElementById js/document "grid-canvas") "2d"))
                                          ;(aset (:context @grid-state) "lineWidth" (:line-width @grid-state))
                                          (draw-grid)
                                          )
                   :reagent-render (fn [this] [grid-comp-divs])

}))
