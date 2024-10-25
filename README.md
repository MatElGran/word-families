# Word families

This is a simple web-based game where the player must associate cards from a deck with their corresponding group.
This is a MPV (Minimal Playable Version) as specified by the client, my sister, who teaches to young children and uses this to keep busy those pupils that may finish their activities sooner than anticipated.
This also provided a good excuse to get a bit more familiar with ClojureScript and Re-frame, which I enjoyed while building an earlier toy project.

## Dependencies

This project relies on [shadow-cljs](https://github.com/thheller/shadow-cljs) to build the final javascript. You'll need a Java SDK, node and npm installed, please see the [shadow-cljs](https://shadow-cljs.github.io/docs/UsersGuide.html#_installation) documentation for more information.

Once those prerequisites are satisfied, install the project dependencies by running the following command:

```shell
npm install
```
## Deployment

Deploying is entirely manual as the deployment frequency does not justify automation at this point.

The app can be built using:

```shell
npm run release
```

This will use optimized settings to build the app and place the output to the `public` directory.

The app can then be deployed by copying the content of the `public` directory to the appropriate location on your web server.

## Development

You can start a development server by running:

```shell
npm run dev
```

This will start a development server that will watch for changes to the source files and automatically rebuild the app and reload the browser. You can access the app at [http://localhost:8080](http://localhost:8080). This also runs the tests every time the code changes.

## Running tests

End-to-end tests are run using Playwright, the first time you run the tests, you will need to install the browser binaries by running:

```shell
npm run playwright install
```

Then run the tests with:

```shell
npm run e2e
```

There is also a unit test configuration using karma, which can be executed with:

```shell
npm run test
```

though none have been deemed necessary at this point.
