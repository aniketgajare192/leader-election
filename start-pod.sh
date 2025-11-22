#!/bin/bash

if [ -z "$1" ]; then
    echo "Usage: ./start-pod.sh <pod-number>"
    echo "Example: ./start-pod.sh 1"
    exit 1
fi

POD_NUM=$1
echo "Starting Application Pod $POD_NUM..."
mvn spring-boot:run -Dspring-boot.run.profiles=pod$POD_NUM

