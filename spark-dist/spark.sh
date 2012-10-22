#!/bin/bash

echo "$@"
exec java -Xmx1024M -jar ../spark-wave/target/spark-wave-1.0-SNAPSHOT.jar "$@"
