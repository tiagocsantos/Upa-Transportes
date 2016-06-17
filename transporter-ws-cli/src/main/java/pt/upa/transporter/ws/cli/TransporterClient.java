package pt.upa.transporter.ws.cli;

import java.util.*;
import javax.xml.ws.*;
import pt.upa.transporter.ws.TransporterPortType;
import pt.upa.transporter.ws.TransporterService;
import pt.upa.transporter.ws.JobView;
import pt.upa.transporter.ws.BadLocationFault_Exception;
import pt.upa.transporter.ws.BadPriceFault_Exception;
import pt.ulisboa.tecnico.sdis.ws.uddi.UDDINaming;
import pt.upa.transporter.ws.BadJobFault_Exception;

import java.util.regex.Pattern;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import javax.crypto.Cipher;
import java.util.Collection;
import javax.jws.HandlerChain;
import javax.xml.bind.DatatypeConverter;

import static javax.xml.ws.BindingProvider.ENDPOINT_ADDRESS_PROPERTY;
import static javax.xml.bind.DatatypeConverter.printHexBinary;
import static javax.xml.bind.DatatypeConverter.parseBase64Binary;
import static javax.xml.bind.DatatypeConverter.printBase64Binary;


public class TransporterClient {
	
	final static String CERTIFICATE_FILE = "UpaBroker.cer";

	final static String KEYSTORE_FILE = "UpaBroker.jks";
	final static String KEYSTORE_PASSWORD = "passwd";

	final static String KEY_ALIAS = "UpaBroker";
	final static String KEY_PASSWORD = "passwd";


	private TransporterPortType _transporter;
	private String _name;
	private String _nameTransporter;

	public TransporterClient(TransporterPortType transporter, String nameTransporter){
		_transporter = transporter;
		_name = "UpaTransporterClient";
		_nameTransporter = nameTransporter;
	}
	
	public TransporterClient(String uddiURL, String name) throws Exception{
	
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
	
		_transporter = port;
		_name = "UpaTransporterClient";
		_nameTransporter = name;
	}
	
	
	public String ping(String name) {
	
		//String encoded = encodeInfo(name);
		
		return _transporter.ping(name);
	}
	
	
	public JobView requestJob(String origin, String destination, int price) throws BadLocationFault_Exception, BadPriceFault_Exception{
		JobView job = _transporter.requestJob(origin, destination, price);
		/*System.out.println("" + job.getCompanyName() + " " + job.getJobIdentifier()
							  + " " + job.getJobOrigin() + " " + job.getJobDestination()
							  + " " + job.getJobPrice());*/
		return job;	
	}
	
	
	public JobView decideJob(String id, boolean accept) throws BadJobFault_Exception{
		JobView job = _transporter.decideJob(id, accept);
		return job;
	}
	
	
	public JobView jobStatus(String id){
		JobView job = _transporter.jobStatus(id);
		return job;
	}
	
	
	public String listJobs(){
		List<JobView> jobs = _transporter.listJobs();
		String response= "";
		for(JobView j : jobs){
			response += "" + j.getCompanyName() + " " + j.getJobIdentifier() + " "
					  + j.getJobOrigin() + " " + j.getJobDestination() + " "
					  + j.getJobPrice() + " " + j.getJobState().value();
		}
		System.out.println(response);
		return response;
			
	}
	
	
	public void clearJobs(){
		_transporter.clearJobs();
		if(_transporter.listJobs() != null)
			System.out.println("Successfully erased Jobs record from transporter " + _nameTransporter + "\n");
	}
	
	
	public String getName(){
		return _name;
	}
	
	public TransporterPortType getTransporter(){
		return _transporter;
	}
	
	public String updateStatus(String id) {
		JobView job  = _transporter.jobStatus(id);
		if (job!=null)
			return job.getJobState().value();
		else
			return null;
	}
	
	
	//Este contrutor é preciso para que o BrokerPort quanto instancia TransporterClient este se ligue ao Servidor
	public TransporterClient(int id) throws Exception{
		System.out.println("Criando TransporterClient");
		String uddiURL = "http://localhost:9090";
		String name = "UpaTransporter"+id;
		_name = "UpaTransporterClient";
		_nameTransporter = name;
	
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
		_transporter = service.getTransporterPort();
	
		System.out.println("Setting endpoint address ...");
		BindingProvider bindingProvider = (BindingProvider) _transporter;
		Map<String, Object> requestContext = bindingProvider.getRequestContext();
		requestContext.put(ENDPOINT_ADDRESS_PROPERTY, endpointAddress);
	}
	
