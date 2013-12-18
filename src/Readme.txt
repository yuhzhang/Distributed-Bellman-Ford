Chih-Kai Chang
cc3527
pa3
java

simply type make to compile

How to use:
	java Client <port number> <timeout (s)> [ip_address port_number cost ...]

after starting a client, type HELP for a list of commands:

	NEIGHBOR : lists the neighbors and the links
	SHOWRT	 : lists the routing table
	LINKDOWN <ipaddress> <port> : destroys a link by sending a LINKDOWN message to the
								  neighbor as well as removing from neighborList and
								  set routing table of any destination through that
								  neighbor to infinity
	LINKUP <ip address> <port> :  retores the link and sends  a LINKUP message to the
								  neighbor
	CLOSE	: closes the client similar to Ctrl + C

Data Structure
The data structure behind the routing table is 3 ArrayList of Destination,
Estimated Cost, and Link. Each client also holds a list of neighbors and a list
of time that the neighbor has send an UPDATE. 

Threads
TimerThread.java	keeps track of when TIMEOUT seconds is up to send UPDATE to neighbors.
					It also tracks TIMEOUT*3 seconds to drop an unresponsive neighbor as a neighbor.
SendThread.java		sends to the neighbors its Routing Table
ListenThread.java	listens to incoming UPDATES, LINKDOWNS, and LINKUPs. It also calculates the least
					cost path to the destinations. This might be weird, but I do not keep track of all
					Routing tables, instead, I look at incoming Routing Table from a neighbor and compare
					from there.

This program works when tested with 3 and 4 nodes with LINKDOWN and CLOSE. Bouncing is observed. But the
optimal path is eventually found.
