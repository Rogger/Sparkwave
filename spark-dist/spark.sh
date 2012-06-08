#!/bin/bash

echo "$@"
exec java -Xmx1024M -jar ../spark-weave/target/spark-weave-1.0-SNAPSHOT.jar "$@"
