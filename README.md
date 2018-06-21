SYSC 3303
iter0

Setup:  
	Step1: Run TFTPServer  
	Step2: Run TFTPClient
	Step3: Run TFTPErrorSimulator (if you want to do the test)

To use TFTP: 
	TFTPClient:  
		- read/get    ---> send a read request, type "read" or "get" then press enter
		- write/send  ---> send a write request, type "write" or "send" then press enter 
		- mode   	  ---> get the current mode, type "mode" then press enter  
		- switch      ---> change the current mode, type "switch" then press enter  
		- help  	  ---> print a help menu, type "help" then press enter  
		- verbose     ---> change the display complexity, type "verbose" then press enter  
		- quit/exit   ---> terminate the client, type "quit" or "exit" then press enter
	TFTPServer:  
		- verbose     ---> change the display complexity, type "verbose" then press enter  
		- quit/exit   ---> terminate the server, type "quit" or "exit" then press enter
	TFTPErrorSimulator:  
		(no actual operation, it's just passing requests and data for now)
		
Testing step:  
	1. setup environment
	2. If you want to test normal mode  
		2.1 Type 'switch' to change the mode into normal mode
		2.2 in client, type read to test read request
		2.3 in client, type write to test write request
		2.4 Type 'verbose' to change the print mode  
		2.5 Type 'mode' to get current mode  
	3. If you want to test test mode  
		3.1 Type 'switch' to change the mode into test mode
		3.2 same as 2.2 or 2.3
	4. to exit, type "exit" or "quit"
	
About files:  
	- TFTPClient.java - TFTP client class, can send RRQ or WRQ to TFTPServer  
	- TFTPServer.java - TFTP server class, it will initialize TFTPRequestListener as a thread and listen to new request  
	- TFTPRequestListener.java - TFTP request listener class, a sub-class of thread and listen to WRQ or RRQ on port 69  
	- TFTPRequestHandler.java - TFTP request handler class, a sub-class of thread and it will handle the WRQ or RRQ received from TFTPRequestListener 
	- TFTPErrorSimulator.java - Used for simulating error, for now it will just receive a packet and forward the packet without touching the packet  