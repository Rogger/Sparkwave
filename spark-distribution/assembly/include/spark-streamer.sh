#!/bin/bash

exec java -Xmx1024M -jar bin/spark-streamer.jar "$@"
