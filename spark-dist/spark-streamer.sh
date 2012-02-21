#!/bin/bash

exec java -Xmx1024M -jar ../spark-streamer/target/spark-streamer-1.0-SNAPSHOT-jar-with-dependencies.jar "$@"
