import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;


public class SendThread extends Thread{
	private Socket socket = null;
	private int maxThreadCount;
	private BufferedReader in = null;
	private PrintWriter out = null;
	
	public ArrayList<String> ipPort;
	public ArrayList<Double> weight;
	public ArrayList<String> link;
	public int myPort;
	
	//constructor with pointers
	public SendThread(int port,
					ArrayList<String> ipPort,
						ArrayList<Double> weight,
							ArrayList<String> link){
		this.ipPort = ipPort;
		this.weight = weight;
		this.link = link;
		myPort = port;
	}
	
	public void run(){
		DatagramSocket dsock = null;

		try {
			dsock = new DatagramSocket(myPort);
		} catch (SocketException e) {
			e.printStackTrace();
		}
		while(true){
			//make a local copy of Routing Table
			ArrayList<String> mIpPort = new ArrayList<String>();
			ArrayList<Double> mWeight = new ArrayList<Double>();
			ArrayList<String> mLink = new ArrayList<String>();
			
			for(int i=0; i<weight.size(); i++){
				mIpPort.add(ipPort.get(i));
				mWeight.add(weight.get(i));
				mLink.add(link.get(i));
			}
			
			for(int i=0; i<mWeight.size(); i++){
				for(int j=0; j<mWeight.size(); j++){
					if(mWeight.get(i) < Double.MAX_VALUE && i!=j){
						//construct packet
						String msg = mIpPort.get(j) + ":" + mWeight.get(j);
						byte arr[] = msg.getBytes();
						InetAddress add = Client.getAddress(mIpPort.get(i));
						int port = Client.getPort(mIpPort.get(i));
						DatagramPacket dpack = new DatagramPacket(arr, arr.length, add, port);
						
						//sending packet
						try {
							dsock.send(dpack);
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
			}
			
			
			
		}
	}
}