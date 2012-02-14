#!/bin/bash
CP=.
SEP=':'

CP="$CP$SEP./lib/arq-2.8.4.jar"
CP="$CP$SEP./lib/jena-2.6.3.jar"
CP="$CP$SEP./lib/slf4j-api-1.5.8.jar"
CP="$CP$SEP./lib/slf4j-log4j12-1.5.8.jar"
CP="$CP$SEP./lib/spark-core-1.0-SNAPSHOT.jar"
CP="$CP$SEP./lib/spark-epsilon-1.0-SNAPSHOT.jar"
CP="$CP$SEP./lib/spark-language-1.0-SNAPSHOT.jar"
CP="$CP$SEP./lib/spark-rete-1.0-SNAPSHOT.jar"
CP="$CP$SEP./lib/spark-weave-1.0-SNAPSHOT.jar"
CP="$CP$SEP./lib/junit-3.8.1.jar"
CP="$CP$SEP./lib/log4j-1.2.16.jar"
CP="$CP$SEP./lib/xercesImpl-2.7.1.jar"
CP="$CP$SEP./lib/iri-0.8.jar"
CP="$CP$SEP./lib/icu4j-3.4.4.jar"
CP="$CP$SEP./lib/junixsocket-1.3.jar"

exec java -cp "$CP" -Xmx2048M at.sti2.spark.network.SparkWeaveNetwork "$@"
