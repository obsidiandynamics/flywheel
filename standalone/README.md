Flywheel - Standalone
===
Supports standalone deployments of Flywheel edge nodes using either Docker images or 'fat' JARs.

# Docker
## Getting the image
Docker images are hosted on Dockerhub, in the [whcom/flywheel](https://hub.docker.com/r/whcom/flywheel) repository. The `latest` tag corresponds to the most recent stable build; the `snapshot` tag is the most recent snapshot.

To get the image, run `docker pull whcom/flywheel`.

## Running
To run the image as-is (i.e. with the default configuration embedded in the image), run

```
docker run -p 8080:8080 -it whcom/flywheel
```

This will start an interactive session, and will bind Flywheel's default HTTP port `8080` to the same on the host machine. You should see the following output:

```
 ____  _    _     _       _     ____  ____  _   
| |_  | |  \ \_/ \ \    /| |_| | |_  | |_  | |  
|_|   |_|__ |_|   \_\/\/ |_| | |_|__ |_|__ |_|__

> Starting java -Xms128M -Xmx4G        -cp build/libs/flywheel-*jar        au.com.williamhill.flywheel.Launchpad        
INFO  main Launchpad.<init>(): 
  Flywheel version: 0.1.1-SNAPSHOT_1034
  Indigo version: 0.9.4_1437
  Properties:
    log4j.configuration: file:conf/default/log4j-default.properties
  Launchers:
    au.com.williamhill.flywheel.ConfigLauncher@6a4f787b
INFO  main ConfigLauncher.launch(): 
  Args: []
  Backplane: au.com.williamhill.flywheel.edge.backplane.NoOpBackplane@6d4b1c02
  Server config:
    port: 8080
    path: /broker
    idle timeout: 300000 ms
    ping interval: 60000 ms
    scan interval: 1000 ms
    servlets:
      /health/* -> HealthServlet
    endpoint config:
      high-water mark: 9223372036854775807
  Plugins:
    Beacon {interval: 1000 ms, topic: time, format: yyyy-MM-dd'T'HH:mm:ssZ}
    TopicLogger {exclude topics: [time]}
```

The broker's WebSocket endpoint will now be served on `ws://localhost:8080/broker`. The built-in configuration also includes the Beacon module, which continuously broadcasts the current date and time on the `time` topic - useful for testing.

In addition, the built-in configuration will serve a health check on `/health`, which is often required when running Flywheel behind a load balancer or a gateway. See [Configuration](#user-content-configuration) for more details.

# JAR
`//TODO`

# Configuration
Standalone deployments are configured through a mechanism called a _profile_. A profile is encapsulated in a YAML file comprising -

* System properties that will be loaded prior to the bootstrapping process; and
* One or more `Launcher` instances that perform the actual bootstrapping, using a supplied configuration.

Below is a sample `profile.yaml`, based on the [built-in](https://raw.githubusercontent.com/William-Hill-Community/flywheel/master/standalone/conf/default/profile.yaml) default.

```yaml
properties:
  log4j.configuration: file:conf/default/log4j-default.properties
  flywheel.logging.splunk.url: ${mandatory(env.FLYWHEEL_SPLUNK_URL, "Splunk URL cannot be null")}
  flywheel.logging.splunk.token: ${secret(mandatory(env.FLYWHEEL_SPLUNK_TOKEN, "Splunk token cannot be null"))}
  flywheel.logging.splunk.index: ${mandatory(env.FLYWHEEL_SPLUNK_INDEX, "Splunk index cannot be null")}
  flywheel.logging.splunk.source: ${env.FLYWHEEL_SPLUNK_INDEX}

launchers: 
- type: au.com.williamhill.flywheel.ConfigLauncher
  backplane:
    type: au.com.williamhill.flywheel.edge.backplane.NoOpBackplane
  serverConfig:
    port: 8080
    path: /broker
    idleTimeoutMillis: 300000
    pingIntervalMillis: 60000
    scanIntervalMillis: 1000
    servlets:
    - path: /health/*
      name: health
      servletClass: au.com.williamhill.flywheel.health.HealthServlet  
    endpointConfig:
      highWaterMark: ${maxLong}
  plugins:
  - type: au.com.williamhill.flywheel.edge.plugin.beacon.Beacon
    topic: time
    intervalMillis: 1000
    format: "yyyy-MM-dd'T'HH:mm:ssZ"
  - type: au.com.williamhill.flywheel.edge.plugin.toplog.TopicLogger
    excludeTopics: ['time']
```

## Properties
The properties are loaded before all other elements of the configuration, appended directly to the system properties. The example above uses system properties to initialise the logging subsystem.

## Launcher
A launcher is used to start a single instance of an edge node. A `ConfigLauncher` is the built-in `Launcher` implementation that draws from the provided configuration to construct an `EdgeNode`. The configuration comprises the following elements:

* `backplane` - The interconnect used between the edge instances. The default is a `NoOpBackplane`, used in single-node deployments. When setting up a cluster of edge nodes, use a `KafkaBackplane` to distribute messages among all nodes in the cluster.
* `serverConfig` - Configures [Socket.x](https://github.com/obsidiandynamics/socketx) - the library used behind the scenes for asynchronous WebSocket I/O. This section states how the socket endpoint is to be published, and bundles additional HTTP servlets, such as a health check. Socket.x also allows you to set a high-water mark, which the number of WebSocket frames that may be buffered on any given connection before frame dropping will occur. This allows the broker to accommodate slow consumers, placing an upper limit on the number of buffered messages.
* `plugins` - A list of plugins to load. The list is in priority order, meaning that the item at the head of the list gets loaded first.

## Parametrised configuration
Flywheel uses the [EL](https://en.wikipedia.org/wiki/Unified_Expression_Language) standard to evaluate values in the YAML file. Thus, a profile need not be concrete; but rather a template that is bound to the actual values during application startup. This makes it convenient for working with build pipelines and a multi-environment set-up, where the configuration artefacts aren't coupled to the build process. Simply use the `${...}` notation to evaluate EL expressions in any YAML scalar value or array. (EL cannot be present in a key.)

The following variables and functions are available to use with EL:

|Name|Type/Signature                    |Description|
|----|----------------------------------|-----------|
|`env`|`Map<String, String>`|Environment variables.|
|`maxInt`|`int`|The result of `Integer.MAX_VALUE`.|
|`maxLong`|`long`|The result of `Long.MAX_VALUE`.|
|`secret`|`(String value) -> Secret`|Wraps the given value in an instance of `Secret`, such that calling `Secret.toString()` returns the constant `<masked>`, rather than the actual value. This prevents a configured value from being logged during application start-up.|
|`mandatory`|`(Object value, String errorMessage) -> Object`|Ensures that the given `value` cannot be null, throwing a `MissingValueException` otherwise with the given `errorMessage`. This forces a configuration to have a value bound to it, rejecting cases where a value is omitted.|



### Building the Docker image
```sh
$ docker build -t <user>/flywheel .
```

Where `user` is the dockerhub user.

### Running the image
```sh
$ docker run -it -p 8080:8080 <user>/flywheel [flywheel args]
```

Where `flywheel args` are optional args to the Flywheel launcher.

#### Running the image in command mode
```sh
$ docker run -it -p 8080:8080 <user>/flywheel --cmd [sh args]
```

If the optional `sh args` are omitted, the image will start with an interactive shell.

To run Flywheel within the shell:

```sh
$ flywheel-launch.sh [flywheel args]
```