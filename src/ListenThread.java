import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;

public class ListenThread extends Thread {

	public ArrayList<String> ipPort;
	public ArrayList<Double> weight;
	public ArrayList<String> link;
	public DatagramSocket dsock = null;
	public ArrayList<Date> timer;
	public ArrayList<String> nb;
	public String localHost;
	public Hashtable<String, Double> stored;
	
	public boolean[] toSend;

	// constructor with pointers
	public ListenThread(Hashtable<String, Double> store,
			boolean[] toSend, String localhost,
			ArrayList<String> neighbors, ArrayList<Date> time,
			DatagramSocket sock, ArrayList<String> ipPort,
			ArrayList<Double> weight, ArrayList<String> link) {
		this.ipPort = ipPort;
		this.weight = weight;
		this.link = link;
		this.dsock = sock;
		this.timer = time;
		this.localHost = localhost;
		this.nb = neighbors;
		this.toSend = toSend;
		this.stored = store;
	}

	public void run() {
		int maxSize = 8000;
		DatagramPacket dpack = new DatagramPacket(new byte[maxSize], maxSize);
		
		while (Client.execute) {
			// if route table is updated set to true;
			// if updated is true at the end; set toSend[0] = true
			boolean updated = false;
			
			try {
				dsock.receive(dpack);
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			// parse info from receiver
			String linkAdd = dpack.getAddress().toString().split("/")[1];
			int linkPort = dpack.getPort();
			String linkAddPort = linkAdd + ":" + linkPort;
			String myAddPort = localHost + ":" + dsock.getLocalPort();

			
//			for (int i = 0; i < weight.size(); i++) {
//				if (ipPort.get(i).equals(linkAddPort))
//					defaultCost = weight.get(i);
//			}

			// if it is a Routing Table
			String msg = new String(dpack.getData(), 0, dpack.getLength());
			String[] tokens = msg.split(":");
			ArrayList<String> cIpPort = new ArrayList<String>();
			ArrayList<Double> cWeight = new ArrayList<Double>();
			
			// tries to catch bad data
			if (tokens.length % 3 == 0) {	
				// reconstruct routing table of neighbor
				for (int i = 0; i < tokens.length / 3; i++) {
					cIpPort.add(tokens[i * 3] + ":" + tokens[i * 3 + 1]);
					cWeight.add(Double.parseDouble(tokens[i * 3 + 2]));
				}
				// find default cost to received node
				double defaultCost = 0;
				if(nb.contains(linkAddPort)){
					defaultCost = stored.get(linkAddPort);
					int index = nb.indexOf(linkAddPort);
					timer.set(index, new Date());
				}else{
					//if not in stored, it is not a neighbor; add to neighbor
					int index = cIpPort.indexOf(myAddPort);
					nb.add(linkAddPort);
					timer.add(new Date());
					toSend[0] = true;
					stored.put(linkAddPort, cWeight.get(index));
					
					//update routing table
					//if not in routing table, add it; else edit it
					if(!ipPort.contains(linkAddPort)){
						ipPort.add(linkAddPort);
						weight.add(cWeight.get(index));
						link.add(linkAddPort);
					}else{
						int myIndex = ipPort.indexOf(linkAddPort);
						weight.set(myIndex, cWeight.get(index));
						link.set(myIndex, linkAddPort);
					}
					updated = true;
				}
				
				// update current Routing Table based on 
				// neighbor's routing table
				for (int i = 0; i < cWeight.size(); i++) {
					String currIpPort = cIpPort.get(i);
					Double currWeight = cWeight.get(i);
					// if referring to self; do nothing; we already check for this
					// if not referring to self, update routing table
					if (!currIpPort.equals(myAddPort)){						
						// update Routing Table
						//if in routing table; check if needs to be updated
						if (ipPort.contains(currIpPort)) {
							int index = ipPort.indexOf(currIpPort);
							
							//
							if(link.get(index).equals(linkAddPort)){
								weight.set(index, defaultCost + currWeight);
								updated = true;
							}
							
							
							
							double direct = Double.MAX_VALUE;
							double min = Double.MAX_VALUE;
							
							if(nb.contains(currIpPort))
								direct = stored.get(currIpPort);
							
							min = Math.min(direct, defaultCost + currWeight);
							min = Math.min(min, weight.get(index));
							
							if(min != weight.get(index)){
								weight.set(index, min);
								if(min == direct){
									link.set(index, currIpPort);
								}else{
									link.set(index, linkAddPort);
								}
								updated = true;
							}
							
						//otherwise just add it
						}else {
							ipPort.add(currIpPort);
							weight.add(defaultCost + currWeight);
							link.add(linkAddPort);
							updated = true;
						}
					}
				}
				if(updated)
					toSend[0] = true;
			}
			
			//if LINKDOWN message
			if(msg.equals("LINKDOWN")){
				System.out.println("LINKDOWN");
				//delete from neighbor list
				for(int i=0; i<nb.size(); i++){
					if(nb.get(i).equals(linkAddPort)){
						nb.remove(i);
						timer.remove(i);
					}
				}
				
				//set cost of any link to infinity
				for(int i=0; i<weight.size(); i++){
					if(link.get(i).equals(linkAddPort)){
						weight.set(i, Double.MAX_VALUE);
						
						if(nb.contains(ipPort.get(i))){
							int index = nb.indexOf(ipPort.get(i));
							weight.set(i, stored.get(ipPort.get(i)));
							link.set(i, ipPort.get(i));
						}
					}
				}
			}
			
			//if LINKUP message
			if(msg.equals("LINKUP")){
				System.out.println("received LINKUP");
				//add back to neighbor list
				nb.add(linkAddPort);
				timer.add(new Date());
				
				//update Routing Table
				for(int i=0; i<weight.size(); i++){
					if(ipPort.get(i).equals(linkAddPort)){
						weight.set(i, stored.get(linkAddPort));
						link.set(i, linkAddPort);
					}
				}
			}
			
		}
	}
}
