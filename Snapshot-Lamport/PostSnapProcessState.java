import java.io.Serializable;
import java.util.List;

/**
 * Class PostSnapProcessState to return final snap shot to snapshot initiating process.
 * @author Raghav Babu
 * Date : 02/24/2016
 */
public class PostSnapProcessState implements Comparable<PostSnapProcessState>,Serializable{

	private static final long serialVersionUID = 6362068574600378344L;
	int processId;
	int snapShotCount;
	int processState;
	List<Channel> incomingChannels;
	
	public PostSnapProcessState(int processId, int processState, int snapShotCount, List<Channel> incomingChannels){
		this.processId = processId;
		this.processState = processState;
		this.snapShotCount = snapShotCount;
		this.incomingChannels = incomingChannels;	
	}

	@Override
	public String toString() {
		return "PostSnapProcessState [processId=" + processId
				+ ", snapShotCount=" + snapShotCount + ", processState="
				+ processState + ", incomingChannels=" + incomingChannels + "]";
	}

	

	@Override
	public int compareTo(PostSnapProcessState o) {
		return this.processId - o.processId;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime
				* result
				+ ((incomingChannels == null) ? 0 : incomingChannels.hashCode());
		result = prime * result + processId;
		result = prime * result + processState;
		result = prime * result + snapShotCount;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		PostSnapProcessState other = (PostSnapProcessState) obj;
		if (incomingChannels == null) {
			if (other.incomingChannels != null)
				return false;
		} else if (!incomingChannels.equals(other.incomingChannels))
			return false;
		if (processId != other.processId)
			return false;
		if (processState != other.processState)
			return false;
		if (snapShotCount != other.snapShotCount)
			return false;
		return true;
	}
}
