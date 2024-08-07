# Chaos Dukey

This is a Java agent to inject chaos delays into specific Java classes and methods.

## Download the JAR file

Download the latest JAR file `chaos-dukey-<VERSION>-all.jar` from https://github.com/komamitsu/chaos-dukey/releases.

## Usage

Set `-javaagent` Java option to the downloaded JAR file with some parameters as follows.

| Parameter           | Description                                                                                                 | Default value            |
|---------------------|-------------------------------------------------------------------------------------------------------------|--------------------------|
| `typeNamePattern`   | Type name pattern to filter classes and interfaces. Java's regex format is supported.                       | All types are effected   |
| `methodNamePattern` | Method name pattern to filter methods. Java's regex format is supported.                                    | All methods are effected |
| `waitMode`          | `RANDOM`: waits for random duration between 1 ms and `maxDelayMillis`. `FIXED`: waits for `maxDelayMillis`. | `RANDOM`                 |
| `ppm`               | Parts per million (ppm) chance that a delay injection occurs. This can't be specified with `percentage`.    | 1000 ppm (0.1 %)         |
| `percentage`        | Percentage chance that a delay injection occurs. This can't be specified with `ppm`.                        | 0.1 % (1000 ppm)         |
| `maxDelayMillis`    | Maximum delay in millis. An actual delay is randomly decided up to the maximum.                             | 100 ms                   |
| `debug`             | Whether to output debug information.                                                                        | `false`                  |

Example:
```
$ java ... -javaagent:/path/to/chaos-dukey-x.x.x-all.jar=typeNamePattern=^org\.example\.transaction\.(?:Foo|Bar),methodNamePattern=.*Update,waitMode=FIXED,ppm=2000,maxDelayMillis=250
```

## Note

This is only for testing purpose, and don't use it in production since it would affect the performance.

