package pt.upa.transporter.ws.handler;

import java.util.Iterator;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.soap.Name;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPHeaderElement;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;

public class NamingHandler implements SOAPHandler<SOAPMessageContext>{
	public static final String SENDER = "Sender";
	public static final String RECEIVER = "Receiver";
	public static final String RESPONSE_NS = "urn:example";
	
	private String transporterName;
	@Override
	public boolean handleMessage(SOAPMessageContext smc) {
		//System.out.println("NAMING HANDLER");
		Boolean outboundElement = (Boolean) smc.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);
		if (outboundElement.booleanValue()){
			//regista quem envia a mensagem e para quems
			try {
				// get SOAP envelope
				SOAPMessage msg = smc.getMessage();
				SOAPPart sp = msg.getSOAPPart();
				SOAPEnvelope se = sp.getEnvelope();
				
				SOAPHeader sh = se.getHeader();
				if (sh == null)
					sh = se.addHeader();
				
				Name name = se.createName(SENDER, "e", RESPONSE_NS);
				SOAPHeaderElement element = sh.addHeaderElement(name);
				element.addTextNode(transporterName);
				
				Name name2 = se.createName(RECEIVER, "e", RESPONSE_NS);
				SOAPHeaderElement element2 = sh.addHeaderElement(name2);
			
				String newValue2 = "UpaBroker";
				element2.addTextNode(newValue2);
			    
				msg.saveChanges();
			}
			catch (Exception e){
				System.out.println(e.getMessage());
			}
			
		}
		else{
			//inbound
			//guarda o seu proprio nome
			try {
				SOAPMessage msg = smc.getMessage();
				SOAPPart sp = msg.getSOAPPart();
				SOAPEnvelope se = sp.getEnvelope();
				SOAPHeader sh = se.getHeader();
				
				Name name = se.createName(RECEIVER, "e", RESPONSE_NS);
				Iterator it = sh.getChildElements(name);
				// check header element
				if (!it.hasNext()) {
					System.out.printf("Header element %s not found.%n", RECEIVER);
					return true;
				}
				SOAPElement element = (SOAPElement) it.next();
				String headerValue = element.getValue();
				transporterName = headerValue;
				//System.out.println("******\n"+headerValue);
			}
			catch (Exception e){
				System.out.println(e.getMessage());
			}
			
		}
		return true;
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

}
