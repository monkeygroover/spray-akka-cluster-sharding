# cluster-sharding

install mongodb 3.0+

run two (or more) backend nodes on ports 8000/8001/... (use option -Dakka.remote.netty.tcp.port=8000 etc)

run frontend, it should send in 1000 AddRecord commands sharded over 100 customers

or start the spray server


add with POST to /customer/{customer_id}

post body of form:
{
    "name" : "name",
    "data1" : "blah blah",
    "data2" : "whatever"
}

get the list with GET to /customer/{customer_id}

delete with POST to /customer/{customer_id}/{record_uuid}

update with PUT to /customer/{customer_id}/{record_uuid}
with a body of form :
{
    "name": "name_updated"
}

omit a field in the update to leave it unchanged

customer shard actor state will be passivated after 5 minutes of inactivity.

if the shard dies then once the node crash is detected and removed from the cluster the data will be auto recovered to another node
