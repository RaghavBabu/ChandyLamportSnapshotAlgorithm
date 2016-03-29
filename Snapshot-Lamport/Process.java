import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.Map.Entry;

/**
 * Class Process
 * Each Process running initiates an event.
 * Event can be transfer of amount to another process which happens every 1 second
 * and sending of marker which happens every 2 seconds.
 * Marker initiates snapshot algorithm.
 * @author Raghav Babu
 * Date : 03/22/2016
 */

public class Process{

	static int processId;
	static int currentTotalAmount;
	static int snapShotCount;
	static boolean stateRecorded = false;
	static int processState;
	static int totalProcess;
	static int bufferAmount;
	static int reduceBufferCount;
	static int snapShotProcessId;
	static int prevRandom;
	static boolean markerRecvdFromAllProcesses;

	//boolean to view channel state during exchange.
	boolean channelStateBool;

	List<Integer> markers;
	List<Channel> incomingChannels;
	Map<Integer, Set<PostSnapProcessState>> map;

	public Process(){

		this.markers = new ArrayList<Integer>(totalProcess - 1);
		this.incomingChannels = new LinkedList<Channel>();
		this.map = new HashMap<Integer, Set<PostSnapProcessState>>();
	}

	/**
	 * Main function.
	 * @param args
	 */
	public static  void main(String args[]) {

		//command line args.
		processId = Integer.parseInt(args[0]);
		snapShotProcessId = Integer.parseInt(args[1]);
		totalProcess = Integer.parseInt(args[2]);

		currentTotalAmount = 1000;

		Process process = new Process();
		process.channelStateBool = new Boolean(args[3]);

		//parse XML file.
		ProcessIPPortXmlParser parser = new ProcessIPPortXmlParser();
		parser.parseXML();

		process.initiateProcess(process);
	}

	/**
	 * initiation of transaction process and snaphot algorithm.
	 * @param process
	 */
	private void initiateProcess(Process process) {


		//create incoming channels based on the number of processes.
		for(Entry<Integer, String> e : ProcessIPPortXmlParser.processIDToIpMap.entrySet() ){

			if(!e.getKey().equals(processId)) {
				Channel channel = new Channel(e.getKey(), processId, 0, false);
				incomingChannels.add(channel);
			}	
		}

		//start server in each process.
		EventServer server = new EventServer(processId, process);
		server.start();

		//start snapshot server in each process.
		if(processId == snapShotProcessId){
			SnapshotServer snapshotServer = new SnapshotServer(processId, process);
			snapshotServer.start();

			SnapshotPrintThread printer = new SnapshotPrintThread(process);
			printer.start();
		}

		//counter for sending marker.
		int counter = 0;
		int transferCount = 0;
		boolean flag = false;
		boolean markerProceed = false;

		//flag to create different type of randomness.
		boolean randomFlag = true;

		int randomAmt;

		while(true) {

			randomAmt = new Random().nextInt(100) + 1;

			//handling case such that amount doesnt go in negative.
			if( (Process.currentTotalAmount - randomAmt) <  0)  {
				continue;
			}

			Event event = null;

			if(randomFlag)
			{
				//choosing a random process to send to.
				int randomToProcess =  chooseRandomProcessId();

				//choosing a different process id other than its own to send money.
				while(randomToProcess == processId){
					randomToProcess = chooseRandomProcessId();
				}

				transferCount += 1;
				event = new Event(EventType.TRANSFER, processId, randomAmt, transferCount);

				//prompt the client to send it to destination process.
				ProcessClient client = new ProcessClient(event, randomToProcess);
				client.send();
			}

			//sending amount to all processes. (for testing channel state).
			else{

				event = new Event(EventType.TRANSFER, processId, randomAmt, transferCount);

				for(Entry<Integer, String> e : ProcessIPPortXmlParser.processIDToIpMap.entrySet() ){

					if(!e.getKey().equals(Process.processId)) {
						transferCount += 1;
						ProcessClient client = new ProcessClient(event, e.getKey());
						client.send();
					}
				}
			}

			try {
				Thread.currentThread();
				Thread.sleep(1000);
				counter += 1;
			} catch (InterruptedException e) {
				e.printStackTrace();
			}



			//snapshot initiating process sends the marker to  random other process. 
			if(counter % 2 == 0 && processId == snapShotProcessId){

				//send a dummy events to all processes to check if they are ready.(first time)
				if(!flag){
					Event eve = new Event(EventType.PING, processId);

					for(Entry<Integer, String> e : ProcessIPPortXmlParser.processIDToIpMap.entrySet() ){

						if(!e.getKey().equals(Process.processId)) {
							ProcessClient dummySend = new ProcessClient(eve, e.getKey());

							if(dummySend.send()){
								markerProceed = true;
							}else{
								markerProceed = false;
							}
						}
					}
				}

				//flag to check if SNAPshot can be started, it checks if all process running
				if(markerProceed){

					if(!flag){
						System.out.println("All processes up and running, snapshot can be initiated.");
					}
					//storing process state before sending marker.
					Process.processState = currentTotalAmount;

					//setting stateRecorded as true to track duplicate marker.
					Process.stateRecorded = true;

					//increment snapshot count.
					snapShotCount += 1;
					Event marker = new Event( EventType.MARKER, processId, snapShotCount);

					//prompt the client to send it to all destination processes.
					for(Entry<Integer, String> e : ProcessIPPortXmlParser.processIDToIpMap.entrySet() ){

						if(!e.getKey().equals(Process.processId)) {

							ProcessClient markerClient = new ProcessClient(marker, e.getKey());
							markerClient.send();
						}
					}

					flag = true;
				}
			}
		}
	}

	/**
	 * Print snapshot in process which initiated it.
	 * @param states
	 * @param snap
	 */
	public void printSnapShot(Set<PostSnapProcessState> states, int snap) {

		//printing state from this process.
		System.out.println("-----------------------Snapshot : "+snap+"------------------");
		System.out.println("Process : "+Process.processId+ ", state : "+Process.processState);

		for(Channel channel : incomingChannels){
			System.out.println(" Channel "+channel.fromProcess+ "-"+channel.toProcess+ " : "+channel.channelStateVal);
			channel.channelStateVal = 0;
		}

		//printing states from other process.
		for(PostSnapProcessState state : states){
			System.out.println("Process : "+state.processId+ ", state : "+state.processState);

			for(Channel channel : state.incomingChannels)
				System.out.println(" Channel "+channel.fromProcess+ "-"+channel.toProcess+ " : "+channel.channelStateVal);
		}

		System.out.println("-------------------------------------------------------------");
	}

	/**
	 * Check if marker received from all other processes.
	 * @param markers
	 * @return true or false.
	 */
	public boolean checkIfMarkerReceivedFromAllProcesses(List<Integer> markers) {

		//create incoming channels based on the number of processes.
		for(Entry<Integer, String> e : ProcessIPPortXmlParser.processIDToIpMap.entrySet() ){

			if(e.getKey().equals(Process.processId) || markers.contains(e.getKey())) {
				continue;
			}else{
				return false;
			}
		}
		return true;
	}

	/**
	 * Generate a random process id to send to.
	 * @return integer 
	 */
	private int chooseRandomProcessId() {

		int random;
		do 
			random = (int) ((Math.random() * 10) % totalProcess) + 1;
		while (random == prevRandom);

		prevRandom = random;

		return random;
	}
}

