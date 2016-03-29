import java.io.Serializable;

/**
 * Class Channel.
 * Channel object to hold state of each incoming channels.
 * @author Raghav Babu
 */
public class Channel implements Serializable {

	private static final long serialVersionUID = -8222410563560336658L;
	int fromProcess;
	int toProcess;
	int channelStateVal;
	boolean channelMarked;
	
	
	public Channel(int fromProcess, int toProcess, int channelStateVal, boolean channelMarked) {
		this.fromProcess = fromProcess;
		this.toProcess = toProcess;
		this.channelStateVal = channelStateVal;
		this.channelMarked =channelMarked;
	}


	@Override
	public String toString() {
		return "Channel [fromProcess=" + fromProcess + ", toProcess="
				+ toProcess + ", channelStateVal=" + channelStateVal
				+ ", channelMarked=" + channelMarked + "]";
	}
}
