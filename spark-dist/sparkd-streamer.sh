#!/bin/bash
CP=.
SEP=':'

CP="$CP$SEP./lib/junixsocket-1.3.jar"
CP="$CP$SEP./lib/spark-streamer-1.0-SNAPSHOT.jar"
CP="$CP$SEP./lib/spark-core-1.0-SNAPSHOT.jar"

exec java -cp "$CP" -Xmx1024M at.sti2.spark.streamer.SparkStreamer "$@"
