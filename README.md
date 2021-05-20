# Questions API demo

Reactive RESTful API built on Quarkus and Vert.x

## Developing the application

You'll require a local PostgreSQL database: 

```sh
sudo podman run --ulimit memlock=-1:-1 -it --rm=true --memory-swappiness=0 --name quarkus_test -e POSTGRES_USER=quarkus_test -e POSTGRES_PASSWORD=quarkus_test -e POSTGRES_DB=quarkus_test -p 5432:5432 postgres:10.5
```
You can then run the application:
```sh
./mvnw compile quarkus:dev
```
Test that the API is working:
```sh
curl http://localhost:8080/api/questions
[{"answer":"I mean...","id":3,"question":"Why is beer better than water"},{"answer":"hunter2","id":2,"question":"What is my password"},{"answer":"42","id":1,"question":"What is the meaning of life"}][shane@helix project-anvil-api]
```

## Packaging and running the application

The application can be packaged using `./mvnw package`.
It produces the `project-anvil-api-0.1.0-runner.jar` file in the `/target` directory.
Be aware that it’s not an _über-jar_ as the dependencies are copied into the `target/lib` directory.

The application is now runnable using `java -jar target/project-anvil-api-0.1.0-runner.jar`.

## Creating a native executable

You can create a native executable using: `./mvnw package -Pnative`.

Or, if you don't have GraalVM installed, you can run the native executable build in a container using: `./mvnw package -Pnative -Dquarkus.native.container-build=true`.

Podman: `./mvnw package -Pnative -Dquarkus.native.container-build=true -Dquarkus.native.container-runtime=podman`

You can then execute your native executable with: `./target/project-anvil-api-0.1.0-runner`

If you want to learn more about building native executables, please consult https://quarkus.io/guides/building-native-image.
