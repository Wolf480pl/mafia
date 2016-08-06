(ns com.github.wolf480pl.mafia.util)

(defn counter 
    ([] (counter 0))
    ([start] (let [cnt (atom start)]
                 #(swap! cnt inc))))

(defn assocIfAbsent [m, k, v]
    ;(merge {k v} m)
    (if (contains? m k) m (assoc m k v)))

(defn putIfAbsent [a, k, v]
    ((swap! a assocIfAbsent k v) k))
