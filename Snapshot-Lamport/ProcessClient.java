

import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;

/**
 * class ProcessClient
 * Sends marker or transfer amount to other processes.
 * @author Raghav Babu
 * Date : 02/24/2016
 */
public class ProcessClient{

	Event event;
	PostSnapProcessState snapState;
	int toProcessId;
	String toIPAddress = null;
	int  toPort;
	

	public ProcessClient(Event event, int toProcessId) {
		this.event = event;
		this.toProcessId = toProcessId;
		this.toIPAddress = ProcessIPPortXmlParser.processIDToIpMap.get(toProcessId);
		this.toPort = ProcessIPPortXmlParser.processIDToEventPortMap.get(toProcessId);
	}

	public boolean send() {

		try {

			Socket socket = null;


			try {
				socket = new Socket(toIPAddress, toPort);
			} catch (Exception e) {
				System.out.println("Server in "+toIPAddress+ " not yet bound to "+toPort);
				System.out.println("Start the Process server in "+toIPAddress+ " at port "+toPort);
				return false;
			}

			OutputStream os = null;
			ObjectOutputStream oos = null;

			//send event object. 
			if(event.eventType == EventType.TRANSFER){

				System.out.println("Sending amount "+event.amt+ " to process : "+toProcessId);
				os = socket.getOutputStream();
				oos = new ObjectOutputStream(os);
				oos.writeObject(event);

				Process.currentTotalAmount -= event.amt;
				System.out.println("Current Total Amount : "+Process.currentTotalAmount);

			}
			//if ping event. for snapshot initiation.
			else if(event.eventType == EventType.PING){
				System.out.println(event);
				System.out.println("Trying to ping to process : "+toProcessId+ " so that snapshot can be initiated.");
				os = socket.getOutputStream();
				oos = new ObjectOutputStream(os);
				oos.writeObject(event);
			}
			//send marker to other process.
			else{
				System.out.println("Sending marker : "+event.eventCount+" to process : "+toProcessId);
				os = socket.getOutputStream();
				oos = new ObjectOutputStream(os);
				oos.writeObject(event);
			}

			socket.close();
		}catch (Exception e){
			System.out.println("Exception while passing event object to  "+toIPAddress);
			e.printStackTrace();
			return false;
		}
		return true;
	}
}
