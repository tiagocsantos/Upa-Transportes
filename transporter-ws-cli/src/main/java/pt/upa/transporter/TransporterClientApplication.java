package pt.upa.transporter;

import java.util.*;
import javax.xml.ws.*;
import java.io.*;
import pt.ulisboa.tecnico.sdis.ws.uddi.UDDINaming;
import pt.upa.transporter.ws.TransporterPortType;
import pt.upa.transporter.ws.TransporterService;
import pt.upa.transporter.ws.cli.TransporterClient;
import pt.upa.transporter.ws.JobView;

import static javax.xml.ws.BindingProvider.ENDPOINT_ADDRESS_PROPERTY;

public class TransporterClientApplication {

	public static void main(String[] args) throws Exception, IOException {
		
		if (args.length < 2) {
			System.err.println("Argument(s) missing!");
			System.err.printf("Usage: java %s uddiURL name%n", TransporterClient.class.getName());
		}

		String uddiURL = args[0];
		String name = args[1];

		System.out.printf("Contacting UDDI at %s%n", uddiURL);
		UDDINaming uddiNaming = new UDDINaming(uddiURL);

		System.out.printf("Looking for '%s'%n", name);
		String endpointAddress = uddiNaming.lookup(name);

		if (endpointAddress == null) {
			System.out.println("Not found!");
			return;
		} else {
			System.out.printf("Found %s%n", endpointAddress);
		}

		System.out.println("Creating stub ...");
		TransporterService service = new TransporterService();
		TransporterPortType port = service.getTransporterPort();

		System.out.println("Setting endpoint address ...");
		BindingProvider bindingProvider = (BindingProvider) port;
		Map<String, Object> requestContext = bindingProvider.getRequestContext();
		requestContext.put(ENDPOINT_ADDRESS_PROPERTY, endpointAddress);
		
		
		TransporterClient client = new TransporterClient(port, name);
		
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

		
		String origin;
		String destination;
		String id;
		String decision;
		boolean accept = false;
		int price;
		int point = 0;
		JobView job;
		
		do{
			
			System.out.println("Choose operation:" + "\n" 
					+ "1 - Ping" + "\n"
					+ "2 - Request Job" + "\n"
					+ "3 - Check Job Status" + "\n" 
					+ "4 - List Jobs" + "\n" 
					+ "5 - Clear All Jobs" + "\n" 
					+ "6 - Quit" + "\n" );
			
			String input = br.readLine();
			
			switch(input) {
			case "1":
				String response = client.ping(client.getName());
				System.out.println(response);
				break;
			case "2":
				System.out.println("Insert origin:");
				origin = br.readLine();
				System.out.println("Insert destination:");
				destination = br.readLine();
				System.out.println("Insert price:");
				price = Integer.parseInt(br.readLine());
			
				job = client.requestJob(origin, destination, price);
				
				System.out.println(job.getJobPrice());
				
				System.out.println("Accept price?");
				decision = br.readLine();
				if(decision.equals("yes"))
					job = client.decideJob(job.getJobIdentifier(), true);
				
				System.out.println("" + job.getCompanyName() + " " + job.getJobIdentifier() + " " 
									  + job.getJobOrigin() + " " + job.getJobDestination() + " " 
									  + job.getJobPrice() + " " + job.getJobState().value());
				break;
			case "3":
				System.out.println("Enter Job Identifier String:");
				id = br.readLine();
				job = client.jobStatus(id);
				System.out.println(job.getJobState().value());
				break;
			case "4":
				client.listJobs();
				break;
			case "5":
				client.clearJobs();
				break;
			case "6":
				point = 1;
				break;
			}
		}while(point != 1);
	}
}
