package pt.upa.broker;

import java.util.List;
import java.util.Scanner;

import com.sun.xml.ws.client.ClientTransportException;

import pt.upa.broker.ws.TransportView;
import pt.upa.broker.ws.cli.BrokerClient;

public class BrokerClientApplication {
	static BrokerClient port;
	
	public static void main(String[] args) throws Exception {
		System.out.println(BrokerClientApplication.class.getSimpleName() + " starting...");
		
		// Check arguments
				if (args.length < 1) {
					System.err.println("Argument(s) missing!");
					System.err.printf("Usage: java %s uddiURL name%n", BrokerClientApplication.class.getName());
					return;
				}
				port = new BrokerClient(args[0]);
				
				Scanner scanner = new Scanner(System.in);
				int command, origin=20, dest=20, price;
				String id;
				String[] cidades = {"Porto", "Braga", "Viana do Castelo", "Vila Real", "Bragança", 
						"Lisboa", "Leiria", "Santarém", "Castelo Branco", "Coimbra", "Aveiro", "Viseu", "Guarda",
						"Setubal", "Évora", "Portalegre", "Beja", "Faro"};
				
				System.out.println("Welcome to BrokerApplication");
				System.out.println("Menu:");
				while(true){
					System.out.println("1 - Request Transport\n2 - View Transport\n3 - Ping Transporters\n4 - List Transports\n5 - Clear all Transports\n6 - Exit");
					command = scanner.nextInt();
					switch(command){
						case(1):
							System.out.println("1-Porto 2-Braga 3-Viana do Castelo 4-Vila Real 5-Bragança 6-Lisboa\n"+
												"7-Leiria 8-Santarém 9-Castelo Branco 10-Coimbra 11-Aveiro 12-Viseu\n"+ 
												"13-Guarda 14-Setubal 15-Évora 16-Portalegre 17-Beja 18-Faro");
							while(origin > 18 | origin < 0){
								System.out.println("Select Origin:");
								origin = scanner.nextInt();
								scanner.nextLine();
							}
						
							while(dest > 18 | dest < 0){
								System.out.println("Select Destination:");
								dest = scanner.nextInt();
								scanner.nextLine();
							}
							System.out.println("Select Max Price:");
							price = scanner.nextInt();
							scanner.nextLine();
							try{
								id = port.requestTransport(cidades[origin], cidades[dest], price);
								System.out.println("New Transport id = "+ id);
							}
							catch (Exception e){
								System.out.println(e.getMessage());
							}				
							dest =origin= 20;
							break;
						case(2):
							scanner.nextLine();
							System.out.println("Transport Id:");
							id = scanner.nextLine();
							try {
								export(port.viewTransport(id));
							}
							catch (Exception e){
								System.out.println(e.getMessage());
							}
							
							break;
						case(3):
							scanner.nextLine();
							System.out.println("String:");
							id = scanner.nextLine();
							try{
								System.out.println(port.ping(id));	
							}
							catch(Exception e){
								System.out.println(e.getMessage());
							}
							break;
						case(4):
							List<TransportView> list = port.listTransports();
							for (TransportView t : list){
								export(t);
							}
							break;
						case(5):
							port.clearTransports();
							break;
						case(6):
							scanner.close();
							return;
					}
				}
	}
	
	private static void export(TransportView t){
		System.out.println("Transport: " +t.getId());
		System.out.println("Company: "+t.getTransporterCompany());
		System.out.println("Origin: "+t.getOrigin());
		System.out.println("Destination: "+t.getDestination());
		System.out.println("Price: "+t.getPrice());
		System.out.println("State: "+t.getState().toString());
	}
	

}