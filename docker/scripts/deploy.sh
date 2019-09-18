#!/bin/bash
docker build -t musicbot:latest -f ./docker/Dockerfile --no-cache .
cmd="docker run -e BOT_TOKEN='$1' -v $2:/config musicbot:latest"
eval $cmd