import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * class SnapshotServer
 * To receive snapshot from other processes to get their states and incoming channel states.
 * Will be running in Snapshot initiation process.
 * @author Raghav Babu
 * Date : 02/24/2016
 */
public class SnapshotServer extends Thread {

		private InetSocketAddress boundPort = null;
		private static int port;
		private ServerSocket serverSocket;
		private int id;
		Process process;

		public SnapshotServer(int  id,Process process) {
			this.id = id;
			this.process = process;
			port = ProcessIPPortXmlParser.processIDToSnapShotPortMap.get(id);
		}

		@Override
		public void run(){

			try {

				initServerSocket();
				
				//list to stores snaps from all processes.
				Set<PostSnapProcessState> states = new TreeSet<PostSnapProcessState>();
				List<Integer> stateIds = new ArrayList<Integer>();
				
				//runs infinitely
				while(true) {

					Socket connectionSocket;
					ObjectInputStream ois;
					InputStream inputStream;
					
					connectionSocket = serverSocket.accept();
					inputStream = connectionSocket.getInputStream();
					ois = new ObjectInputStream(inputStream);

					//receive process state from other process for a snapshot.
					PostSnapProcessState state = (PostSnapProcessState) ois.readObject();
					states.add(state);
					
					System.out.println("----------After Receiving a snap in snapshotServer---------");
					System.out.println("Id : "+state.processId);
					System.out.println("state : "+state.processState);
					System.out.println("snapshot : "+state.snapShotCount);
					System.out.println(process.incomingChannels.get(0)+" "+process.incomingChannels.get(1));
					System.out.println("Channel size : "+process.incomingChannels.size());
					System.out.println("------------------------------------------");
					
					stateIds.add(state.processId);
					
					Set<PostSnapProcessState> tmp = new TreeSet<PostSnapProcessState>();
					tmp.addAll(states);
					//update snapshot map.
					process.map.put(state.snapShotCount, tmp);	
					
					if(process.checkIfMarkerReceivedFromAllProcesses(stateIds)){
						stateIds.clear();
						states.clear();
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
					System.out.println("SnapShot Server bound to data port " + serverSocket.getLocalPort() + " and is ready...");
				}
			}
			catch (Exception e)
			{
				System.out.println("Unable to initiate socket.");
			}

		}

	}

