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

## Docker Image For Deployment

The build script creates a versioned application image in the local Docker daemon and also tags it as `latest`.

Build version `1.0.0`:

```bash
./scripts/build-image.sh 1.0.0
```

This creates `stocktracker:1.0.0` and `stocktracker:latest` in the local Docker daemon. The image name, platform, and fallback tag can be adjusted using `IMAGE_NAME`, `PLATFORM`, and `IMAGE_TAG`.

To export the versioned local image as a TAR archive in the current directory:

```bash
./scripts/build-image.sh 1.0.0 --export
```

The export is named `stocktracker-1.0.0.tar` by default. Use `--export-dir <directory>` for another destination or `TAR_FILE` for an explicit file path.

## NAS Deployment With Native MariaDB

After importing the exported image into the NAS Container Manager, start the application with the deployment Compose file:

```bash
APP_IMAGE=stocktracker:1.0.0 \
DB_HOST=<nas-hostname-or-ip> \
DB_PORT=3306 \
DB_NAME=stocktracker \
DB_USERNAME=<database-user> \
DB_PASSWORD=<database-password> \
docker compose -f docker-compose.app.yml up -d
```

The application runs in the `stocktracker-app` bridge network with the default address `172.31.10.10`. The native MariaDB user must permit connections from this address. Set `APP_SUBNET` and `APP_IPV4` if this subnet conflicts with an existing Docker network on the NAS.

## Local Runtime With MariaDB

For an IDE run against a local MariaDB instance, activate the Spring profile `mariadb`. Spring Boot loads the local `.env` file automatically; it contains `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USERNAME`, and `DB_PASSWORD`. The file is excluded from Git. Use [.env.example](.env.example) as a reference when the local database differs from the default Compose configuration.

Start only the local database for an IDE run:

```bash
docker compose -f docker-compose.db.yml up -d
```

MariaDB is then available on `localhost:${DB_PORT}`. Its data is stored in `.local/mariadb`, so it is isolated from the Docker volumes of other projects.

Build the application JAR first:

```bash
./gradlew bootJar
```

Then start MariaDB and the application:

```bash
docker compose up --build
```

The MariaDB runtime uses the Spring profile `mariadb`. The application and database communicate only through the project-specific Docker network `stocktracker-app`; MariaDB is not exposed on a host port. The persistent database volume is named `stocktracker-mariadb-data`.
