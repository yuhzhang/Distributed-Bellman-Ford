import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;

public class Client {

	public static void main(String[] args) {
		
		ArrayList<String> ipPort = new ArrayList<String>();
		ArrayList<Double> weight = new ArrayList<Double>();
		ArrayList<String> link = new ArrayList<String>();
		
		int localPort;
		double timeOut;
		
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
				timeOut = (double) Float.parseFloat(args[1]);
				
				int size = (args.length - 2)/3;
				for(int i=0; i<size; i++){
					ipPort.add(args[i*3 + 2]+":"+args[i*3 + 3]);
					link.add(args[i*3 + 2]+":"+args[i*3 + 3]);
					weight.add((double) Float.parseFloat(args[i*3 + 4]));
				}
			}catch (Exception e){
				System.err.println("Make sure inputs are valid");
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
}
