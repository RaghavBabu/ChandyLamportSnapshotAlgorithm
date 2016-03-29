import java.io.Serializable;

/**
 * class Event
 * Event can be transfer or ping to another process or a Marker to start ssnapshot.
 * @author Raghav Babu
 * 
 */
public class Event implements Serializable {

	private static final long serialVersionUID = -793093149318224434L;
	int processId;
	int eventCount;
	EventType eventType;
	int amt;
	
	public Event(EventType eventType, int processId, int eventCount) {
		this.processId = processId;
		this.eventCount = eventCount;
		this.eventType = eventType;
	}

	
	public Event(EventType eventType, int fromProcessId, int randomAmt, int eventCount) {
		this.eventType = eventType;
		this.processId = fromProcessId;
		this.amt = randomAmt;
		this.eventCount = eventCount;
	}


	public Event(EventType eventType, int fromProcessId) {
		this.eventType = eventType;
		this.processId = fromProcessId;
	}


	@Override
	public String toString() {
		
		if(eventType == EventType.TRANSFER || eventType == EventType.RECEIVE){
			return "Event [type=" + eventType
					+ ", TransferCount=" + eventCount 
					+ ", amt=" + amt 
					+ "]";
		}
		else if(eventType == EventType.PING){
			return "Event [type=" + eventType+ "]";
		}
		else{
			return "Event [type=" + eventType
					+ ", SnapShotCount=" + eventCount 
					+ ", processId=" + processId 
				    + "]";
		}
	}

}
