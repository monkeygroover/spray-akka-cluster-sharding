# cluster-sharding

install mongodb 3.0+

sby publish-local on the shared project
sbt assembly on each of seed,backend and rest

bootstrap two seed nodes on ports 8000/8001 (use option -Dakka.remote.netty.tcp.port=8000 etc)

start as many other backend nodes as you like (on unique ports)

start the spray server (uses 8080)

add with POST to /customer/{customer_id}

post body of form:
{
    "name" : "name",
    "data1" : "blah blah",
    "data2" : "whatever"
}

get the list with GET to /customer/{customer_id}

delete with POST to /customer/{customer_id}/{record_uuid}

update with PATCH to /customer/{customer_id}/{record_uuid}
with a body of form :
{
    "name": "name_updated"
}

omit a field in the update to leave it unchanged

customer shard actor state will be passivated after 5 minutes of inactivity.

if the shard dies then once the node crash is detected and removed from the cluster the data will be auto recovered to another node


Event sourced query side:

GET to /customer/{customer_id}/history

will retrieve all historical events for the specified customer


---------------------------------------------------------------------
java -DPORT=8000 -DHOST=127.0.0.1 -DLOG_LEVEL=DEBUG -jar ./target/scala-2.11/seed-assembly-0.1.0-SNAPSHOT.jar
java -DPORT=9000 -DHOST=127.0.0.1 -jar ./target/scala-2.11/backend-assembly-0.1.0-SNAPSHOT.jar 
java -DPORT=9001 -DHOST=127.0.0.1 -jar ./target/scala-2.11/rest-assembly-0.1.0-SNAPSHOT.jar 

---------------------------------------------------------------------
DOCKER

run (sudo) sbt docker to create docker images

to run in bluemix containers

install cf cli, and ICE

ice login -a 'https://api.ng.bluemix.net' --registry 'registry.ng.bluemix.net' --host 'https://containers-api.ng.bluemix.net/v3/containers'







