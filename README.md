# MongoDB Index Inspector

Inspect MongoDB indexes and compare them between environments.

## Requirements

* [Java 17+](https://adoptium.net/)
* [Clojure](https://clojure.org/)

## Development

If you're a beginner to Clojure and don't have a favorite setup yet, give [Visual Studio Code](https://code.visualstudio.com/) in combination with the [Calva extension](https://calva.io/) a try.

Once you've installed Visual Studio Code and Calva, [connect Calva to the project](https://calva.io/connect/) using the project type `deps.edn` and the alias `:dev`, and start development.

A convenient way to get started is opening `dev/user.clj` and evaluating expressions using `alt+enter`.

## Running the app

Run `clojure -X:run` to start the app.
This requires Clojure.

Alternatively, run `clojure -X:uberjar` to create an uberjar, followed by `java -jar target/mongo-index-inspector-<version>-standalone.jar` to start the application.
Starting the application like this doesn't require Clojure, only Java.
