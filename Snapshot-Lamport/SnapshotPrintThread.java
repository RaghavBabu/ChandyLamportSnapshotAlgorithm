
/**
 * class SnapshotPrintThread
 * Thread which prints snapshot as and when server receives it and updates the map.
 * @author Raghav Babu
 *  Date : 02/24/2016
 */
public class SnapshotPrintThread extends Thread {

	Process process;

	public SnapshotPrintThread(Process process){
		this.process = process;
	}

	public void run(){

		int snap = 1;
		
		while(true){

			try {
			//to print the previous snapshot.
			if(process.map.containsKey(snap)){

				if(process.map.get(snap).size() == Process.totalProcess - 1){
					process.printSnapShot(process.map.get(snap), snap);
					snap += 1; //get next snap.
				}
			}else {
				continue;
			}
			}catch (Exception e ){
				e.printStackTrace();
			}

		}

	}
}
