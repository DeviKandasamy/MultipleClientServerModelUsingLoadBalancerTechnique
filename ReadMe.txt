******************************************************************************
*                       COEN 233-01: Computer Networks
*
*                         	 Project
*
*               Development of an efficient multiple Client-Server Model 
*	 for an Medical Application using Load Balancer Technique 
				
				 README
*
*******************************************************************************
*
*                                  Objective
*
* The /home/<login>/AutoTestDir/<login>/P3/ directory contains this README file,
* the location of this directory is on SCU Linux server.This README describes how
* to build and run java code for client/server on linux and windows.
*
*
* For linux:
* The below files are under the /home/<login>/AutoTestDir/<login_name>/P3 directory
*       1) LoadBalancer.java
*       2) Server.java
*       3) Server1.java
*       4) Client.java
*       5) Client1.java
*	6) Client2.java
*       7) Client3.java
*       8) README
*
*
* On execution the following files are created:
* client*
* server*
* loadBalancer*
* and all class files*
*
**********************************************************************************

            Prepare your environment to compile and execute


#Steps to create Database

1) Goto http://www.mysql.com/downloads/

2) For details on how to install MySQL, please refer to the following link:

	https://www.youtube.com/watch?v=iP1wOSsKjW8

3) Download the JDBK connector and include the jar file in your project.

	http://www.mysql.com/products/connector/


#Steps to execute the code

1) Run the LoadBalancer process.

2) Start up the Database.

2) Run the Server processes.

3) Run the Client processes.


#What to expect

1) Initially when all the processes are running:
	a) The servers status is reported on the Load Balancer every 5 seconds.
	b) The server status port and listener port is included in connection table of laod balancer.
	c) The clients request will be processed i.e, when the patients ID and First Name is passed, the server gives the patients details from the database.

2) If one of the servers go down:
	a) The Load Balancer reports if the server is down.
	b) All of the incomming connections from clients are sent to the next available server. If server is not available, client request is held till a a server is available.
	


#Input

1) Input text files are read from a path specified in the clients program.
2) the path can be changed to the location of the files.
3) The text files contain the Clients id and First Name. For example:
	3 Bharghavi

#Software used
Eclipse, mysql workBench

#Language used
Java
