(ns super-koalio.start-dev
  (:require [super-koalio.start :as start]
            [super-koalio.core :as c]
            [orchestra.spec.test :as st]
            [expound.alpha :as expound]
            [clojure.spec.alpha :as s]
            [play-cljc.gl.core :as pc]
            [paravim.start]
            [paravim.core])
  (:import [org.lwjgl.glfw GLFW]
           [super_koalio.start Window]))

(defn start []
  (st/instrument)
  (set! s/*explain-out* expound/printer)
  (let [window (start/->window)
        game (pc/->game (:handle window))
        paravim-utils (paravim.start/init game)
        *focus-on-game? (atom true)]
    (extend-type Window
      start/Events
      (on-mouse-move [{:keys [handle]} xpos ypos]
        (if @*focus-on-game?
          (start/on-mouse-move! handle xpos ypos)
          (paravim.start/on-mouse-move! paravim-utils handle xpos ypos)))
      (on-mouse-click [{:keys [handle]} button action mods]
        (if @*focus-on-game?
          (start/on-mouse-click! handle button action mods)
          (paravim.start/on-mouse-click! paravim-utils handle button action mods)))
      (on-key [{:keys [handle]} keycode scancode action mods]
        (when (and (= action GLFW/GLFW_PRESS)
                   (= keycode GLFW/GLFW_KEY_ESCAPE)
                   (= (paravim.core/get-mode) 'NORMAL))
          (swap! *focus-on-game? not))
        (if @*focus-on-game?
          (start/on-key! handle keycode scancode action mods)
          (paravim.start/on-key! paravim-utils handle keycode scancode action mods)))
      (on-char [{:keys [handle]} codepoint]
        (if @*focus-on-game?
          (start/on-char! handle codepoint)
          (paravim.start/on-char! paravim-utils handle codepoint)))
      (on-tick [this game]
        (cond-> (c/tick game)
                (not @*focus-on-game?)
                paravim.core/tick)))
    (start/start game window)))

