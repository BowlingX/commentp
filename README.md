commentp
--------

[![Circle CI](https://circleci.com/gh/BowlingX/commentp.svg?style=svg)](https://circleci.com/gh/BowlingX/commentp)

CommentP is an inline commenting platform

# Setup docker

The only supported method right now is developing with OSX and any docker supported linux Distro (like ubuntu).

## OSX

Docker does not work out of the box with OSX, that's why we use `boot2docker`.
Grab your copy @ http://boot2docker.io/ and follow installation @ https://docs.docker.com/installation/mac/.

Also install `docker-compose`: https://docs.docker.com/compose/install/

Instead of using VirtualBox with slow and buggy file-system support we use Parallels:

### Install Vagrant with Parallels under OSX
The best method to get Docker working with OSX is using the following boot2docker image with parallels:
https://github.com/Parallels/boot2docker-vagrant-box

- Make sure you have parallels installed
- run `vagrant plugin install vagrant-parallels`
    - It's possible that this fails and you have to install additional ruby libraries (should be visible in the given error)
- run `vagrant up --provider parallels`

Export all needed variables for the docker client (or use `$(./setup-env.sh)`)

### Build a new Base-Image

Commentp uses a custom base-image with bundled Java (7) and a prepared Environment.
The Dockerfile is included in this repository and will also be used for development in the future.

To build a new Version you need to have a local `docker` client running:

```
./build-docker.sh
```

## Deployment on DigitalOcean

- https://www.digitalocean.com/community/tutorials/how-to-set-up-a-coreos-cluster-on-digitalocean

## Using `fleetctl` from remote

```
export FLEETCTL_TUNNEL=$COREOS_CLOUD_IP
eval `ssh-agent`
ssh-add ~/.ssh/id_rsa
```


# NPM Tasks

- `npm test` will execute all unit tests and writes junit output and coverage-report
- `npm run tdd` will start tests in tdd mode
- `npm run dist` will build frontend bundle