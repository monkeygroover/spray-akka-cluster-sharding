# cluster-sharding

Just an example to illustrate cluster sharding, also took the opportunity to try out akka-http to see how it differed from spray (not a lot fortunately! :) ) and upgrade to the latest akka versions.

install mongodb 3.0+  (can be switched to cassandra simply by changing the config, instructions here: https://github.com/krasserm/akka-persistence-cassandra/ )

sbt assembly will create 3 jars; seed,backend and rest

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
