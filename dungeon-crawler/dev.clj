(defmulti task first)

(defmethod task :default
  [[task-name]]
  (println "Unknown task:" task-name)
  (System/exit 1))

(require
  '[figwheel.main :as figwheel]
  '[nightlight.core :as nightlight]
  '[clojure.java.io :as io])

(defn delete-children-recursively! [f]
  (when (.isDirectory f)
    (doseq [f2 (.listFiles f)]
      (delete-children-recursively! f2)))
  (when (.exists f) (io/delete-file f)))

(defmethod task "nightlight"
  [[_ port]]
  (nightlight/start {:port (if port
                             (Integer/parseInt port)
                             4000)
                     :url "http://localhost:9500"}))

(defmethod task nil
  [_]
  (delete-children-recursively! (io/file "resources/public/main.out"))
  (task ["nightlight"])
  (figwheel/-main "--build" "dev"))

(require '[dungeon-crawler.start-dev])

(defmethod task "native"
  [_]
  (task ["nightlight" "4001"])
  (dungeon-crawler.start-dev/start))

(task *command-line-args*)
