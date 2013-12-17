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

		while (true) {
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
				}else{
					//if not in stored, it is not a neighbor; add to neighbor
					int index = cIpPort.indexOf(myAddPort);
					nb.add(linkAddPort);
					timer.add(new Date());
					toSend[0] = true;
					stored.put(linkAddPort, cWeight.get(index));
					
					
					
					
					
				}
				
				
				// update current Routing Table
				for (int i = 0; i < cWeight.size(); i++) {

					String currIpPort = cIpPort.get(i);
					// if referring to self
					if (currIpPort.equals(myAddPort)) {
						// check if already on nb list
						if (!nb.contains(linkAddPort)) {
							// if not, add to nb
							nb.add(linkAddPort);
							timer.add(new Date());
							toSend[0] = true;
							stored.put(linkAddPort, cWeight.get(i));
							
							//if not in routing table, add it
							if(!ipPort.contains(linkAddPort)){
								ipPort.add(linkAddPort);
								weight.add(cWeight.get(i));
								link.add(linkAddPort);
							//else update routing table
							}else{
								
							}
							
						} else {
							//if it is in nb list; log its time
							for (int j = 0; j < nb.size(); j++) {
								if (nb.get(j).equals(linkAddPort)){
									timer.set(j, new Date());
								}
							}
							//update routing table
							for (int j = 0; j < weight.size(); j++) {
								if (ipPort.get(j).equals(currIpPort)
										&& weight.get(j) > cWeight.get(i)) {
									weight.set(j, cWeight.get(i));
									link.set(j, linkAddPort);
									toSend[0] = true;
								}
							}
						}
						// update Routing Table
					} else if (ipPort.contains(currIpPort)) {
						for (int j = 0; j < weight.size(); j++) {
							if (ipPort.get(j).equals(currIpPort)
									&& weight.get(j) > cWeight.get(i)) {
								weight.set(j, cWeight.get(i));
								link.set(j, linkAddPort);
								toSend[0] = true;
							}
						}
					} else {
						ipPort.add(cIpPort.get(i));
						weight.add(cWeight.get(i));
						link.add(linkAddPort);
						toSend[0] = true;
					}
				}
			}
			
			//if LINKDOWN message
			if(msg.equals("LINKDOWN")){
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
					}
				}
			}
			
			//if LINKUP message
			if(msg.equals("LINKUP")){
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
