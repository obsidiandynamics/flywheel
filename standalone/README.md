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
    Beacon [interval: 1000 ms, topic: time, format: yyyy-MM-dd'T'HH:mm:ssZ]
  Log-excluded topic filters:
    time
```

The broker's WebSocket endpoint will now be served on `ws://localhost:8080/broker`. The built-in configuration also includes the Beacon module, which continuously broadcasts the current date and time on the `time` topic - useful for testing.

In addition, the built-in configuration will serve a health check on `/health`, which is often required when running Flywheel behind a load balancer or a gateway. See [Configuration](#user-content-configuration) for changing the configuration.

# JAR
`//TODO`

# Configuration
Standalone deployments are configured through a _profile_.





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