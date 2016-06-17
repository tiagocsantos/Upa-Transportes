package example.ws.handler;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.StringWriter;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.Iterator;
import java.util.Set;

import javax.xml.bind.DatatypeConverter;
import javax.xml.namespace.QName;
import javax.xml.soap.Name;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPHeaderElement;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import ca.ws.cli.CAClient;


public class SigningHandler implements SOAPHandler<SOAPMessageContext> {
	public static final String NS = "urn:example";
	
	
	@Override
	public boolean handleMessage(SOAPMessageContext smc) {
		String senderName = getName(smc, "Sender");
		
		Boolean outboundElement = (Boolean) smc.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);
		if (outboundElement.booleanValue()) {
			//outbund
			try {
				byte[] messageBytes =  getMessageBytes(smc);
				
				//gerar e assinar resumo
				byte[] signedDigest = signDigest(messageBytes, senderName);
				
				//poe resumo na mensagem
				SOAPMessage msg = smc.getMessage();
				SOAPPart sp = msg.getSOAPPart();
				SOAPEnvelope se = sp.getEnvelope();
				
				SOAPHeader sh = se.getHeader();
				if (sh == null)
					sh = se.addHeader();
				
				Name name = se.createName("Digest", "Security", "urn:example");
				SOAPHeaderElement element = sh.addHeaderElement(name);
				String newValue = DatatypeConverter.printBase64Binary(signedDigest);
				element.addTextNode(newValue);
				
				msg.saveChanges();
			
			}
			catch (Exception e){
				System.out.println(e.getMessage());
			}
			
		}
		else {
			//inbound
			try {
				//tirar digest da mensagem
				SOAPMessage msg = smc.getMessage();
				SOAPPart sp = msg.getSOAPPart();
				SOAPEnvelope se = sp.getEnvelope();
				SOAPHeader sh = se.getHeader();
				
				Name name = se.createName("Digest", "Security", "urn:example");
				
				Iterator it = sh.getChildElements(name);
				// check header element
				if (!it.hasNext()) {
					System.out.println("Security Header not Found");
					return false;
				}
				SOAPElement element = (SOAPElement) it.next();
				String digestString = element.getValue();
				
				byte[] messageSignature  = DatatypeConverter.parseBase64Binary(digestString);
				
				element.detachNode();
				msg.saveChanges();
				
				byte[] messageBytes = getMessageBytes(smc);

				String CERTIFICATE_FILE = senderName+".cer";
				Certificate certificate = readCertificateFile(CERTIFICATE_FILE);
				PublicKey publicKey = certificate.getPublicKey();
				
				boolean isValid = verifyDigitalSignature(messageSignature, messageBytes, publicKey);
				
				if (isValid) {
					System.out.println("The digital signature is valid");
				} else {
					System.out.println("The digital signature is NOT valid");
					return false;
				}

								
			}
			catch (Exception e){
				System.out.println(e.getMessage());
			}
				
		}
		
