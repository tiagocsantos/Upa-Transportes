package pt.upa.broker.ws.cli;

import static javax.xml.ws.BindingProvider.ENDPOINT_ADDRESS_PROPERTY;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.xml.registry.JAXRException;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.WebServiceException;

import pt.ulisboa.tecnico.sdis.ws.uddi.UDDINaming;
import pt.upa.broker.ws.BrokerPortType;
import pt.upa.broker.ws.BrokerService;
import pt.upa.broker.ws.InvalidPriceFault_Exception;
import pt.upa.broker.ws.TransportView;
import pt.upa.broker.ws.UnavailableTransportFault_Exception;
import pt.upa.broker.ws.UnavailableTransportPriceFault_Exception;
import pt.upa.broker.ws.UnknownLocationFault_Exception;
import pt.upa.broker.ws.UnknownTransportFault_Exception;

public class BrokerClient {
	private BrokerPortType port;
	private String uddiURL;
	private String name;
	private UDDINaming uddiNaming;
	private String endpointAddress;
	
	public BrokerClient(String url){
		uddiURL = url;
		name = "UpaBroker";
		
		System.out.printf("Contacting UDDI at %s%n", uddiURL);
		uddiNaming = null;
		try {
			uddiNaming = new UDDINaming(uddiURL);
		} catch (JAXRException e) {
			System.out.println("Servidor não Encontrado");
		}

		System.out.printf("Looking for '%s'%n", name);
		endpointAddress = null;
		try {
			endpointAddress = uddiNaming.lookup(name);
		} catch (JAXRException e) {
			System.out.println("Serviço não encontrado");
		}

		if (endpointAddress == null) {
			System.out.println("Not found!");
			return;
		} else {
			System.out.printf("Found %s%n", endpointAddress);
		}
		
		System.out.println("Creating stub ...");
		BrokerService service = new BrokerService();
		port = service.getBrokerPort();

		System.out.println("Setting endpoint address ...");
		BindingProvider bindingProvider = (BindingProvider) port;
		Map<String, Object> requestContext = bindingProvider.getRequestContext();
		requestContext.put(ENDPOINT_ADDRESS_PROPERTY, endpointAddress);
		
		  int connectionTimeout = 3000;
          // The connection timeout property has different names in different versions of JAX-WS
          // Set them all to avoid compatibility issues
          final List<String> CONN_TIME_PROPS = new ArrayList<String>();
          CONN_TIME_PROPS.add("com.sun.xml.ws.connect.timeout");
          CONN_TIME_PROPS.add("com.sun.xml.internal.ws.connect.timeout");
          CONN_TIME_PROPS.add("javax.xml.ws.client.connectionTimeout");
          // Set timeout until a connection is established (unit is milliseconds; 0 means infinite)
          for (String propName : CONN_TIME_PROPS)
              requestContext.put(propName, connectionTimeout);
          System.out.printf("Set connection timeout to %d milliseconds%n", connectionTimeout);

          int receiveTimeout = 3000;
          // The receive timeout property has alternative names
          // Again, set them all to avoid compability issues
          final List<String> RECV_TIME_PROPS = new ArrayList<String>();
          RECV_TIME_PROPS.add("com.sun.xml.ws.request.timeout");
          RECV_TIME_PROPS.add("com.sun.xml.internal.ws.request.timeout");
          RECV_TIME_PROPS.add("javax.xml.ws.client.receiveTimeout");
          // Set timeout until the response is received (unit is milliseconds; 0 means infinite)
          for (String propName : RECV_TIME_PROPS)
              requestContext.put(propName, 1000);
          System.out.printf("Set receive timeout to %d milliseconds%n", receiveTimeout);
		
		
	}

	public String requestTransport(String origin, String destination, int price) 
			throws InvalidPriceFault_Exception, UnavailableTransportFault_Exception, 
					UnavailableTransportPriceFault_Exception, UnknownLocationFault_Exception {
		try {
			return port.requestTransport(origin, destination, price);
		}
		catch (WebServiceException e){
			System.out.println("Caught: " + e);
			try {
				Thread.sleep(4000);
				return port.requestTransport(origin, destination, price);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
			}
		return null;
	}


	public String ping(String id) {
		try {
			return port.ping(id);
		}
		catch (WebServiceException e){
			System.out.println("Caught: " + e);
			try {
				Thread.sleep(4000);
				return port.ping(id);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
			
		}
		return null;
	}

	public List<TransportView> listTransports() {
		try {
			return port.listTransports();
		}
		catch (WebServiceException e){
			System.out.println("Caught: " + e);
			try {
				Thread.sleep(4000);
				return port.listTransports();
			}
			catch (InterruptedException e1) {
				e1.printStackTrace();
			}
		}
		return null;
	}

	public TransportView viewTransport(String id) throws UnknownTransportFault_Exception {
		try {
			return port.viewTransport(id);
		}
		catch (WebServiceException e){
			System.out.println("Caught: " + e);
			try {
				Thread.sleep(4000);
				return port.viewTransport(id);
			}
			catch (InterruptedException e1) {
				e1.printStackTrace();
			}
		}
		return null;
	}

	public void clearTransports() {
		try {
			port.clearTransports();
		}
		catch (WebServiceException e){
			try {
				Thread.sleep(4000);
				port.clearTransports();
				return;
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
		}
		return;
	}
}
