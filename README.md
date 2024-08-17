# Chaos Dukey

This is a Java agent to inject chaos delays and/or failures into specific Java classes and methods.

## Download the JAR file

Download the latest JAR file `chaos-dukey-<VERSION>-all.jar` from https://github.com/komamitsu/chaos-dukey/releases.

## Usage

Set `-javaagent` Java option to the downloaded JAR file with the parameters.

### Parameters

| Parameter                   | Description                                                                                                                       | Default value                     |
|-----------------------------|-----------------------------------------------------------------------------------------------------------------------------------|-----------------------------------|
| `delay.typeNamePattern`     | [For delay injection] Type name pattern to filter classes and interfaces. Supports Java's regex format.                           | No targeted classes or interfaces |
| `delay.methodNamePattern`   | [For delay injection] Method name pattern to filter methods. Supports Java's regex format.                                        | No targeted methods               |
| `delay.waitMode`            | [For delay injection] `RANDOM`: waits for random duration between 1 ms and `maxDelayMillis`. `FIXED`: waits for `maxDelayMillis`. | `RANDOM`                          |
| `delay.ppm`                 | [For delay injection] Parts per million (ppm) chance that a delay injection occurs. Cannot be specified with `percentage`.        | 20000 ppm (2%)                    |
| `delay.percentage`          | [For delay injection] Percentage chance that a delay injection occurs. Cannot be specified with `ppm`.                            | 2% (20000 ppm)                    |
| `delay.maxDelayMillis`      | [For delay injection] Maximum delay in millis.                                                                                    | 500 ms                            |
| `failure.typeNamePattern`   | [For failure injection] Type name pattern to filter classes and interfaces. Supports Java's regex format.                         | No targeted classes or interfaces |
| `failure.methodNamePattern` | [For failure injection] Method name pattern to filter methods. Supports Java's regex format.                                      | No targeted methods               |
| `failure.exception`         | [For failure injection] Exception thrown from the target methods.                                                                 | `java.lang.RuntimeException`      |
| `failure.ppm`               | [For failure injection] Parts per million (ppm) chance that a delay injection occurs. Cannot be specified with `percentage`.      | 20000 ppm (2%)                    |
| `failure.percentage`        | [For failure injection] Percentage chance that a delay injection occurs. Cannot be specified with `ppm`.                          | 2% (20000 ppm)                    |
| `debug`                     | Specifies whether to output debug information to STDERR.                                                                          | `false`                           |
| `configFile`                | The path to properties file that contains the parameters above. Other parameters will be ignored if this parameter is specified.  | ----                              |

### Example

#### Without a config file

```console
java -javaagent:/path/to/chaos-dukey-x.x.x-all.jar=delay.typeNamePattern=^org\.komamitsu\.transaction\.(?:LockManager|OCCManager)$,delay.methodNamePattern=^(?:lock.*|cas.*)$,delay.waitMode=RANDOM,delay.percentage=1,delay.maxDelayMillis=1500,failure.typeNamePattern=^org\.komamitsu\.repository\..*Repository$,failure.methodNamePattern=^(?:get.*|save.*|delete.*)$,failure.ppm=1000,failure.exceptionClassName=java.io.IOException ...
```

#### With a config file

- /path/to/chaos-dukey.properties

```console
delay.typeNamePattern=^org\.komamitsu\.transaction\.(?:LockManager|OCCManager)$
delay.methodNamePattern=^(?:lock.*|compareAndSwap)$
delay.waitMode=RANDOM
delay.percentage=1
delay.maxDelayMillis=1500
failure.typeNamePattern=^org\.komamitsu\.repository\..*Repository$
failure.methodNamePattern=^(?:get.*|save.*|delete.*)$
failure.ppm=1000
failure.exceptionClassName=java.io.IOException
```

```console
java -javaagent:/path/to/chaos-dukey-x.x.x-all.jar=configFile=/path/to/chaos-dukey.properties ...
```

## Note

This is only for testing purpose, and don't use it in production since it would affect the performance.

