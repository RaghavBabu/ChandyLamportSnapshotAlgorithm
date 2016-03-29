
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


/**
 * Class ValueToServerIPXmlParser
 * parse XML file.
 * @author Raghav Babu
 * Date : 02/24/2016
 */
public class ProcessIPPortXmlParser {


	static Map<Integer, String> processIDToIpMap = new HashMap<Integer, String>();
	static Map<Integer, Integer> processIDToEventPortMap = new HashMap<Integer, Integer>();
	static Map<Integer, Integer> processIDToSnapShotPortMap = new HashMap<Integer, Integer>();
	
	public void parseXML(){


		try {	
			File inputFile = new File("ProcessIPPort.xml");
			DocumentBuilderFactory factory  = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document doc = builder.parse(inputFile);

			//System.out.println("Root element :" + doc.getDocumentElement().getNodeName());
			NodeList nodeList = doc.getElementsByTagName("Process");

			for (int i = 0; i < nodeList.getLength(); i++) {

				Node nNode = nodeList.item(i);

				String id = null;
				String ipAddress = null;
				String eventPort = null;
				String snapShotPort= null;
				
				if (nNode.getNodeType() == Node.ELEMENT_NODE) {

					Element eElement = (Element) nNode;
					id = eElement.getAttribute("id");
					ipAddress = eElement.getAttribute("IPAddress");
					eventPort = eElement.getAttribute("EventPort") ;
					snapShotPort = eElement.getAttribute("SnapshotPort") ;
					
					processIDToIpMap.put(Integer.parseInt(id), ipAddress);
					processIDToEventPortMap.put(Integer.parseInt(id), Integer.parseInt( eventPort ));
					processIDToSnapShotPortMap.put(Integer.parseInt(id), Integer.parseInt( snapShotPort ));

					System.out.println("Id : "  + id+ ", IP : "+ipAddress+ ",eventPort : "+eventPort+ ", snapShot : "+snapShotPort);
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}

	}
}


