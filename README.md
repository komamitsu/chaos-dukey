# Chaos Dukey

This is a Java agent to inject chaos delays into specific Java classes and methods.

## Download the JAR file

Download the latest JAR file `chaos-dukey-<VERSION>-all.jar` from https://github.com/komamitsu/chaos-dukey/releases.

## Usage

Set `-javaagent` Java option to the downloaded JAR file with some parameters as follows.

| Parameter | Description | Default value |
| ---- | ---- | ---- |
| `typeNamePattern` | Type name pattern to filter effected classes and interfaces. Java's regex format is supported. | All types are effected |
| `methodNamePattern` | Method name pattern to filter effected methods. Java's regex format is supported. | All methods are effected |
| `percentage` | Percentage that a delay injection occurs. | 5 % |
| `maxDelayMillis` | Maximum delay in millis. An actual delay is randomly decided up to the maximum. | 100 ms |

Example:
```
$ java ... -javaagent:/path/to/chaos-dukey-x.x.x-all.jar=typeNamePattern=^org\.example\.transaction\.(?:Foo|Bar),methodNamePattern=.*Update,percentage=15,maxDelayMillis=250
```

## Note

This is only for testing purpose, and don't use it in production since it would affect the performance.

