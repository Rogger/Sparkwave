#!/bin/bash

for i in {1..5}
do
	echo "--------------------------> Test no. $i started at $(date) <--------------------------"
	echo "--> Starting Sparkweave engine with pattern-PT2-TW100.tpg, NULL, NULL and 5ms GC."
	
	exec ./spark.sh tests/bsbm/pattern-PT2-TW100.tpg NULL NULL 5 &
	
	sparkweave=$!
	echo "--> Sparkweave engine started as background process $sparkweave."
	
	echo "--> Waiting 5 sec to build Sparkweave network..."
	sleep 5
	
	echo "--> Streaming offers-bydate-fc.nt at port 8080..."
	
	exec ./spark-streamer.sh 8080 tests/bsbm/dataset-10000/offers-bydate-fc.nt &
	
	streamer=$!
	echo "--> Offers streamed as background process $streamer."
	
	wait $streamer
	
	echo "--> Waiting 5 seconds for Sparkweave engine to process data stream..."
	sleep 5
	
	echo "--> Terminating Sparkweave engine process $sparkweave."
	kill $sparkweave
	
	echo "--------------------------> Test no. $i finished at $(date) <--------------------------"
done


