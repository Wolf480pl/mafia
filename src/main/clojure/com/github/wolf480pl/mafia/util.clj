;
; Copyright (c) 2015 Wolf480pl <wolf480@interia.pl>
; This program is licensed under the GNU Lesser General Public License.
;
; This program is free software: you can redistribute it and/or modify
; it under the terms of the GNU Lesser General Public License as published by
; the Free Software Foundation, either version 3 of the License, or
; (at your option) any later version.
;
; This program is distributed in the hope that it will be useful,
; but WITHOUT ANY WARRANTY; without even the implied warranty of
; MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
; GNU Lesser General Public License for more details.
;
; You should have received a copy of the GNU Lesser General Public License
; along with this program.  If not, see <http://www.gnu.org/licenses/>.
;

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
