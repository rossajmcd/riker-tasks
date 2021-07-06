(ns rikertask.core
  (:require [aero.core :refer (read-config)]
            [org.httpkit.client :refer [make-client request]]
            [clojure.data.json :refer [read-str]]
            [com.rpl.specter :refer [ALL select]]
            [marge.core :refer [markdown]])
  (:import [java.net URI]
           [javax.net.ssl SNIHostName SNIServerName SSLEngine SSLParameters])
  (:gen-class))

(def cfg (read-config "config.edn" {}))

(def app (read-config "resources/app.edn" {}))

(def token (str "Bearer " (:api-auth-token cfg)))

(def uri-prefix (:todoist-uri cfg))

(defn sni-configure
  [^SSLEngine ssl-engine ^URI uri]
  (let [^SSLParameters ssl-params (.getSSLParameters ssl-engine)]
    (.setServerNames ssl-params [(SNIHostName. (.getHost uri))])
    (.setUseClientMode ssl-engine true)
    (.setSSLParameters ssl-engine ssl-params)))

(def sni-client (make-client {:ssl-configurer rikertask.core/sni-configure}))

(defn req [filter]
  (read-str (:body @(request {:url (str uri-prefix "tasks")
            :client sni-client
            :method :get
            :query-params {"filter" filter}
            :headers {"Authorization" token}})) :key-fn keyword))

(defn select4md [m]
  (let [l-name (select [:list-name] m)
        tasks (select [:filter ALL :content] m)]
    (conj (into [] (concat [:h2] l-name [:ul])) tasks)))

(defn -main [& args]
  (println "Running Riker Task: v" (:version app))
  (let [task-lists (:task-list-filters cfg)
        rsp-lists (mapv (fn [m] (update-in m [:filter] req)) task-lists)
        md-lists (map select4md rsp-lists)
        md-header [:hr :p "kanban-plugin: basic" :hr]
        md (into [] (concat md-header (reduce into md-lists)))]
    (spit "kanban.md" (markdown md))
    (println "configuration found - markdown output created")))
