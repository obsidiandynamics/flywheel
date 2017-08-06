Flywheel - Standalone
===
Supports standalone deployments of Flywheel edge nodes using either Docker images or 'fat' JARs.

# Docker
## Getting the image
Docker images are hosted on Dockerhub, in the [whcom/flywheel](https://hub.docker.com/r/whcom/flywheel) repository. The `latest` tag corresponds to the most recent stable build; the `snapshot` tag is the most recent snapshot.

To get the image, run `docker pull whcom/flywheel`.

## Running
To run the image from Dockerhub as-is (i.e. with the default configuration packaged in the image), run

```sh
docker run -p 8080:8080 -it whcom/flywheel
```

Note: if you're running a custom image build, omit the `whcom/` from the command. This will source the image from your local repo.

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
Running off a 'fat' JAR is essentially the same as using Docker. (In fact, that's exactly what the container does when it starts.) But first, you need to get the source code and build it.
```sh
git clone https://github.com/william-hill-community/flywheel
cd flywheel
./gradlew build
```

The above assumes you are running on *NIX or macOS, and have JDK 8 installed.

To start Flywheel with the default configuration:
```sh
cd standalone
scripts/flywheel-launch.sh
```

# Configuration
Standalone deployments are configured through a mechanism called a _profile_. A profile is encapsulated in a YAML file comprising -

* System properties that will be loaded during the bootstrapping process; and
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
The properties are loaded before all other elements of the configuration, appended directly to the system properties. The example above uses system properties to initialise the logging subsystem. You can pass in _most_ system properties this way, as you would otherwise do with a `-D` JVM flag. However, some system properties, such as `com.sun.management...` can _only_ be passed in via `-D` during JVM initialisation.

## Launcher
A launcher is used to start a single instance of an edge node. A `ConfigLauncher` is the built-in `Launcher` implementation that draws from the provided configuration to construct an `EdgeNode`. The configuration comprises the following elements:

* `backplane` - The interconnect used between the edge instances. The default is a `NoOpBackplane`, used in single-node deployments. When setting up a cluster of edge nodes, use a `KafkaBackplane` to distribute messages among all nodes in the cluster.
* `serverConfig` - Configures [Socket.x](https://github.com/obsidiandynamics/socketx) - the library used behind the scenes for asynchronous WebSocket I/O. This section states how the socket endpoint is to be published, and bundles additional HTTP servlets, such as a health check. Socket.x also allows you to set a high-water mark, which the number of WebSocket frames that may be buffered on any given connection before frame dropping will occur. This allows the broker to accommodate slow consumers, placing an upper limit on the number of buffered messages.
* `plugins` - A list of plugins to load. The list is in priority order, meaning that the item at the head of the list gets loaded first.

## Parametrised configuration
Behind the scenes Flywheel uses the [YConf library](https://github.com/obsidiandynamics/yconf) for configuration and bootstrapping. YConf supports the [EL](https://en.wikipedia.org/wiki/Unified_Expression_Language) standard to evaluate values in the YAML file. Thus, a profile need not be concrete; but rather a template that is bound to the actual values during application startup. This makes it convenient for working with build pipelines and a multi-environment set-up, where the configuration artefacts aren't coupled to the build process. Simply use the `${...}` notation to evaluate EL expressions in any YAML scalar value or array. (EL cannot be present in a key.)

The following variables and functions are available to use with EL:

|Name|Type/Signature                    |Description|
|----|----------------------------------|-----------|
|`env`|`Map<String, String>`|Environment variables.|
|`maxInt`|`int`|The result of `Integer.MAX_VALUE`.|
|`maxLong`|`long`|The result of `Long.MAX_VALUE`.|
|`secret`|`(String value) -> Secret`|Wraps the given value in an instance of `Secret`, such that calling `Secret.toString()` returns the constant `<masked>`, rather than the actual value. This prevents a configured value from being logged during application start-up.|
|`mandatory`|`(Object value, String errorMessage) -> Object`|Ensures that the given `value` cannot be null, throwing a `MissingValueException` otherwise with the given `errorMessage`. This forces a configuration to have a value bound to it, rejecting cases where a value is omitted.|

## Specifying a profile
A profile is specified in one of two ways: the `FLYWHEEL_PROFILE` environment variable or the `flywheel.launchpad.profile` system property. The value of the variable/property must be the path to the profile _directory_, whereas the profile configuration _must_ be a file named `profile.yaml` in that directory.

For example, we would create a custom profile in `conf/custom` named `profile.yaml`. Prior to launching Flywheel, run `export FLYWHEEL_PROFILE=conf/custom`.

By default (if neither the environment variable nor the system property is supplied), Flywheel assumes a profile in `conf/default` (relative to Flywheel's working directory).

### Configuring Docker
When launching Flywheel via Docker, you need a couple of extra steps to configure the profile.

First, Docker needs to be told to mount an external volume - a directory containing your configuration. The container expects the volume mounted in `/ext`.  Then, you need to pass in an environment variable to Docker. The example below launches Flywheel in interactive mode using a configuration in `~/conf/custom`.
```sh
docker run -it -p 8080:8080 -v ~/conf:/ext -e FLYWHEEL_PROFILE=ext/custom flywheel
```

## The `bootrc` file
The `bootrc` file is used to initialise environment variables with default values prior to launching the JVM. Flywheel looks for `bootrc` in both `./conf` and in `./ext`, executing `./conf/bootrc` first, if present, followed by `./ext/bootrc`, if present. (The `./` here signifies a path relative to the working directory.) The `bootrc` file normally houses low-level JVM options, such as the heap parameters, GC configuration, JMX flags, and so on.

Under normal circumstances, there is no reason to change `bootrc` directly. Instead, values should be overridden by assigning the appropriate environment variables. (And passing the `-e` option when using Docker.) Where it does make sense to change `bootrc` is when you want to bake your own distribution with different defaults. For example, you might want to target low-powered ARM platforms like the Raspberry Pi, which would be best addressed with more conservative default `-Xms` and `-Xmx` values.

# Performance tuning
You can override the default JVM options by setting the `FLYWHEEL_JVM_OPTS` environment variable. The example below caps the heap to between one and two gigabytes and sets an upper bound on the amount of direct (non-heap) memory. It also lifts any restrictions on the number of file descriptors dispensed to the JVM. 

```sh
export FLYWHEEL_JVM_OPTS="-Xms1G -Xmx2G -XX:MaxDirectMemorySize=2G -XX:-MaxFDLimit"
```

As with any JVM performance tuning scenario, there is a myriad of options available. One should apply changes judiciously, measuring the effect of each change in isolation, as well as the performance of the overall configuration. The parameters in example above should be your starting point, as they have the most profound impact on the system's behaviour and are also the easiest to understand. 

Flywheel makes heavy use of direct buffers and async I/O, meaning that the bulk of the memory pressure is concentrated outside the heap. Failing to limit this space might initially show improved performance (by maximising the use of buffer pools) for short benchmark runs; however, as the resident size grows it could eventually lead to excessive swapping. The recommended approach is to keep the sum of the maximum heap size and the direct memory size to below the maximum desired resident size, and allowing for 1-2 GB of slack.

Example: suppose a machine with 8 GB of RAM is used as a dedicated Edge node. Let's say the OS and other processes have a steady-state size of around 2 GB. This leaves around 6 GB for Flywheel. We would set the maximum heap size and the direct memory size to about 2 GB each, allowing for another 2 GB of slack. Some of this will be taken up by sundry non-heap areas, such as the PermGen, code cache, stack memory, and so on. The bulk of it though, will still be direct buffers that are pending collection, as it takes some time for direct memory to be fully reclaimed. We would then run long soak tests with the connection profile expected in production, monitoring Flywheel's memory usage. The objective is to give Flywheel the most amount of RAM for its direct buffers without hitting swap. If you're tracking well, increase the _direct memory_ size. Otherwise, consider decreasing the _heap_ size. Because Flywheel is so reliant upon async I/O, there's little point in giving it more than 2 GB of heap. We've tested very large configurations (up to 50K connections per node) comfortably with `-Xms1G -Xms2G`.

# Logging
The standalone image uses Log4j 1.2. By default, the log is appended both to the console and to a file in `./log` using a rolling file appender, for a maximum file size of 100 MB. Logging is configured through the profile, by specifying a path to a Log4j configuration file, as shown in the snippet below.
```yaml
properties:
  log4j.configuration: file:conf/default/log4j-default.properties
```

## Logging in Docker
When running in Docker with the default profile, logs will be written to the `/log` directory within the container. To get these out to the host file system, run docker with the volume option, e.g. `-v ~/logs:/logs`.

# Building the Docker Image
The build process is completely automated through Gradle. To build the image from source (assuming you have the source checked out and JDK 8 installed):
```sh
./gradlew dockerBuild
```

The resulting build will be tagged as `flywheel:latest`. See [Running](#user-content-running) for instructions on running the image.