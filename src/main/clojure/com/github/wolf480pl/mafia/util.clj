(ns com.github.wolf480pl.mafia.util)

(defn counter 
    ([] (counter 0))
    ([start] (let [cnt (atom start)]
                 #(swap! cnt inc))))
