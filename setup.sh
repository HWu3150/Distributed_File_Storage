#!/bin/bash

echo "Installing jpaxos.jar to local Maven repository..."
mvn install:install-file \
    -Dfile=libs/jpaxos.jar \
    -DgroupId=github_jpaxos \
    -DartifactId=jpaxos \
    -Dversion=1.0 \
    -Dpackaging=jar

echo "Setup completed!"
