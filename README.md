# Chaos Dukey

This is a Java agent to inject chaos delays into specific Java classes and methods.

## Download the JAR file

Download the latest JAR file `chaos-dukey-<VERSION>-all.jar` from https://github.com/komamitsu/chaos-dukey/releases.

## Usage

Set `-javaagent` Java option to the downloaded JAR file with some parameters as follows.

| Parameter                   | Description                                                                                                                       | Default value                |
|-----------------------------|-----------------------------------------------------------------------------------------------------------------------------------|------------------------------|
| `delay.enabled`             | Specifies whether to inject delays, waiting for a fixed or random duration.                                                       | `false`                      |
| `delay.typeNamePattern`     | [For delay injection] Type name pattern to filter classes and interfaces. Supports Java's regex format.                           | All types are targeted       |
| `delay.methodNamePattern`   | [For delay injection] Method name pattern to filter methods. Supports Java's regex format.                                        | All methods are targeted     |
| `delay.waitMode`            | [For delay injection] `RANDOM`: waits for random duration between 1 ms and `maxDelayMillis`. `FIXED`: waits for `maxDelayMillis`. | `RANDOM`                     |
| `delay.ppm`                 | [For delay injection] Parts per million (ppm) chance that a delay injection occurs. Cannot be specified with `percentage`.        | 20000 ppm (2%)               |
| `delay.percentage`          | [For delay injection] Percentage chance that a delay injection occurs. Cannot be specified with `ppm`.                            | 2% (20000 ppm)               |
| `delay.maxDelayMillis`      | [For delay injection] Maximum delay in millis.                                                                                    | 500 ms                       |
| `failure.enabled`           | Specifies whether to inject failures, throwing an exception.                                                                      | `false`                      |
| `failure.typeNamePattern`   | [For failure injection] Type name pattern to filter classes and interfaces. Supports Java's regex format.                         | All types are effected       |
| `failure.methodNamePattern` | [For failure injection] Method name pattern to filter methods. Supports Java's regex format.                                      | All methods are effected     |
| `failure.exception`         | [For failure injection] Exception thrown from the target methods.                                                                 | `java.lang.RuntimeException` |
| `failure.ppm`               | [For failure injection] Parts per million (ppm) chance that a delay injection occurs. Cannot be specified with `percentage`.      | 20000 ppm (2%)               |
| `failure.percentage`        | [For failure injection] Percentage chance that a delay injection occurs. Cannot be specified with `ppm`.                          | 2% (20000 ppm)               |
| `debug`                     | Specifies whether to output debug information to STDERR.                                                                          | `false`                      |
| `configFile`                | The path to properties file that contains the parameters above. Other parameters will be ignored if this parameter is specified.  | ----                         |

Example:
```
$ java ... -javaagent:/path/to/chaos-dukey-x.x.x-all.jar=typeNamePattern=^org\.example\.transaction\.(?:Foo|Bar),methodNamePattern=.*Update,waitMode=FIXED,ppm=2000,maxDelayMillis=250
```

## Note

This is only for testing purpose, and don't use it in production since it would affect the performance.

