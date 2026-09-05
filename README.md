# stocktracker

A tiny system for monitoring food stocks.

## Requirements

- Java 25
- Docker, only for the MariaDB-based local runtime

The project includes a Gradle Wrapper using Gradle 9.7.1.

## Local Development With H2

```bash
./gradlew bootRun
```

The default Spring profile is `h2`.

## Tests

```bash
./gradlew test
```

## Build

```bash
./gradlew bootJar
```

## Local Runtime With MariaDB

Build the application JAR first:

```bash
./gradlew bootJar
```

Then start MariaDB and the application:

```bash
docker compose up --build
```

The MariaDB runtime uses the Spring profile `mariadb`.
