(ns aclaimant.compojure.meta
  (:require
    [compojure.core]))

(defn ^:private handler-with-middleware [handler {:keys [meta-middleware]}]
  (reduce
    (fn [acc f]
      (f acc))
    handler
    meta-middleware))

(defn ^:private method [method path args-list metadata-body]
  (let [[metadata body] (if (map? (first metadata-body))
                          [(first metadata-body) (rest metadata-body)]
                          [nil metadata-body])]
    `(~method ~path req#
              (let [handler# (fn ~args-list ~@body)]
                ((#'handler-with-middleware handler# req#)
                 (assoc req# :metadata ~metadata))))))

(defn wrap-meta-middleware
  "Takes a handler and meta-middleware to use with the handler"
  [handler & middleware]
  (fn [req]
    (handler (assoc req :meta-middleware middleware))))

(defmacro GET [path args-list & metadata-body]
  (method 'compojure.core/GET path args-list metadata-body))
(defmacro POST [path args-list & metadata-body]
  (method 'compojure.core/POST path args-list metadata-body))
(defmacro PATCH [path args-list & metadata-body]
  (method 'compojure.core/PATCH path args-list metadata-body))
(defmacro PUT [path args-list & metadata-body]
  (method 'compojure.core/PUT path args-list metadata-body))
(defmacro DELETE [path args-list & metadata-body]
  (method 'compojure.core/DELETE path args-list metadata-body))
(defmacro HEAD [path args-list & metadata-body]
  (method 'compojure.core/DELETE path args-list metadata-body))
(defmacro ANY [path args-list & metadata-body]
  (method 'compojure.core/ANY path args-list metadata-body))
(defmacro OPTIONS [path args-list & metadata-body]
  (method 'compojure.core/OPTIONS path args-list metadata-body))
