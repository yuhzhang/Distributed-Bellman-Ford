import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Date;


public class SendThread extends Thread{
	
	public ArrayList<String> ipPort;
	public ArrayList<Double> weight;
	public ArrayList<String> link;
	
	public ArrayList<String> nb;
	public ArrayList<Date> timer;
	
	public boolean[] toSend;
	public Date[] sentTime;
	
	public DatagramSocket dsock = null;
	
	//constructor with pointers
	public SendThread(Date[] sTime, boolean[] toSend,
		ArrayList<String> neighbor,
			ArrayList<Date> time,
				DatagramSocket sock,
					ArrayList<String> ipPort,
						ArrayList<Double> weight,
							ArrayList<String> link){
		this.ipPort = ipPort;
		this.weight = weight;
		this.link = link;
		this.dsock  = sock;
		this.nb = neighbor;
		this.timer = time;
		this.toSend = toSend;
		this.sentTime = sTime;
	}
	
	public void run(){
		while(true){
			
			//stop blasting!!
			try {
				Thread.sleep(100);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
			
			if(toSend[0]){
				System.out.println("sending");
				sentTime[0] = new Date();
				//make a local copy of Routing Table
				ArrayList<String> mIpPort = new ArrayList<String>();
				ArrayList<Double> mWeight = new ArrayList<Double>();
				ArrayList<String> mLink = new ArrayList<String>();
				
				for(int i=0; i<weight.size(); i++){
					mIpPort.add(ipPort.get(i));
					mWeight.add(weight.get(i));
					mLink.add(link.get(i));
				}
				
				//make a local copy of neighbor
				ArrayList<String> mNb = new ArrayList<String>();
				for(int i=0; i<nb.size(); i++)
					mNb.add(nb.get(i));
				
				String msg;
				for(int i=0; i<mNb.size(); i++){
					msg = "";
					//construct packet
					for(int j=0; j<mWeight.size(); j++){
						//ignore unlinked neighbor
						if(mWeight.get(j) < Double.MAX_VALUE)
							msg += mIpPort.get(j) + ":" + mWeight.get(j) + ":";
					}
					
					byte arr[] = msg.getBytes();
					InetAddress add = Client.getAddress(mNb.get(i));
					int port = Client.getPort(mNb.get(i));
					DatagramPacket dpack = new DatagramPacket(arr, arr.length, add, port);
					
					//sending packet
					try {
						dsock.send(dpack);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				toSend[0] = false;
			}
		}
	}
}
