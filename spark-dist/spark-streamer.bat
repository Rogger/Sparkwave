@echo off
rem spark-streamer.bat starts a new instance of spark streamer network

set classpath=.

set classpath=%classpath%;%UserProfile%\.m2\repository\spark\spark-core\1.0-SNAPSHOT\spark-core-1.0-SNAPSHOT.jar
set classpath=%classpath%;%UserProfile%\.m2\repository\spark\spark-streamer\1.0-SNAPSHOT\spark-streamer-1.0-SNAPSHOT.jar

java -cp %classpath% -Xmx1024M at.sti2.spark.streamer.SparkStreamer %1 %2