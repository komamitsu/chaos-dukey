# Chaos Dukey Example Test

This is an example test project for Chaos Dukey.

## Preparation

Copy a Chaos Dukey Jar file (`chaos-dukey-x.x.x-all.jar`) to this directory as the name `chaos-dukey-all.jar`).

## Execution

### With Chaos Dukey disabled

```console
./gradlew clean test
```

### With Chaos Dukey enabled

```console
CHAOS_DUKEY_ENABLED=true ./gradlew clean test
```

