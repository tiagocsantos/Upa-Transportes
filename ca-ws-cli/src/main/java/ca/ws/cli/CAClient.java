package ca.ws.cli;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import ca.ws.CA;
import ca.ws.CAImplService;
import pt.ulisboa.tecnico.sdis.ws.uddi.UDDINaming;

public class CAClient {
	private static CA port;
	
	public CAClient(){
		String uddiURL = "http://localhost:9090";
		String name = "CA";

		System.out.printf("Contacting UDDI at %s%n", uddiURL);
		UDDINaming uddiNaming;
		String endpointAddress = null;
		try {
			uddiNaming = new UDDINaming(uddiURL);
			System.out.printf("Looking for '%s'%n", name);
			endpointAddress = uddiNaming.lookup(name);
		}
		catch (Exception e){
			System.out.println(e.getMessage());
		}

		if (endpointAddress == null) {
			System.out.println("Not found!");
			return;
		} else {
			System.out.printf("Found %s%n", endpointAddress);
		}

		System.out.println("Creating stub ...");
		CAImplService service = new CAImplService();
		port = service.getCAImplPort();
		System.out.println("CONNECTION DONE");
	}
	
	public boolean getCertificate(String name){
		System.out.println("REQUESTED "+name);
		String[] wsname = name.split("\\.");
		byte[] cert = port.askForCertificate(wsname[0]);
		Path certfile = Paths.get(name);
		try {
			Files.write(certfile, cert);
			System.out.println("File Created");
		} catch (IOException e) {
			System.out.println("Cannot Write Certificate File");
			return false;
		}
		return true;
	}
}
