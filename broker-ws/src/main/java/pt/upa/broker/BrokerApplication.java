package pt.upa.broker;

import javax.xml.ws.Endpoint;

import pt.ulisboa.tecnico.sdis.ws.uddi.UDDINaming;
import pt.upa.broker.ws.BrokerPort;
import pt.upa.broker.ws.BrokerPortType;

public class BrokerApplication {
	private static BrokerPortType port;

	public static void main(String[] args) throws Exception {
		System.out.println(BrokerApplication.class.getSimpleName() + " starting...");
		
		String uddiURL = args[0];
		String name = args[1];
		String url = args[2];
		boolean mode = Boolean.parseBoolean(args[3]);

		register(uddiURL, name, url, mode);
			
	}
	
	public static void register(String uddiURL, String name, String url, boolean mode){		
		
		Endpoint endpoint = null;
		UDDINaming uddiNaming = null;
		if (mode)  {
			name = name + "Secundary";
			url = "http://localhost:8085/broker-wsSecundary/endpoint";
		}
			
		try {

			port = new BrokerPort(2, mode); //TODO QUANTAS UPAS COMO ARGUMENTO
			endpoint = Endpoint.create(port);
			
			// publish endpoint
			System.out.printf("Starting %s%n", url);
			endpoint.publish(url);
			
			// publish to UDDI
			System.out.printf("Publishing '%s' to UDDI at %s%n", name, uddiURL);
			uddiNaming = new UDDINaming(uddiURL);
			uddiNaming.rebind(name, url);
			((BrokerPort) port).registerAssets(uddiNaming, endpoint, name, url);
			
			// wait
			System.out.println("Awaiting connections");
			System.out.println("Press enter to shutdown");
			System.in.read();
			//((BrokerPort) port).stop();
		} 
		catch (Exception e) {
			System.out.printf("Caught exception: %s%n", e);
			e.printStackTrace();
		} 
		finally {
			((BrokerPort) port).stopWS();
		}
	}
	

}
