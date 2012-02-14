What is spark-streamer?
====================================================

Purpose of Sparkweave Streamer project is to enable reading and communication of RDF triples in a separate Java VM.
RDF triples are communicated either as serialized Java objects or represented as String instances. Each triple is represented as an instance of
StreamedTriple class defined in spark-core project. Upon arrival, Sparkweave network associates timestamps to streamed triples and feeds them into the epsilon network.

How to run a spark-streamer instance? 
====================================================
In order to run spark-streamer execute following script:

$./spark-streamer <socket_name> <triple_file>

<socket_name> - spark-streamer takes as an argument a UNIX domain socket name.
<triple_file> - spark-streamer reads triples from a file given in a command line. The expected format of the file is N-TRIPLES.


