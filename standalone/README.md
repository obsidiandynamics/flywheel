

# Building the Docker image
```sh
$ docker build -t <user>/flywheel .
```

Where `user` is the dockerhub user.

# Running the image
```sh
$ docker run -it -p 8080:8080 <user>/flywheel [flywheel args]
```

Where `flywheel args` are optional args to the Flywheel bootstrapper.

## Running the image in command mode
```sh
$ docker run -it -p 8080:8080 <user>/flywheel --cmd [sh args]
```

If the optional `sh args` are omitted, the image will start with an interactive shell.

To run Flywheel within the shell:

```sh
$ docker-flywheel.sh [flywheel args]
```