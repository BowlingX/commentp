#!/bin/bash

echo export DOCKER_TLS_VERIFY="1"
echo export DOCKER_CERT_PATH="`pwd`/tls"
echo export DOCKER_HOST="tcp://`vagrant ssh-config | sed -n "s/[ ]*HostName[ ]*//gp"`:2376"
