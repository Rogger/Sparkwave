#!/bin/bash

echo "$@"
exec java -Xmx2048M -jar ../spark-weave/target/spark-weave-1.0-SNAPSHOT.one-jar.jar "$@"
