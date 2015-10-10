# cluster-sharding

install mongodb 3.0+

run two (or more) backend nodes on ports 8000/8001/... (use option -Dakka.remote.netty.tcp.port=8000 etc)

run frontend, it should send in 1000 AddRecord commands sharded over 100 customers

or start the spray server and use post and get to /consumer/{id}

post body of form:
{
    "name" : "Joe Bloggs",
    "data1" : "blah blah",
    "data2" : "optional"
}

