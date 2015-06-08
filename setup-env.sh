#!/bin/bash

echo export DOCKER_HOST_IP=$(vagrant ssh-config | sed -n "s/[ ]*HostName[ ]*//gp")
echo export DOCKER_HOST="tcp://${DOCKER_HOST_IP}:2375"