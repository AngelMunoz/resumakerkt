[picocli]: https://picocli.info/
[kotlin]: https://kotlinlang.org/

# A simple skeleton CLI

I made this sample github repo to show how to make a simple CLI using [kotlin] and [picocli].

It includes an `ApplicationEnvironment` (Manual DI Container), an action handler, and a single command with some CLI options

The main idea is to provide multiple handlers as part of a transformation pipeline to get to the final result, each handler also has to be provided of required arguments (as data classes) and services (that are hosted in the `ApplicationEnvironment`)

This allows the handlers to be tested in isolation, and the application to be tested as a whole.

## Run

```bash
./gradlew run --args="-p ./"
```

## Tests

```bash
./gradlew test
```

The tests are composed of two different kind of tests.

1. Unit tests for the handlers
2. Integration tests for the application

The handlers are unit tested by providing them with a fake service and a specific set of parameters to control the result of the operation.

The integration tests are done by running the CLI Command in question with a fake `ApplicationEnvironment` with a set of specific arguments.

Since CLI applications are often working with stdin/stdout, to ease testing your outputs you should provide a logger interface so you can fake it in your tests and corroborate that what you're logging to the console is what you actually want.
