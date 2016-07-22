(ns aclaimant.compojure.meta
  (:require
    [compojure.core]))

(defn ^:private handler-with-middleware [handler {:keys [meta-middleware]}]
  (reduce
    (fn [acc f]
      (f acc))
    handler
    meta-middleware))

(defn ^:private destructure-metadata-args-list-body
  [metadata-args-list-body]
  {:pre [(>= (count metadata-args-list-body) 2)]}
  (let [fs (if (>= (count metadata-args-list-body) 3)
             [first second nnext]
             [(constantly nil) first next])]
    (mapv #(% metadata-args-list-body) fs)))

(defn ^:private method [method path metadata-args-list-body]
  (let [[metadata args-list body] (destructure-metadata-args-list-body metadata-args-list-body)]
    `(~method ~path req#
              (let [handler# (fn ~args-list ~@body)]
                ((#'handler-with-middleware handler# req#)
                 (assoc req# :metadata ~metadata))))))

(defmacro ^:private defmethods [& methods]
  `(do
     ~@(for [method' methods
             :let [f (symbol "compojure.core" (name method'))]]
         `(defmacro ~method'
            [path# & metadata-args-list-body#]
            (~'method '~f path# metadata-args-list-body#)))))

(defmethods GET POST PATCH PUT DELETE HEAD ANY OPTIONS)

(defn wrap-meta-middleware
  "Takes a handler and meta-middleware to use with the handler"
  [handler & middleware]
  (fn [req]
    (handler (update req :meta-middleware concat middleware))))
