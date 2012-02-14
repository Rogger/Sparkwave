#!/bin/bash
CP=.
SEP=':'

CP="$CP$SEP./lib/junixsocket-1.3.jar"
CP="$CP$SEP/home/skomazec/.m2/repository/spark/spark-streamer/1.0-SNAPSHOT/spark-streamer-1.0-SNAPSHOT.jar"
CP="$CP$SEP/home/skomazec/.m2/repository/spark/spark-core/1.0-SNAPSHOT/spark-core-1.0-SNAPSHOT.jar"

exec java -cp "$CP" -Xmx1024M at.sti2.spark.streamer.SparkStreamer "$@"