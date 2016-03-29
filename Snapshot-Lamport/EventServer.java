import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map.Entry;

/**
 * Class EventServer
 * Receives event from other process and changes process state and channel state accordingly.
 * @author Raghav Babu
 *
 */
public class EventServer extends Thread {

	private InetSocketAddress boundPort = null;
	private static int port;
	private ServerSocket serverSocket;
	private int id;
	private Process process;

	public EventServer(int  id, Process process) {
		this.id = id;
		this.process = process;
		port = ProcessIPPortXmlParser.processIDToEventPortMap.get(id);
	}

	@Override
	public void run(){

		try {

			initServerSocket();

			while(true) {

				Socket connectionSocket;
				ObjectInputStream ois;
				InputStream inputStream;


				connectionSocket = serverSocket.accept();
				inputStream = connectionSocket.getInputStream();
				ois = new ObjectInputStream(inputStream);

				//read event object.
				Event event = (Event)ois.readObject();

				//if amount transfer.
				if(event.eventType == EventType.TRANSFER) {

					//create a new event to print and show current state.
					event = new Event(EventType.RECEIVE, event.processId, event.amt, event.eventCount);
					System.out.println("Received an amount from process : "+event.processId);
					System.out.println(event);


					if(!Process.stateRecorded){

						System.out.println("State Not Recorded ");

						Process.currentTotalAmount += event.amt;
						System.out.println("After receiving "+event.amt+" from process "+event.processId+ ", "
								+ "current total Amount : "+Process.currentTotalAmount);
					}

					else{
						System.out.println("State Recorded : adding this amount : "+event.amt+ " to channel state and buffer");

						//adding to buffer.
						Process.bufferAmount += event.amt;

						//adding to channel state amount.
						for(Channel channel : process.incomingChannels){

							//System.out.println(event.processId +" "+channel.fromProcess);
							//System.out.println(channel.channelMarked);

							if(event.processId == channel.fromProcess && !channel.channelMarked){
								channel.channelStateVal += event.amt;
								System.out.println("Channel state changed "+channel);
							}
						}
						System.out.println("Buffer Amount Updated : "+Process.bufferAmount);
					}
				}

				// ping to other processes.
				else if(event.eventType == EventType.PING){
					System.out.println("Process : "+event.processId+" able to ping");
				}

				else if(event.eventType == EventType.MARKER) {

					System.out.println("Received Marker from process : "+event.processId);
					System.out.println(event);

					process.markers.add(event.processId);
					Process.markerRecvdFromAllProcesses = process.checkIfMarkerReceivedFromAllProcesses(process.markers);

					if(!Process.stateRecorded){

						if(event.processId == Process.snapShotProcessId) {
							System.out.println("------------------------------");
							System.out.println("Total Amount when receiving marker : "+event.eventCount+
									" from process "+ event.processId+" is "+Process.currentTotalAmount);
							System.out.println("------------------------------");
						}

						System.out.println("Marker Received : State not recorded, so save current process "
								+ "state and set this channel state to 0.");
						Process.processState = Process.currentTotalAmount;
						Process.stateRecorded = true;


						for(Channel channel : process.incomingChannels){

							if(event.processId == channel.fromProcess){
								channel.channelStateVal = 0;
								channel.channelMarked = true;
							}
						}

						Event marker = new Event(EventType.MARKER, Process.processId, event.eventCount);				

						if(process.channelStateBool){
							try {
								Thread.currentThread();
								Thread.sleep(1000);
							} catch (InterruptedException ex) {
								ex.printStackTrace();
							}
						}

						//prompt the client to send it to all destination processes.
						for(Entry<Integer, String> e : ProcessIPPortXmlParser.processIDToIpMap.entrySet() ){

							if(!e.getKey().equals(Process.processId)) {

								ProcessClient markerClient = new ProcessClient(marker, e.getKey());
								markerClient.send();
							}
						}

					}

					//if process,channel state recorded and it has received markers from all other processes.
					//then send the state to snap shot initiator, 
					else if(Process.stateRecorded && Process.markerRecvdFromAllProcesses ){


						if(Process.processId != Process.snapShotProcessId){

							System.out.println("Snapshot completed in this process, sending process and "
									+ " channels states to snapshot initiation process : "+Process.snapShotProcessId);

							PostSnapProcessState state = new PostSnapProcessState(Process.processId, Process.processState, event.eventCount, 
									process.incomingChannels);

							System.out.println("----------Before Sending a snap---------");
							System.out.println("Id : "+state.processId);
							System.out.println("state : "+state.processState);
							System.out.println("snap : "+state.snapShotCount);
							System.out.println(process.incomingChannels.get(0)+" "+process.incomingChannels.get(1));
							System.out.println("Channel size : "+process.incomingChannels.size());
							System.out.println("------------------------------------------");

							SnapshotClient snapshotClient = new SnapshotClient(state);
							snapshotClient.start();
							snapshotClient.join();
						}

						//adding the buffer amount to totalAmount and clearing buffer amount.

						Process.currentTotalAmount += Process.bufferAmount;
						System.out.println("Amount received after initiating snapshot, so adding buffer amount "+Process.bufferAmount+" ,"
								+ " so current Total Amount : "+Process.currentTotalAmount);


						Process.bufferAmount = 0;
						//Process.reduceBufferCount = 0;

						//clearing all gathered information about channel and process.
						process.markers.clear();
						Process.stateRecorded = false;
						Process.markerRecvdFromAllProcesses = false;

						//clearing all channels.
						for(Channel channel : process.incomingChannels){

							if(event.processId == channel.fromProcess){
								channel.channelStateVal = 0;
								channel.channelMarked = false;
							}
						}

					}
					//marker received, state of process already recorded but havnt received marker from all processes.
					else if(Process.stateRecorded){
						//nothing need to be done.
					}

				}
			}

		}catch(Exception e){
			System.out.println("Exception while receiving event in process : "+id+" ");
			e.printStackTrace();
		}

	}

	/**
	 * method which initialized and bounds a server socket to a port.
	 * @return void.
	 */
	private void initServerSocket()
	{
		boundPort = new InetSocketAddress(port);
		try
		{
			serverSocket = new ServerSocket(port);

			if (serverSocket.isBound())
			{
				System.out.println("Server bound to data port " + serverSocket.getLocalPort() + " and is ready...");
			}
		}
		catch (Exception e)
		{
			System.out.println("Unable to initiate socket.");
		}

	}

}
