[picocli]: https://picocli.info/
[kotlin]: https://kotlinlang.org/

# ResumakerKT

I made this sample github repo to show how to make a simple CLI using [kotlin] and [picocli].

This is a small CLI application that takes a JSON file with a specific format and turns it into a PDF file using a default HTML template.

The part where I make the shiny and cool template for the resume is currently missing, because it is the least exciting (for me) thing to do 😆.

The rest of the software should be working as expected.

## Run

```bash
./gradlew run --args="./sample.json"
```

## Tests

```bash
./gradlew test
```

There are no tests yet, feel free to contribute them if you're feeling generous 😌.
