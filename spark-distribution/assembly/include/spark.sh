#!/bin/bash

echo "$@"
exec java -Xmx1024M -jar bin/spark-wave.jar "$@"
