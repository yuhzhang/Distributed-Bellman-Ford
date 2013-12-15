import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;


public class ListenThread extends Thread{
	
	public ArrayList<String> ipPort;
	public ArrayList<Double> weight;
	public ArrayList<String> link;
	public int myPort;
	
	//constructor with pointers
	public ListenThread(int port,
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
		int maxSize = 8000;
		DatagramPacket dpack = new DatagramPacket(new byte[maxSize], maxSize);
		
		try {
			dsock = new DatagramSocket(myPort);
		} catch (SocketException e) {
			e.printStackTrace();
		}
		
		while(true){
			try {
				dsock.receive(dpack);
			} catch (IOException e) {
				e.printStackTrace();
			}
			String linkAdd = dpack.getAddress().toString();
			int linkPort = dpack.getPort();
			String linkAddPort = linkAdd + ":" + linkPort;
			
			String msg =  new String(dpack.getData(), 0, dpack.getLength());
			String[] tokens = msg.split(":");
			ArrayList<String> cIpPort = new ArrayList<String>();
			ArrayList<Double> cWeight = new ArrayList<Double>();
			
			//tries to catch bad data
			if(tokens.length%3 == 0){
				//reconstruct routing table of neighbor
				for(int i=0; i<tokens.length/3; i++){
					cIpPort.add(tokens[i*3] + ":" + tokens[i*3+1]);
					cWeight.add(Double.parseDouble(tokens[i*3+2]));
				}
				
				//update current Routing Table
				for(int i=0; i<cWeight.size(); i++){
					//update Routing Table
					if(ipPort.contains(cIpPort.get(i))){
						for(int j=0; j<weight.size(); j++){
							if(ipPort.get(j).equals(cIpPort.get(i))
									&& weight.get(j) > cWeight.get(i)){
								weight.set(j, cWeight.get(i));
								link.set(j, linkAddPort);
							}
						}
					}else{
						ipPort.add(cIpPort.get(i));
						weight.add(cWeight.get(i));
						link.add(linkAddPort);
					}
				}
			}
		}
	}
}
