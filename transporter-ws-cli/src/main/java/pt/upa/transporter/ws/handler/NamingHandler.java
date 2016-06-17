package pt.upa.transporter.ws.handler;

import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.soap.Name;
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
	public static final String REQUEST_NS = "urn:example";
	@Override
	public boolean handleMessage(SOAPMessageContext smc) {
		
	    Boolean outbound = (Boolean) smc.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);
	    if (outbound){
	    	try {
				// get SOAP envelope
				SOAPMessage msg = smc.getMessage();
				SOAPPart sp = msg.getSOAPPart();
				SOAPEnvelope se = sp.getEnvelope();
				
				SOAPHeader sh = se.getHeader();
				if (sh == null)
					sh = se.addHeader();
				
				Name name = se.createName(SENDER, "e", REQUEST_NS);
				SOAPHeaderElement element = sh.addHeaderElement(name);
				String newValue = "UpaBroker";
				element.addTextNode(newValue);
				
				Name name2 = se.createName(RECEIVER, "e", REQUEST_NS);
				SOAPHeaderElement element2 = sh.addHeaderElement(name2);
			
				String newValue2 = getName(smc);
				element2.addTextNode(newValue2);
			    
				msg.saveChanges();
				
	    	}
	    	catch (Exception e){
	    		System.out.println(e.getMessage());
	    	}
	    	
	    }
		
		return true;
	}
	
	public String getName(SOAPMessageContext smc){
		String endpointAddress = (String) smc.get(BindingProvider.ENDPOINT_ADDRESS_PROPERTY);
	//	System.out.println(endpointAddress);
		String[] parts  = endpointAddress.split("/");
		String[] info = parts[2].split(":");
		String port = info[1];
		if (port.equals("8081"))
			return "UpaTransporter1";
		else if (port.equals("8082"))
			return "UpaTransporter2";
		else 
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

}
