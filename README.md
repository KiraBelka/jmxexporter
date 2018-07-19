# jmxexporter

jmxexporter simple class within:
- port option 
- rmi url connection string 

# build:
javac /com/company/jmxexporter.java

# execution:

java -cp $PATH_TO_CLASS jmxexporter --port=$PORT --rmiurl=$RMIURL
example: port [0-65000]
         rmiurl [hostIP:HostRMIPort]
# troubles:
Utility reconnect to jmx each time when client follow /metrics uri.
There are some issues with exceptions

