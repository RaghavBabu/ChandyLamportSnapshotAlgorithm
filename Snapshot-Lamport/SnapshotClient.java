import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;

/**
 * class SnapshotClient
 * Client thread runs in all processes other than snapshot initiation process to send process snap to it.
 * Whenever marker received from all other processes it sends a snap.
 * @author Raghav Babu
 * Date : 02/24/2016
 */
public class SnapshotClient extends Thread {

	PostSnapProcessState state;
	String toIPAddress = null;
	int  toPort;

	public SnapshotClient(PostSnapProcessState state) {
		this.state = state;
		this.toIPAddress = ProcessIPPortXmlParser.processIDToIpMap.get(Process.snapShotProcessId);
		this.toPort = ProcessIPPortXmlParser.processIDToSnapShotPortMap.get(Process.snapShotProcessId);
	}

	public void run(){
		
		try {

			Socket socket = null;


			try {
				socket = new Socket(toIPAddress, toPort);
			} catch (Exception e) {
				System.out.println("Server in "+toIPAddress+ " not yet bound to "+toPort);
				System.out.println("Start the Process server in "+toIPAddress+ " at port "+toPort);
				return;
			}

			OutputStream os = null;
			ObjectOutputStream oos = null;

			System.out.println("Snapshot "+state.snapShotCount+" completed, so sending PostSnap Process state to "
						+Process.snapShotProcessId);
			
			//send event object. 
			os = socket.getOutputStream();
			oos = new ObjectOutputStream(os);
			oos.writeObject(state);

			socket.close();
			
		}catch (Exception e){
			System.out.println("Exception while passing event object to  "+toIPAddress);
			e.printStackTrace();
		}
	}
}
