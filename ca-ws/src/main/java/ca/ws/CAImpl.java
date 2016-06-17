package ca.ws;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.jws.WebService;

@WebService(endpointInterface = "ca.ws.CA")
public class CAImpl implements CA {

	@Override
	public byte[] askForCertificate(String name) {
		System.out.println("Requested "+ name+ " certificate");
		byte[] cert = null;
		BufferedReader br = null;
		try {
			Path path = Paths.get("keys\\"+name+"\\"+name+".cer");
			cert = Files.readAllBytes(path);
			//System.out.write(cert);
		} catch (Exception e) {
			System.out.println("Cerificate for " + name + "not found");
		}		
		return cert;
	}



}