		return true;
	}
	

	private byte[] getMessageBytes(SOAPMessageContext smc) {
		SOAPMessage msg = smc.getMessage();
		try{
			SOAPBody element = msg.getSOAPBody();
			DOMSource source = new DOMSource(element);
			StringWriter stringResult = new StringWriter();
			TransformerFactory.newInstance().newTransformer().transform(source, new StreamResult(stringResult));
			String message = stringResult.toString();
			return  message.getBytes();
		}
		catch (Exception e){
			System.out.println(e.getMessage());
		}
		return null;
	}


	private byte[] signDigest(byte[] plainBytes, String senderName) {
		String KEYSTORE_FILE = senderName+".jks";
		String KEYSTORE_PASSWORD = "ins3cur3";
		String KEY_ALIAS = senderName;
		String KEY_PASSWORD = "1nsecure";
		byte[] digitalSignature = null;
		try {
			digitalSignature = makeDigitalSignature(plainBytes, getPrivateKeyFromKeystore(KEYSTORE_FILE,
					KEYSTORE_PASSWORD.toCharArray(), KEY_ALIAS, KEY_PASSWORD.toCharArray()));
		} catch (Exception e) {
			System.out.println(e.getMessage());;
		}
		return digitalSignature;
	}

	public String getName(SOAPMessageContext smc, String className){
		//para ir buscar o nome escrito pelo naminghandler
		try {
			SOAPMessage msg = smc.getMessage();
			SOAPPart sp = msg.getSOAPPart();
			SOAPEnvelope se = sp.getEnvelope();
			SOAPHeader sh = se.getHeader();
			
			Name name = se.createName(className, "e",NS);
			Iterator it = sh.getChildElements(name);
			// check header element
			if (!it.hasNext()) {
				System.out.printf("Header element not found");
				return null;
			}
			SOAPElement element = (SOAPElement) it.next();
			String headerValue = element.getValue();
		//	System.out.println("***\n"+className+"   "+headerValue);
			return headerValue;
		}
		catch (Exception e){
			System.out.println(e.getMessage());
		}	
		return null;
	}
		
	@Override
	public void close(MessageContext arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean handleFault(SOAPMessageContext arg0) {
		// TODO Auto-generated method stub
		return false;
	}
	
	@Override
	public Set<QName> getHeaders() {
		// TODO Auto-generated method stub
		return null;
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
	 * Reads a certificate from a file
	 * 
	 * @return
	 * @throws Exception
	 */
	public static Certificate readCertificateFile(String certificateFilePath) throws Exception {
		FileInputStream fis = null;

		try {
			fis = new FileInputStream(certificateFilePath);
			System.out.println("Certifcate Found");
		} catch (FileNotFoundException e) {
			System.err.println("Certificate file <" + certificateFilePath + "> not found.");
			System.out.println("Asking for Certificate to CA");
			CAClient ca = new CAClient();
			boolean ok = ca.getCertificate(certificateFilePath);
			if(!ok)
				return null;
			else {
				System.out.println("Got Certificate");
				verifyCertificate(certificateFilePath);
				fis = new FileInputStream(certificateFilePath);
			}
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
	
	private static void verifyCertificate(String certificateFilePath) {
		String CA_CERTIFICATE_FILE = "ca-certificate.pem.txt";
		String CERTIFICATE_FILE = certificateFilePath;
		Certificate certificate = null;
		Certificate caCertificate;
		PublicKey caPublicKey = null;
		try {
			 certificate = readCertificateFile(CERTIFICATE_FILE);
			 caCertificate = readCertificateFile(CA_CERTIFICATE_FILE);
			 caPublicKey = caCertificate.getPublicKey();
		}
		catch (Exception e){
			System.out.println(e.getMessage());
		}
		if (verifySignedCertificate(certificate, caPublicKey)) {
			System.out.println("The signed certificate is valid");
		} else {
			System.err.println("The signed certificate is not valid");
		}
		
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
	
	/**
	 * Verifica se um certificado foi devidamente assinado pela CA
	 * 
	 * @param certificate
	 *            certificado a ser verificado
	 * @param caPublicKey
	 *            certificado da CA
	 * @return true se foi devidamente assinado
	 */
	public static boolean verifySignedCertificate(Certificate certificate, PublicKey caPublicKey) {
		try {
			certificate.verify(caPublicKey);
		} catch (InvalidKeyException | CertificateException | NoSuchAlgorithmException | NoSuchProviderException
				| SignatureException e) {
			// O método Certifecate.verify() não retorna qualquer valor (void).
			// Quando um certificado é inválido, isto é, não foi devidamente
			// assinado pela CA
			// é lançada uma excepção: java.security.SignatureException:
			// Signature does not match.
			// também são lançadas excepções caso o certificado esteja num
			// formato incorrecto ou tenha uma
			// chave inválida.

			return false;
		}
		return true;
	}
}
