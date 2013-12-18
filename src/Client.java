import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;

public class Client {

	public static boolean execute = true;
	
	public static void main(String[] args) {
		
		//Routing Table
		ArrayList<String> ipPort = new ArrayList<String>();
		ArrayList<Double> weight = new ArrayList<Double>();
		ArrayList<String> link = new ArrayList<String>();
		
		//neighbors
		ArrayList<String> nb = new ArrayList<String>();
		ArrayList<Date> timer = new ArrayList<Date>();
		
		//send boolean
		boolean toSend[] = {false};
		Date sentTime[] = {new Date()};
		
		Hashtable<String, Double> stored = new Hashtable<String, Double>();
		
		
		DatagramSocket dsock = null;
		int localPort = 0;
		double timeOut = 0;
		String localHost = "";
		
		BufferedReader in = null;
		
		//make sure user is using program correctly
		if(args.length < 5){
			System.out.println("usage: java Client <localport> <timeout> [ipaddress1 port1 weight1 ...]");
			return;
		}else if((args.length - 2)%3 != 0){
			System.out.println("syntax error in neighbor declaration");
			return;
		}else{
			try{
				localPort = Integer.parseInt(args[0]);
				timeOut = Double.parseDouble(args[1]);
				Socket tsock = new Socket("google.com", 80);
				localHost = tsock.getLocalAddress().getHostAddress();
				
				in = new BufferedReader(new InputStreamReader(System.in));
				dsock = new DatagramSocket(localPort);
				
				//constructing Routing Table
				int size = (args.length - 2)/3;
				for(int i=0; i<size; i++){
					//replace localhost with true ip
					String add = "";
					Double cost = Double.parseDouble(args[i*3 + 4]);
					if(args[i*3 +2].equals("localhost"))
						add = localHost;
					else
						add = getAddress(args[i*3 +2]).toString().split("/")[1];
					String addPort = add+":"+args[i*3 + 3];
					
					nb.add(addPort);
					ipPort.add(addPort);
					link.add(addPort);
					weight.add(cost);
					timer.add(new Date());
					stored.put(addPort, cost);
				}
			}catch (Exception e){
				System.err.println("Make sure inputs are valid");
			}
		}
		
		ListenThread listen = new ListenThread(stored, toSend, localHost, nb, timer, dsock, ipPort, weight, link);
		SendThread send = new SendThread(sentTime, toSend, nb, timer, dsock, ipPort, weight, link);
		TimerThread time = new TimerThread(nb, timer, toSend, sentTime, timeOut, weight, link);
		
		listen.start();
		send.start();
		time.start();
		
		while(true){
			String input = "";
			System.out.println("well?");
			
			try {
				input = in.readLine();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			System.out.println(toString(ipPort, weight, link));
			
			if(input.equals("nb")){
				for(int i=0; i<nb.size(); i++){
					System.out.println(nb.get(i) + " " + stored.get(nb.get(i)));
				}
			}
			
			
			if(input.equals("SHOWRT")){
				System.out.println(toString(ipPort, weight, link));
			}
			
			if(input.contains("LINKDOWN")){
				String[] tokens = input.split(" ");
				
				if(tokens.length != 3){
					System.out.println("LINKDOWN <ip_address> <port>");
				}else{
					String currAdd = tokens[1] + ":" + tokens[2];
					
					//if such link exists as a neighbor
					if(nb.contains(currAdd)){
						//delete from neighbor list
						int index = nb.indexOf(currAdd);
						nb.remove(index);
						timer.remove(index);
						
						//find links in the Routing Table
						//update cost to MAX_VALUE
						for(int i=0; i<weight.size(); i++){
							if(link.get(i).equals(currAdd)){
								weight.set(i, Double.MAX_VALUE);
								
								//if links deleted are still in nb; restore it
								if(nb.contains(ipPort.get(i))){
									index = nb.indexOf(ipPort.get(i));
									weight.set(i, stored.get(ipPort.get(i)));
									link.set(i, ipPort.get(i));
									System.out.println("here!");
								}
							}
						}
						
						//send LINKDOWN message to the neighbor
						byte arr[] = "LINKDOWN".getBytes();
						InetAddress add = Client.getAddress(currAdd);
						int port = Client.getPort(currAdd);
						DatagramPacket dpack = new DatagramPacket(arr, arr.length, add, port);
						try {
							dsock.send(dpack);
						} catch (IOException e) {
							e.printStackTrace();
						}
						
					}else{
						System.out.println(currAdd + " is not linked");
					}
				}
			}
			
			if(input.contains("LINKUP")){
				String[] tokens = input.split(" ");
				if(tokens.length != 3){
					System.out.println("LINKDOWN <ip_address> <port>");
				}else{
					String currAdd = tokens[1] + ":" + tokens[2];
					//if such link was a neighbor
					//find in Routing Table and change weight&link
					if(stored.containsKey(currAdd)){
						for(int i=0; i<weight.size(); i++){
							if(ipPort.get(i).equals(currAdd)){
								weight.set(i, stored.get(currAdd));
								link.set(i, currAdd);
							}
						}
						//add back to neighbor list
						nb.add(currAdd);
						timer.add(new Date());
						
						//send LINKUP message to the neighbor
						byte arr[] = "LINKUP".getBytes();
						InetAddress add = Client.getAddress(currAdd);
						int port = Client.getPort(currAdd);
						DatagramPacket dpack = new DatagramPacket(arr, arr.length, add, port);
						try {
							dsock.send(dpack);
						} catch (IOException e) {
							e.printStackTrace();
						}
					}else{
						System.out.println("Nothing to restore");
					}
				}
			}
			
			if(input.contains("CLOSE")){
				execute = false;
				return;
			}
			
		}
		
	}

	static public int getPort(String ipPort){
		return Integer.parseInt(ipPort.split(":")[1]);
	}
	
	static public InetAddress getAddress(String ipPort){
		try {
			return InetAddress.getByName(ipPort.split(":")[0]);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	static public double getWeight(String tuple){
		return Double.parseDouble(tuple.split(":")[2]);
	}
	
	static String toString(ArrayList<String> ipPort,
							ArrayList<Double> weight,
								ArrayList<String> link){
		String s = "";
		for(int i=0; i<ipPort.size() ;i++){
			if(weight.get(i) < Double.MAX_VALUE){
				s += "Destination = " + ipPort.get(i) + ", "; 
				s += "Cost = " + weight.get(i) +", ";
				s += "Link = (" + link.get(i) + ")\n";
			}
		}
		return s;
	}
}
