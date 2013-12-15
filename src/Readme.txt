Chih-Kai Chang
cc3527
pa2
java

simply type make to compile

How to use:
	java Reciever <filename> <listening_port> <remote_ip> <remote_port> <log_filename>
	java Sender <filename> <remote_ip> <remote_port> <ack_port> <window_size> <log_filename>

test file:
	alternatively, 3 .sh files are included to test with proxy

	proxy.sh
	sender.sh
	receiver.sh

	simply type sh <file>.sh to run it

Reciever uses Go Back N protocal to handle corrupt/out-of-order/dropped/duplicate packets.
	All packets received will be logged regardless of out-of-order/duplicate packets.
		Corrupt packets are dropped and not written to the log file.

Sender starts sending as soon as it is started; Uses a separate thread to listen for ack;
	Checksum is the sum over the data.
	Timeout is set to 50ms. To change this, find the variable timeout.
		It is updated everytime an ACK is received using Told = 0.25 * Tnew + 0.75 * RTT.
	Flags: ACK significance and FIN significance
	Sequence number starts at 0;

//extra
window size greater than 1 is supported
