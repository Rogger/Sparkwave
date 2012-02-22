@echo off
rem spark.bat starts a new instance of Sparkweave network

set classpath=.

set classpath=%classpath%;%UserProfile%\.m2\repository\com\hp\hpl\jena\arq\2.8.4\arq-2.8.4.jar
set classpath=%classpath%;%UserProfile%\.m2\repository\com\hp\hpl\jena\jena\2.6.3\jena-2.6.3.jar
set classpath=%classpath%;%UserProfile%\.m2\repository\org\slf4j\slf4j-api\1.5.8\slf4j-api-1.5.8.jar
set classpath=%classpath%;%UserProfile%\.m2\repository\org\slf4j\slf4j-log4j12\1.5.8\slf4j-log4j12-1.5.8.jar
set classpath=%classpath%;%UserProfile%\.m2\repository\spark\spark-core\1.0-SNAPSHOT\spark-core-1.0-SNAPSHOT.jar
set classpath=%classpath%;%UserProfile%\.m2\repository\spark\spark-epsilon\1.0-SNAPSHOT\spark-epsilon-1.0-SNAPSHOT.jar
set classpath=%classpath%;%UserProfile%\.m2\repository\spark\spark-language\1.0-SNAPSHOT\spark-language-1.0-SNAPSHOT.jar
set classpath=%classpath%;%UserProfile%\.m2\repository\spark\spark-rete\1.0-SNAPSHOT\spark-rete-1.0-SNAPSHOT.jar
set classpath=%classpath%;%UserProfile%\.m2\repository\spark\spark-weave\1.0-SNAPSHOT\spark-weave-1.0-SNAPSHOT.jar
set classpath=%classpath%;%UserProfile%\.m2\repository\junit\junit\3.8.1\junit-3.8.1.jar
set classpath=%classpath%;%UserProfile%\.m2\repository\log4j\log4j\1.2.16\log4j-1.2.16.jar
set classpath=%classpath%;%UserProfile%\.m2\repository\xerces\xercesImpl\2.7.1\xercesImpl-2.7.1.jar
set classpath=%classpath%;%UserProfile%\.m2\repository\com\hp\hpl\jena\iri\0.8\iri-0.8.jar
set classpath=%classpath%;%UserProfile%\.m2\repository\com\ibm\icu\icu4j\3.4.4\icu4j-3.4.4.jar

java -cp %classpath% -Xmx2048M at.sti2.spark.network.SparkWeaveNetwork %1 %2 %3 %4
