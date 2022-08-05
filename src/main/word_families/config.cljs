(ns word-families.config)

(def debug?
  ;; Perf optimisation: the `^boolean` type hint indicates to the compiler that
  ;; this value is either true or false, not some truthy or falsey value.
  ;;
  ;; As Javascript considers 0 and "" falsey and Clojure/Script does not, when
  ;; we perform a boolean check, the generated Javascript code has to ensure
  ;; that the checked value is false according to Clojure/Script's rules, not
  ;; Javascript's one.
  ;;
  ;; This generated code is less efficient than a simple boolean check. With
  ;; this type hint, ClojureScript compiler is able to generate a simple check
  ;; version of the code  and avoid the overhead.
  ;;
  ;; the goog.DEBUG constant is provided by the closure library and is true
  ;; for shadow-cljs dev mode builds, and false for release mode builds
  ;;
  ;; ClojureScript leverages the closure library
  ;; (https://developers.google.com/closure/compiler/) and the closure compiler
  ;; (https://developers.google.com/closure/compiler/) to generate optimized
  ;; Javascript
  ;;
  ;; The ClojureScript compiler generates Javascript code using the closure
  ;; library and then optimizes it using the closure compiler
  ;;
  ;; Depending on the command used, shadow-cljs builds are generated either in
  ;; dev mode (watch, compile) or in release mode (release).
  ;;
  ;; Shadow-cljs configures the closure compiler so that this constant is
  ;; evaluated to true in dev mode, and false in release mode (cf shadow.build/configure)
  ^boolean goog.DEBUG)
