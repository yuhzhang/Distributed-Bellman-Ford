import java.util.ArrayList;
import java.util.Date;


public class TimerThread extends Thread{

	ArrayList<String> nb;
	ArrayList<Date> timer;
	boolean[] toSend;
	Date[] sentTime;
	double timeOut;
	
	ArrayList<Double> weight;
	ArrayList<String> link;
	
	public TimerThread(ArrayList<String> neighbor,
						ArrayList<Date> timer, boolean[] toSend, 
							Date[] sentTime, double timeOut,
								ArrayList<Double> weight,
									ArrayList<String> link){
		this.timer = timer;
		this.toSend = toSend;
		this.sentTime = sentTime;
		this.timeOut = timeOut;
		this.nb = neighbor;
		this.weight = weight;
		this.link = link;
	}
	
	public void run(){
		while(Client.execute){
			Date currTime = new Date();
			long time = 0;

			//TIMEOUT rule for sending
			if(!toSend[0]){
				time = (currTime.getTime() - sentTime[0].getTime())/1000;
				if(time > timeOut){
					toSend[0] = true;
				}
			}
			//TIMEOUT rule for 3*TIMEOUT
			int i = 0;
			
			while(i < timer.size()){
				time = (currTime.getTime() - timer.get(i).getTime())/1000;
				
				//remove from neighbor list
				//update Routing Table
				if(time > 3 * timeOut){
					String add = nb.remove(i);
					timer.remove(i);
					
					for(int j=0; j<weight.size(); j++){
						if(link.get(j).equals(add))
							weight.set(j, Double.MAX_VALUE);
					}
					
					i = -1; //start at 0 again
				}
				i++;
			}
		}
	}
}