	public String encodeInfo(String info) throws Exception{
		final byte[] plainBytes = info.getBytes();

		System.out.println("Text:");
		System.out.println(info);

		System.out.println("Bytes:");
		System.out.println(printHexBinary(plainBytes));
		
		// make digital signature
		System.out.println("Signing ...");
		byte[] digitalSignature = makeDigitalSignature(plainBytes, getPrivateKeyFromKeystore(KEYSTORE_FILE,
				KEYSTORE_PASSWORD.toCharArray(), KEY_ALIAS, KEY_PASSWORD.toCharArray()));
		
		System.out.println("Signature Bytes:");
		System.out.println(printHexBinary(digitalSignature));

		Certificate certificate = readCertificateFile(CERTIFICATE_FILE);
		PublicKey publicKey = certificate.getPublicKey();
		
		Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
		System.out.println(cipher.getProvider().getInfo());
		
		// verify the signature
		System.out.println("Verifying ...");
		boolean isValid = verifyDigitalSignature(digitalSignature, plainBytes, publicKey);

		if (isValid) {
			System.out.println("The digital signature is valid");
		} else {
			System.out.println("The digital signature is NOT valid");
		}
		
		System.out.println("Ciphering with public key ...");
		cipher.init(Cipher.ENCRYPT_MODE, publicKey);
		byte[] cipherBytes = cipher.doFinal(plainBytes);

		System.out.println("Result:");
		System.out.println(printHexBinary(cipherBytes));
		
		String encodedInfo = DatatypeConverter.printBase64Binary(cipherBytes);
		
		return encodedInfo;

	}
	
	public static PublicKey getPublicKeyFromCertificate(Certificate certificate) {
		return certificate.getPublicKey();
	}

	/**
	 * Reads a certificate from a file
	 * 
	 * @return
	 * @throws Exception
	 */
	public static Certificate readCertificateFile(String certificateFilePath) throws Exception {
		FileInputStream fis;

		try {
			fis = new FileInputStream(certificateFilePath);
		} catch (FileNotFoundException e) {
			System.err.println("Certificate file <" + certificateFilePath + "> not fount.");
			return null;
		}
		BufferedInputStream bis = new BufferedInputStream(fis);

		CertificateFactory cf = CertificateFactory.getInstance("X.509");

		if (bis.available() > 0) {
			Certificate cert = cf.generateCertificate(bis);
			return cert;
			// It is possible to print the content of the certificate file:
			// System.out.println(cert.toString());
		}
		bis.close();
		fis.close();
		return null;
	}

	/**
	 * Reads a collections of certificates from a file
	 * 
	 * @return
	 * @throws Exception
	 */
	public static Collection<Certificate> readCertificateList(String certificateFilePath) throws Exception {
		FileInputStream fis;

		try {
			fis = new FileInputStream(certificateFilePath);
		} catch (FileNotFoundException e) {
			System.err.println("Certificate file <" + certificateFilePath + "> not fount.");
			return null;
		}
		CertificateFactory cf = CertificateFactory.getInstance("X.509");
		@SuppressWarnings("unchecked")
		Collection<Certificate> c = (Collection<Certificate>) cf.generateCertificates(fis);
		fis.close();
		return c;

	}

	/**
	 * Reads a PrivateKey from a key-store
	 * 
	 * @return The PrivateKey
	 * @throws Exception
	 */
	public static PrivateKey getPrivateKeyFromKeystore(String keyStoreFilePath, char[] keyStorePassword,
			String keyAlias, char[] keyPassword) throws Exception {

		KeyStore keystore = readKeystoreFile(keyStoreFilePath, keyStorePassword);
		PrivateKey key = (PrivateKey) keystore.getKey(keyAlias, keyPassword);

		return key;
	}

	/**
	 * Reads a KeyStore from a file
	 * 
	 * @return The read KeyStore
	 * @throws Exception
	 */
	public static KeyStore readKeystoreFile(String keyStoreFilePath, char[] keyStorePassword) throws Exception {
		FileInputStream fis;
		try {
			fis = new FileInputStream(keyStoreFilePath);
		} catch (FileNotFoundException e) {
			System.err.println("Keystore file <" + keyStoreFilePath + "> not fount.");
			return null;
		}
		KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType());
		keystore.load(fis, keyStorePassword);
		return keystore;
	}

	/** auxiliary method to calculate digest from text and cipher it */
	public static byte[] makeDigitalSignature(byte[] bytes, PrivateKey privateKey) throws Exception {

		// get a signature object using the SHA-1 and RSA combo
		// and sign the plain-text with the private key
		Signature sig = Signature.getInstance("SHA1WithRSA");
		sig.initSign(privateKey);
		sig.update(bytes);
		byte[] signature = sig.sign();

		return signature;
	}

	/**
	 * auxiliary method to calculate new digest from text and compare it to the
	 * to deciphered digest
	 */
	public static boolean verifyDigitalSignature(byte[] cipherDigest, byte[] bytes, PublicKey publicKey)
			throws Exception {

		// verify the signature with the public key
		Signature sig = Signature.getInstance("SHA1WithRSA");
		sig.initVerify(publicKey);
		sig.update(bytes);
		try {
			return sig.verify(cipherDigest);
		} catch (SignatureException se) {
			System.err.println("Caught exception while verifying signature " + se);
			return false;
	}
	}
	
}