# clj-jokenpo

A few implementations of the classic Jokenpo (rock, paper, scissors) game:

- **CLI**: Terminal-based game.
- **Static website**: SSR (Server Side Rendering) using Ring, Compojure, and Hiccup.
- **CLJS**: Barebones ClojureScript with manual DOM manipulation.
- **Reagent**: Minimalist React wrapper for CLJS.
- **Reagent + Reframe**: Full state management using the dispatch/subscribe pattern.

**TODO**: Replicant / Nexus / Portfolio.

Game logic in a shared .cljc file.
Frontend with shadow-cljs.
Clojure Tests with lambdaisland/kaocha.
CLI uberjar build sample with tools.build.

*Disclaimer: I'm currently learning Clojure/ClojureScript, so these might not be good representations of the language and best practices.*

## Pre-requisites

Developed on Windows 10, with:

- Java Development Kit *(OpenJDK Temurin 25.0.1)*
- Clojure CLI *(1.12.3.1577)*
- NodeJS *(v20.19.5)*
- npm *(10.8.2)*

## How to run

### CLI
```sh
clj -M:run/cli

# or build and run a JAR file
clj -T:build/cli uber
java -jar target/jokenpo-1.0.0-cli.jar
```

### Static website
```sh
clj -M:env/static:run/static
# Open browser on http://localhost:3000
```

### CLJS
```sh
npm install
npx shadow-cljs watch cljs
# Open browser on http://localhost:8080
```

### Reagent
```sh
npm install
npx shadow-cljs watch reagent
# Open browser on http://localhost:8080
```

### Reagent + Reframe
```sh
npm install
npx shadow-cljs watch reframe
# Open browser on http://localhost:8080
```

## How to test

Run and watch all tests (JVM):
```sh
clj -M:env/static:test
```

## How to REPL

Guide for VSCode + Calva (v2.0.563):

CLI:
- Jack-In
- Select `deps.edn`
- Uncheck all aliases
- Evaluate `cli/core.clj`

Static website:
- Jack-In
- Select `deps.edn`
- Select `:env/static` alias
- Evaluate `static/core.clj`
- Evaluate `(start-dev-server!)`
- Open browser on http://localhost:3000

CLJS/Reagent/Reframe:
- Jack-In
- Select `deps.edn + shadow-cljs`
- Select `:env/frontend` alias
- Select one of the builds: `:cljs`, `:reagent` or `:reframe`
- Select again the same build as before.
- Open browser on http://localhost:8080 to connect the REPL
- To check if everything works, you can try evaluating `(println "hello")`, it should print "hello" in the browser console.